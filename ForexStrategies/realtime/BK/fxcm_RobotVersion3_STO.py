import fxcmpy
import time
import datetime as dt
from pyti.stochastic import percent_k as per_k
from pyti.stochastic import percent_d as per_d


print('''
  _ __ ___ | |__   ___ | |_ ___ 
 | '__/ _ \| '_ \ / _ \| __/ __|
 | | | (_) | |_) | (_) | |_\__ \_
 |_|  \___/|_.__/ \___/ \__|___/
 
By 

Juan Gabriel Perez Guerra

''')

# **************************************************
# Parametros Generales
# **************************************************

token = '3410a757b991fb7bbfa96fa947edff8d4fcd0a32'
symbol = 'EUR/JPY'

# Periodos : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = "m1"

# **************************************************
# Parametros STO
# **************************************************



# **************************************************
# Parametros APERTURA Y CIERRE DE OPERACIONES
# **************************************************

amount = 5
stop = -5
limit = 5

# **************************************************
# PARAMETROS CANTIDAD DE VELAS
# **************************************************
numberofcandles = 300


# **************************************************
# FIN DE PARAMETROS
# **************************************************


# Global Variables
pricedata = None

# Conexion con FXCM
#data = socket.get_candles(instrument = 'GBP/USD', period = 'D1', start = dt.datetime(2017,1,1), end = dt.datetime(2018, 6, 10))
con = fxcmpy.fxcmpy(access_token=token, log_level="error", log_file=None)

def Prepare():
    global pricedata
    print("Solicitando Precios...")
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    print(pricedata)
    print("Precios Iniciales Recibidos")

def StrategyStart():
    while True:
        currenttime = dt.datetime.now()
        if timeframe == "m1" and currenttime.second == 0 and getLatestPriceData():
            Update()
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
    if new_pricedata.index.values[len(new_pricedata.index.values)-1] != pricedata.index.values[len(pricedata.index.values)-1]:
        pricedata = new_pricedata
        return True

    counter = 0 
    while new_pricedata.index.values[len(new_pricedata.index.values)-1] == pricedata.index.values[len(pricedata.index.values)-1] and counter < 3:
        print("No updated prices found, trying again in 10 seconds...")
        counter+=1
        time.sleep(10)
        new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)

    if new_pricedata.index.values[len(new_pricedata.index.values)-1] != pricedata.index.values[len(pricedata.index.values)-1]:
        pricedata = new_pricedata
        return True
    else:
        return False

def Update():

    print('''
          [ ]
         (   )
          |>|
       __/===\__
      _/_/| o=o |\_\_ 
    <]  | o=o |  [>
        \=====/
      _/ / | \ \_
      <_________>
    ''')

    print(str(dt.datetime.now()) + " " + timeframe + " Vela Formada - Analizando -  Running Update Function...")


    data_per_k = per_k(pricedata['bidclose'], 14)
    data_per_d = per_d(pricedata['bidclose'], 3)




    # si RSI Cruza el upper_rsi, Abrir una operacion de Venta
    if crossesUnder(iRsi, upper_rsi):
        print("   SELL SIGNAL!")
        print("   Opening Sell Trade...")
        #enter("S")

    # Exit Logic
    # SI RSI es superario a upper_rsi and tenemos una operacion en compra procedemos a cerrarla
    if iRsi[len(iRsi)-1] > upper_rsi and countOpenTrades("B") > 0:
        print("   RSI Superior " + str(upper_rsi) + ". Cerrando Operaciones en Compra")
        #exit("B")
    # si RSI es menor que lower_rsi and y tenemos una peracion en venta procedemos a cerrarla
    if iRsi[len(iRsi)-1] < lower_rsi and countOpenTrades("S") > 0:
        print("   RSI inferior " + str(lower_rsi) + ". Cerrando Operaciones en Venta")
        #exit("S")


    # Valores del Stoochastic
    percent_k = sto.percent_k(pricedata['bidclose'], 6)
    percent_d = sto.percent_d(pricedata['bidclose'], 6)
    percent_d_slow = sto.percent_d(pricedata['bidclose'], 6)

    # Logica Robot Con los Indicadores

    # Verifica el Cruce del SMA para Arriba.
    if crossesOver(iFastSMA,iSlowSMA):
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
        if countOpenTrades("S") > 0:
            print("	  Cerrando Ventas Abiertas...\n")
            exit("S")
        print("	  Abrir Operacion de Compra...\n")
        enter("B")
    # Verifica el Cruce del SMA para Abajo.
    if crossesUnder(iFastSMA,iSlowSMA):
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
        if countOpenTrades("B") > 0:
            print("	  Cerrando Operacion de Compras...\n")
            exit("B")
        print("	  Abrir Operacion de Venta...\n")
        enter("S")
    print(str(dt.datetime.now()) + " " + timeframe + " Verificacion Realizada.\n")
    print("\n")



