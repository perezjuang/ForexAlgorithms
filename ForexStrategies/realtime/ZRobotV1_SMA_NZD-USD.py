import configparser
import datetime as dt
import os
import time
# import only system from os
from os import system, name
# import sleep to show output for some time period
from time import sleep
import fxcmpy
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from pyti.simple_moving_average import simple_moving_average as sma
from pyti.stochastic import percent_k as per_k
from pyti.stochastic import percent_d as per_d
# from pyti.moving_average_convergence_divergence import percent_d as per_d


import Probabilidades.RegrsionLineal2 as regresionlineal2

# Extraemos la Moneda del Nombre del Archivo
fileName = str(os.path.basename(__file__))
fileName = fileName.replace(".py", "")
fileName = fileName.replace("ZRobotV1_SMA_", "")
symbol = fileName.replace("-", "/")
symbolConfig = symbol.replace("/", "_")
config = configparser.ConfigParser()
config.read('RobotV5.ini')
time_frame_operations = config['timeframe']
# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = time_frame_operations['timeframe']

token = time_frame_operations['token']

fast_sma_periods = int(time_frame_operations['fast_sma_periods'])
slow_sma_periods = int(time_frame_operations['slow_sma_periods'])

stoD = int(time_frame_operations['stoD'])
stoK = int(time_frame_operations['stoK'])

amount = int(time_frame_operations['amount'])
stop = int(time_frame_operations['stop'])
limit = int(time_frame_operations['limit'])
trailing_step = int(time_frame_operations['trailing_step'])

macdSlow = 26
macdFast = 12
macdSmooth = 9

macdsub = float(time_frame_operations["macdsub" + symbolConfig])
macduper = float(time_frame_operations["macduper" + symbolConfig])

# Global Variables
pricedata = None
numberofcandles = int(time_frame_operations['numberofcandles'])
operacionventa = False
operacioncompra = False

open_conexion = True
con = None
while open_conexion:
    try:
        print("Opening First Conection")
        con = fxcmpy.fxcmpy(access_token=token, log_level="error", log_file=None)
        print("Conection Stablished")
        open_conexion = False
    except Exception as e:
        print("\n1.An exception occurred Obtaining Conexion: " + symbol + " Exception: " + str(e))
        time.sleep(120)
        open_conexion = True

pricedata_stadistics = pd.DataFrame([],
                                    columns=['index', 'indexdates'
                                                      'x', 'y'
                                                           'bidclose',
                                             'pos',
                                             'y_pred',
                                             'y_pred_print', 'x_pred_print', 'tickqty', 'per_k', 'per_d',
                                             'lower_sto', 'upper_sto',
                                             'macd', 'signal', 'hist', 'macdline0', 'macdoperSale', 'macdoperBuy'
                                             ])

plt.style.use('dark_background')
plt.ion()
plt.show(block=False)

fig = plt.figure()

mng = plt.get_current_fig_manager()
mng.set_window_title(symbol)

ax1 = fig.add_subplot(5, 1, 1)
ax1.clear()

ax2 = fig.add_subplot(5, 1, 2)
ax2.clear()

ax3 = fig.add_subplot(5, 1, 3)
ax3.clear()

ax4 = fig.add_subplot(5, 1, 4)
ax4.clear()

ax5 = fig.add_subplot(5, 1, 5)
ax5.clear()

linePrice, = ax1.plot([], [], label='Precio ' + timeframe + ' ' + symbol)
lineEmaFast, = ax1.plot([], [], label='EMA Fast ' + str(fast_sma_periods))
lineEmaSlow, = ax1.plot([], [], label='EMA Slow ' + str(slow_sma_periods), color='green')
lineRegrbidClose, = ax2.plot([], [], label='Regresion Lineal Precio ' + timeframe, color='silver', linestyle='--')

stoPer_k, = ax4.plot([], [], label='K ' + timeframe, color='green')
stoPer_d, = ax4.plot([], [], label='D ' + timeframe, color='red')
stoLower, = ax4.plot([], [], label='Lower ' + timeframe, color='white', linestyle='--')
stoUpper, = ax4.plot([], [], label='Upper ' + timeframe, color='white', linestyle='--')

macdLine, = ax5.plot([], color='grey', linewidth=1.5, label='MACD')
macdSignal, = ax5.plot([], color='skyblue', linewidth=1.5, label='SIGNAL')
macdline0, = ax5.plot([], color='green', linewidth=1.5)


