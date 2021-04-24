import fxcmpy
import time
import datetime as dt
from pyti.relative_strength_index import relative_strength_index as rsi

print('''
  _ __ ___ | |__   ___ | |_ ___ 
 | '__/ _ \| '_ \ / _ \| __/ __|
 | | | (_) | |_) | (_) | |_\__ \_
 |_|  \___/|_.__/ \___/ \__|___/
 
Version 2

By 

Juan Gabriel Perez Guerra
''')

token = '3410a757b991fb7bbfa96fa947edff8d4fcd0a32'
symbol = 'EUR/USD'

timeframe = "m15"  # (m1,m5,m15,m30,H1,H2,H3,H4,H6,H8,D1,W1,M1)

# **************************************************
# Parametros RSI
# **************************************************

rsi_periods = 14
upper_rsi = 70.0
lower_rsi = 30.0

# **************************************************
# Parametros APERTURA Y CIERRE DE OPERACIONES
# **************************************************
amount = 5
stop = -5
limit = 5

# Global Variables
pricedata = None
numberofcandles = 300

con = fxcmpy.fxcmpy(access_token=token, log_level="error", log_file=None)


def Prepare():
    global pricedata
    print("Solicitando Precios...")
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    print(pricedata)
    print("Precios Iniciales Recibidos")


# Esta Funcion realiza una orden en la direccion indicada     BuySell, "B" = Buy, "S" = Sell, usa las variables usas symbol, amount, stop, limit
def enter(BuySell):
    direction = True;
    if BuySell == "S":
        direction = False;
    try:
        opentrade = con.open_trade(symbol=symbol, is_buy=direction, amount=amount, time_in_force='GTC',
                                   order_type='AtMarket', is_in_pips=True, limit=limit, stop=stop)
    except:
        print("   Error Opening Trade.")
    else:
        print("   Trade Opened Successfully.")



# Esta función cierra todas las posiciones que están en la dirección BuySell
# "B" = Cerrar todas las posiciones de compra,
# "S" = Cerrar todas las posiciones de venta,
def exit(BuySell=None):
    openpositions = con.get_open_positions(kind='list')
    isbuy = True
    if BuySell == "S":
        isbuy = False
    for position in openpositions:
        if position['currency'] == symbol:
            if BuySell is None or position['isBuy'] == isbuy:
                print("	  Cerrando Operacion: " + position['tradeId'])
                try:
                    closetrade = con.close_trade(trade_id=position['tradeId'], amount=position['amountK'])
                except:
                    print("	  Error cerrando la operacion.")
                else:
                    print("	  Operacion Cerrada Satisfactoriamente.")

# Retorna Verdadero si stream1 sobre pasa sream2 en la vela mas reciente, stream2 puede ser integer/float or data array
def crossesOver(stream1, stream2):
    if isinstance(stream2, int) or isinstance(stream2, float):
        if stream1[len(stream1) - 1] <= stream2:
            return False
        else:
            if stream1[len(stream1) - 2] > stream2:
                return False
            elif stream1[len(stream1) - 2] < stream2:
                return True
            else:
                x = 2
                while stream1[len(stream1) - x] == stream2:
                    x = x + 1
                if stream1[len(stream1) - x] < stream2:
                    return True
                else:
                    return False
    else:
        if stream1[len(stream1) - 1] <= stream2[len(stream2) - 1]:
            return False
        else:
            if stream1[len(stream1) - 2] > stream2[len(stream2) - 2]:
                return False
            elif stream1[len(stream1) - 2] < stream2[len(stream2) - 2]:
                return True
            else:
                x = 2
                while stream1[len(stream1) - x] == stream2[len(stream2) - x]:
                    x = x + 1
                if stream1[len(stream1) - x] < stream2[len(stream2) - x]:
                    return True
                else:
                    return False




# Retorna Verdadero si stream cruza abajo de stream2 in la vela mas reciente, stream2 can be integer/float or data array
def crossesUnder(stream1, stream2):
    if isinstance(stream2, int) or isinstance(stream2, float):
        if stream1[len(stream1) - 1] >= stream2:
            return False
        else:
            if stream1[len(stream1) - 2] < stream2:
                return False
            elif stream1[len(stream1) - 2] > stream2:
                return True
            else:
                x = 2
                while stream1[len(stream1) - x] == stream2:
                    x = x + 1
                if stream1[len(stream1) - x] > stream2:
                    return True
                else:
                    return False
    else:
        if stream1[len(stream1) - 1] >= stream2[len(stream2) - 1]:
            return False
        else:
            if stream1[len(stream1) - 2] < stream2[len(stream2) - 2]:
                return False
            elif stream1[len(stream1) - 2] > stream2[len(stream2) - 2]:
                return True
            else:
                x = 2
                while stream1[len(stream1) - x] == stream2[len(stream2) - x]:
                    x = x + 1
                if stream1[len(stream1) - x] > stream2[len(stream2) - x]:
                    return True
                else:
                    return False