# Retorna Verdadero si stream1 sobre pasa sream2 en la vela mas reciente, stream2 puede ser integer/float or data array
def crossesOver(stream1, stream2):
    if isinstance(stream2, int) or isinstance(stream2, float):
        if stream1[len(stream1)-1] <= stream2:
            return False
        else:
            if stream1[len(stream1)-2] > stream2:
                return False
            elif stream1[len(stream1)-2] < stream2:
                return True
            else:
                x = 2
                while stream1[len(stream1)-x] == stream2:
                    x = x + 1
                if stream1[len(stream1)-x] < stream2:
                    return True
                else:
                    return False
    else:
        if stream1[len(stream1)-1] <= stream2[len(stream2)-1]:
            return False
        else:
            if stream1[len(stream1)-2] > stream2[len(stream2)-2]:
                return False
            elif stream1[len(stream1)-2] < stream2[len(stream2)-2]:
                return True
            else:
                x = 2
                while stream1[len(stream1)-x] == stream2[len(stream2)-x]:
                    x = x + 1
                if stream1[len(stream1)-x] < stream2[len(stream2)-x]:
                    return True
                else:
                    return False

# Retorna Verdadero si stream cruza abajo de stream2 in la vela mas reciente, stream2 can be integer/float or data array
def crossesUnder(stream1, stream2):
    if isinstance(stream2, int) or isinstance(stream2, float):
        if stream1[len(stream1)-1] >= stream2:
            return False
        else:
            if stream1[len(stream1)-2] < stream2:
                return False
            elif stream1[len(stream1)-2] > stream2:
                return True
            else:
                x = 2
                while stream1[len(stream1)-x] == stream2:
                    x = x + 1
                if stream1[len(stream1)-x] > stream2:
                    return True
                else:
                    return False
    else:
        if stream1[len(stream1)-1] >= stream2[len(stream2)-1]:
            return False
        else:
            if stream1[len(stream1)-2] < stream2[len(stream2)-2]:
                return False
            elif stream1[len(stream1)-2] > stream2[len(stream2)-2]:
                return True
            else:
                x = 2
                while stream1[len(stream1)-x] == stream2[len(stream2)-x]:
                    x = x + 1
                if stream1[len(stream1)-x] > stream2[len(stream2)-x]:
                    return True
                else:
                    return False

# Esta Funcion realiza una orden en la direccion indicada     BuySell, "B" = Buy, "S" = Sell, usa las variables usas symbol, amount, stop, limit
'''    open_trade(self, symbol, is_buy,               amount, time_in_force, order_type, rate=0,       is_in_pips=True, limit=None, at_market=0, stop=None,   trailing_step=None, account_id=None):
'''
def enter(BuySell):
    direction = True;
    if BuySell == "S":
        direction = False;
    try:
        opentrade = con.open_trade(symbol=symbol, is_buy=direction,amount=amount, time_in_force='GTC',order_type='AtMarket',is_in_pips=True,limit=limit, stop=stop, trailing_step=1)
    except:
        print("	  Error Opening Trade.")
    else:
        print("	  Trade Opened Successfully.")


# In[ ]:


# This function closes all positions that are in the direction BuySell, "B" = Close All Buy Positions, 
# "S" = Close All Sell Positions, uses symbol

def exit(BuySell=None):
    openpositions = con.get_open_positions(kind='list')
    isbuy = True
    if BuySell == "S":
        isbuy = False
    for position in openpositions:
        if position['currency'] == symbol:
            if BuySell is None or position['isBuy'] == isbuy:
                print("	  Closing tradeID: " + position['tradeId'])
                try:
                    closetrade = con.close_trade(trade_id=position['tradeId'], amount=position['amountK'])
                except:
                    print("	  Error Closing Trade.")
                else:
                    print("	  Trade Closed Successfully.")


# Retorna el numero de posiciones abiertas para el symbol in la direccion de compra, retorna el total de numeros de ambos de compr ay venta, si la direccion no es especificada
def countOpenTrades(BuySell=None):
    openpositions = con.get_open_positions(kind='list')
    isbuy = True
    counter = 0
    if BuySell == "S":
        isbuy = False
    for position in openpositions:
        if position['currency'] == symbol:
            if BuySell is None or position['isBuy'] == isbuy:
                counter+=1
    return counter



Prepare() # Perar la Estrategia
StrategyStart() # Iniciar la Estrategia