def UpdatePlotter():
    global pricedata_stadistics
    global pricedata_stadistics_sup

    linePrice.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['bidclose'].values)

    # lineEmaFast.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['emaFast'].values)
    # lineEmaSlow.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['emaSlow'].values)

    lineRegrbidClose.set_data(pricedata_stadistics['x'].values,
                              pricedata_stadistics['y_pred'].values)

    stoPer_k.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['per_k'].values)
    stoPer_d.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['per_d'].values)

    stoLower.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['lower_sto'].values)
    stoUpper.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['upper_sto'].values)

    macdLine.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['macd'].values)
    macdSignal.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['signal'].values)
    macdline0.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['macdline0'].values)

    # for i in range(len(pricedata_stadistics['index'].values)):
    #    if str(pricedata_stadistics.loc[i, 'hist'])[0] == '-':
    #        ax5.bar(pricedata_stadistics.loc[i, 'index'], pricedata_stadistics.loc[i, 'hist'], color='#ef5350')
    #    else:
    #        ax5.bar(pricedata_stadistics.loc[i, 'index'], pricedata_stadistics.loc[i, 'hist'], color='#26a69a')

    ax3.cla()
    ax3.bar(pricedata_stadistics['x'].values, pricedata_stadistics['tickqty'].values, color='blue', label='Volumen ')

    ax1.autoscale_view(True, True, True)
    ax1.legend(loc='best', prop={'size': 7})
    ax1.relim()

    ax2.autoscale_view(True, True, True)
    ax2.legend(loc='best', prop={'size': 7})
    ax2.relim()

    ax4.autoscale_view(True, True, True)
    ax4.legend(loc='best', prop={'size': 7})
    ax4.relim()

    ax5.autoscale_view(True, True, True)
    ax5.legend(loc='best', prop={'size': 7})
    ax5.relim()

    plt.draw()
    plt.pause(0.5)


def Prepare():
    global pricedata, operacionventa, operacioncompra
    print("Solicitando Precios...")
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    print("Precios Iniciales Recibidos")
    if countOpenTrades("B") > 0:
        operacioncompra = True
    if countOpenTrades("S") > 0:
        operacionventa = True
    print("Operacion Compra: " + str(operacioncompra) + " - Operacion Venta " + str(operacionventa))


def StrategyStart():
    Update()
    while True:
        currenttime = dt.datetime.now()
        if timeframe == "m1" and currenttime.second == 0 and getLatestPriceData():
            Update()
            # time.sleep(10)
        elif timeframe == "m5" and currenttime.second == 0 and currenttime.minute % 5 == 0 and getLatestPriceData():
            Update()
            # time.sleep(240)
        elif timeframe == "m15" and currenttime.second == 0 and currenttime.minute % 15 == 0 and getLatestPriceData():
            Update()
            # time.sleep(840)
        elif timeframe == "m30" and currenttime.second == 0 and currenttime.minute % 30 == 0 and getLatestPriceData():
            Update()
            # time.sleep(1740)
            UpdatePlotter()
        elif currenttime.second == 0 and currenttime.minute == 0 and getLatestPriceData():
            Update()
            # time.sleep(3540)
            UpdatePlotter()
        # time.sleep(1)
        UpdatePlotter()
        if currenttime.second == 0:
            print(" - " + str(currenttime))



def getLatestPriceData():
    global pricedata , con
    print("Get Prices")
    open_conexion = True
    while open_conexion:
        try:
            new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
            print("Prices Recived")
            if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != pricedata.index.values[
                len(pricedata.index.values) - 1]:
                pricedata = new_pricedata
                return True
            else:
                print("Prices not Updated")
                time.sleep(120)
                return False
        except Exception as e:
            print("\n1.An exception occurred Obtaining Prices: " + symbol + " Exception: " + str(e))
            time.sleep(120)
            print("\n2.Try Again")
            return False