# Retorna el numero de posiciones abiertas para el symbol en la direccion de compra,
# retorna el total de numeros de ambos de compr ay venta, si la direccion no es especificada
def countOpenTrades(BuySell=None):
    openpositions = con.get_open_positions(kind='list')
    isbuy = True
    counter = 0
    if BuySell == "S":
        isbuy = False
    for position in openpositions:
        if position['currency'] == symbol:
            if BuySell is None or position['isBuy'] == isbuy:
                counter += 1
    return counter


# Funcion Corre cuando las velas cierran
def Update():

    print(str(dt.datetime.now()) + "     " + timeframe + " Vela Cerrada - Corriendo Funcion de Actualizacion...")

    # Calcular Indicador
    iRSI = rsi(pricedata['bidclose'], rsi_periods)

    # Print Price/Indicators
    print("Precio de Cierre: " + str(pricedata['bidclose'][len(pricedata) - 1]))
    print("RSI: " + str(iRSI[len(iRSI) - 1]))

    # si RSI cruza el lower_rsi, Abrir Operacion de Trade
    if crossesOver(iRSI, lower_rsi):
        print("	 SEÑAL DE COMPRA ! \n")
        print('''        
                     __,_,
                     [_|_/ 
                      //
                    _//    __
                   (_|)   |@@|
                    \ \__ \--/ __
                     \o__|----|  |   __
                         \ }{ /\ )_ / _\_
                         /\__/\ \__O (__
                        (--/\--)    \__/
                        _)(  )(_
                       `---''---`
                   ''')
        print("	 SEÑAL DE COMPRA ! \n")
        enter("B")
    # If RSI crosses under upper_rsi, Open Sell Trade
    if crossesUnder(iRSI, upper_rsi):
        print("	  SEÑAL DE VENTA ! \n")
        print('''
                     __
                 _  |@@|
                / \ \--/ __
                ) O|----|  |   __
               / / \ }{ /\ )_ / _\_
               )/  /\__/\ \__O (__
              |/  (--/\--)    \__/
              /   _)(  )(_
                 `---''---`

              ''')
        print("	  SEÑAL DE VENTA ! \n")
        enter("S")


    # Logica de Cierre
    # If RSI is superior than upper_rsi y hay una operacion de compra cierra las operaciones
    if iRSI[len(iRSI) - 1] > upper_rsi and countOpenTrades("B") > 0:
        print("   RSI above " + str(upper_rsi) + ". Closing Buy Trade(s)...")
        exit("B")

    # If RSI is inferior que lower_rsi y tenemos una peracion de venta cierre todas las operaciones de venta.
    if iRSI[len(iRSI) - 1] < lower_rsi and countOpenTrades("S") > 0:
        print("   RSI below " + str(lower_rsi) + ". Closing Sell Trade(s)...")
        exit("S")

    print(str(dt.datetime.now()) + "     " + timeframe + " Actualizacion Completada.\n")




def StrategyStart():
    while True:
        currenttime = dt.datetime.now()
        if timeframe == "m1" and currenttime.second == 0 and getLatestPriceData():
            Update()
            time.sleep(10)
        elif timeframe == "m5" and currenttime.second == 0 and currenttime.minute % 5 == 0 and getLatestPriceData():
            Update()
            time.sleep(240)
        elif timeframe == "m15" and currenttime.second == 0 and currenttime.minute % 15 == 0 and getLatestPriceData():
            Update()
            time.sleep(840)
        elif timeframe == "m30" and currenttime.second == 0 and currenttime.minute % 30 == 0 and getLatestPriceData():
            Update()
            time.sleep(1740)
        elif currenttime.second == 0 and currenttime.minute == 0 and getLatestPriceData():
            Update()
            time.sleep(3540)
        time.sleep(1)


def getLatestPriceData():
    global pricedata

    new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != pricedata.index.values[
        len(pricedata.index.values) - 1]:
        pricedata = new_pricedata
        return True

    counter = 0
    while new_pricedata.index.values[len(new_pricedata.index.values) - 1] == pricedata.index.values[
        len(pricedata.index.values) - 1] and counter < 3:
        print("No hay precios actualizados intentando en 10 Segundos...")
        counter += 1
        time.sleep(10)
        new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)

    if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != pricedata.index.values[
        len(pricedata.index.values) - 1]:
        pricedata = new_pricedata
        return True
    else:
        return False


Prepare()  # Perar la Estrategia
StrategyStart()  # Iniciar la Estrategia