def Update():
    global pricedata_stadistics, operacionventa, operacioncompra

    print(str(dt.datetime.now()) + " " + timeframe + " Vela Formada - Analizando -  Running Update Function...")

    pricedata_stadistics['index'] = pricedata['bidclose'].index
    pricedata_stadistics['bidclose'] = pricedata['bidclose'].values
    pricedata_stadistics['tickqty'] = pricedata['tickqty'].values

    # Calculate Indicators
    iFastSMA = sma(pricedata['bidclose'], fast_sma_periods)
    iSlowSMA = sma(pricedata['bidclose'], slow_sma_periods)

    pricedata_stadistics['emaFast'] = iFastSMA
    pricedata_stadistics['emaSlow'] = iSlowSMA

    data_per_k = per_k(pricedata['bidclose'], stoK)
    data_per_d = per_d(pricedata['bidclose'], stoD)

    pricedata_stadistics['per_k'] = data_per_k
    pricedata_stadistics['per_d'] = data_per_d

    for index, row in pricedata_stadistics.iterrows():
        pricedata_stadistics.loc[index, 'lower_sto'] = 0.20
        pricedata_stadistics.loc[index, 'upper_sto'] = 0.80
        pricedata_stadistics.loc[index, 'macdline0'] = 0.00

    # ***********************************************************
    # *  Regresion al precio de cierre las velas ================
    # ***********************************************************
    pricedata_stadistics['x'] = np.arange(len(pricedata_stadistics))
    # ************* Calcular la poscion Relativa Y
    for index, row in pricedata_stadistics.iterrows():
        pricedata_stadistics.loc[index, 'y'] = int(
            '{:.5f}'.format((pricedata_stadistics.loc[index, 'bidclose'])).replace('.', ''))

    max_value = max(np.array(pricedata_stadistics['y'].values))
    min_value = min(np.array(pricedata_stadistics['y'].values))
    for index, row in pricedata_stadistics.iterrows():
        value = pricedata_stadistics.loc[index, 'y'] - min_value
        NewPricePosition = ((value * 100) / max_value) * 100
        pricedata_stadistics.loc[index, 'y'] = NewPricePosition

    # ***********  Calcular la poscion Relativa X
    max_value = max(np.array(pricedata_stadistics['x'].values))
    min_value = min(np.array(pricedata_stadistics['x'].values))
    for index, row in pricedata_stadistics.iterrows():
        value = pricedata_stadistics.loc[index, 'x'] - min_value
        NewPricePosition = ((value * 100) / max_value)
        pricedata_stadistics.loc[index, 'x'] = NewPricePosition

    regresionLineal_xx = np.array(pricedata_stadistics['x'].values)
    regresionLineal_yy = np.array(pricedata_stadistics['y'].values)

    regresionLineal_bb = regresionlineal2.estimate_b0_b1(regresionLineal_xx, regresionLineal_yy)
    y_pred_sup = regresionLineal_bb[0] + regresionLineal_bb[1] * regresionLineal_xx
    pricedata_stadistics['y_pred'] = y_pred_sup

    if pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] < \
            pricedata_stadistics.iloc[1]['y_pred'] and \
            pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] < \
            pricedata_stadistics.iloc[1]['y_pred']:
        lv_Tendency = "Bajista"
    elif pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] > \
            pricedata_stadistics.iloc[1]['y_pred'] and \
            pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] > \
            pricedata_stadistics.iloc[1]['y_pred']:
        lv_Tendency = "Alcista"

    # MACD        ########################################################################
    exp1 = pricedata_stadistics['bidclose'].ewm(span=macdFast, adjust=False).mean()
    exp2 = pricedata_stadistics['bidclose'].ewm(span=macdSlow, adjust=False).mean()
    macd = exp1 - exp2
    pricedata_stadistics['macd'] = pricedata_stadistics.index.map(macd)
    pricedata_stadistics['signal'] = pd.DataFrame(
        pricedata_stadistics['macd'].ewm(span=macdSmooth, adjust=False).mean())
    pricedata_stadistics['hist'] = pd.DataFrame(pricedata_stadistics['macd'] - pricedata_stadistics['signal'])

    # Imprimir Precio/Indicador
    print("Precio Cierre: " + str(pricedata['bidclose'][len(pricedata) - 1]))
    print("Tendencia Regresion Lineal: " + lv_Tendency)
    lv_signal = pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['signal']

    if data_per_k[len(data_per_k) - 1] <= 0.20 and crossesOver(data_per_k, data_per_d) and lv_signal <= macdsub:
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
        if countOpenTrades("B") == 0:
            enter("B")
            operacioncompra = True

    # Verifica el Cruce del SMA para Abajo.
    # if crossesUnder(data_per_d, 0.80):
    # if crossesUnder(pricedata_stadistics['signal'], 0.0004):
    if data_per_k[len(data_per_k) - 1] >= 0.80 and crossesUnder(data_per_k, data_per_d) and lv_signal >= macduper:
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
        if countOpenTrades("S") == 0:
            enter("S")
            operacionventa = True

    # Cerrar Ventas #########################################
    if operacionventa and data_per_k[len(data_per_k) - 1] <= 0.20 and crossesOver(data_per_k, data_per_d):
        if countOpenTrades("S") > 0:
            print("	  Cerrando Ventas Abiertas...\n")
            operacionventa = False
            exit("S")

    # Cerrar Compras #########################################
    if operacioncompra and data_per_k[len(data_per_k) - 1] >= 0.80 and crossesUnder(data_per_k, data_per_d):
        if countOpenTrades("B") > 0:
            print("	  Cerrando Ventas Abiertas...\n")
            operacioncompra = False
            exit("B")

    print(str(dt.datetime.now()) + " " + timeframe + " Verificacion Realizada.\n")


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


# Esta Funcion realiza una orden en la direccion indicada     BuySell, "B" = Buy, "S" = Sell, usa las variables usas symbol, amount, stop, limit
def enter(BuySell):
    direction = True
    if BuySell == "S":
        direction = False
    try:
        # opentrade = con.open_trade(symbol=symbol, is_buy=direction,amount=amount, time_in_force='GTC',order_type='AtMarket',is_in_pips=True,limit=limit, stop=stop, trailing_step=1)
        opentrade = con.open_trade(symbol=symbol,
                                   is_buy=direction,
                                   amount=amount,
                                   time_in_force='GTC',
                                   order_type='AtMarket',
                                   is_in_pips=True,
                                   limit=limit,
                                   stop=stop, trailing_step=trailing_step)

    except:
        print("	  Error Abriendo la Operacion.")
    else:
        print("	  Operacion Abierta Exitosamente.")


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


Prepare()  # Perar la Estrategia
StrategyStart()  # Iniciar la Estrategia
