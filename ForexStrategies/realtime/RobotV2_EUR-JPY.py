import os

import fxcmpy
import time
import datetime as dt
from pyti.simple_moving_average import simple_moving_average as sma
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import Probabilidades.RegrsionLineal2 as regresionlineal2


# Extraemos la Moneda del Nombre del Archivo
fileName = str(os.path.basename(__file__))
fileName = fileName.replace(".py", "")
fileName = fileName.replace("RobotV2_", "")
symbol = fileName.replace("-", "/")

# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = "m5"
timeframe_sup = "H4"
fast_sma_periods = 10
slow_sma_periods = 30

amount = 3
stop = -10
limit = 30

# Global Variables
pricedata = None
pricedata_sup = None

numberofcandles = 300

numberofcandles_sup = 6
numberofregresion_sup = 6

con = fxcmpy.fxcmpy(config_file='../fxcm.cfg')

pricedata_stadistics = pd.DataFrame([],
                                    columns=['rowid',
                                             'bidclose',
                                             'emaFast',
                                             'emaSlow',
                                             'signal',
                                             'position',
                                             'y_pred',
                                             'y_pred_inf',
                                             'date'
                                             ])

pricedata_stadistics_sup = pd.DataFrame([],
                                        columns=['rowid',
                                                 'bidclose', 'bidhigh', 'bidlow',
                                                 'y_pred_sup', 'y_pred_bidhigh', 'y_pred_bidlow', 'date'
                                                 ])



pricedata_stadistics_proyeccion = pd.DataFrame([],
                                               columns=['rowid',
                                                        'y_pred_sup', 'y_pred_bidhigh', 'y_pred_bidlow', 'date'
                                                        ])





plt.style.use('dark_background')
plt.ion()  # Enable interactive mode
plt.show(block=False)
fig = plt.figure()

ax1 = fig.add_subplot(1, 1, 1)
ax1.clear()
ax1.set_autoscale_on(True)

linePrice, = ax1.plot([], [], label='Precio ' + timeframe + ' ' + symbol)
lineEmaFast, = ax1.plot([], [], label='EMA Fast ' + str(fast_sma_periods))
lineEmaSlow, = ax1.plot([], [], label='EMA Slow ' + str(slow_sma_periods))
lineRegrbidhigh, = ax1.plot([], [], label='Regresion Lineal bidhigh ' + timeframe_sup)
lineRegrbidlow, = ax1.plot([], [], label='Regresion Lineal bidlow ' + timeframe_sup)
lineRegrbidhigh_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal bidhigh ' + timeframe_sup)
lineRegrbidlow_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal bidlow ' + timeframe_sup)


def UpdatePlotter():
    global pricedata
    global pricedata_stadistics
    linePrice.set_data(pricedata['bidclose'].index, pricedata['bidclose'].values)
    lineEmaFast.set_data(pricedata_stadistics['emaFast'].index, pricedata_stadistics['emaFast'].values)
    lineEmaSlow.set_data(pricedata_stadistics['emaSlow'].index, pricedata_stadistics['emaSlow'].values)

    lineRegrbidhigh.set_data(pricedata_stadistics_sup['y_pred_bidhigh'].index,
                             pricedata_stadistics_sup['y_pred_bidhigh'].values)

    lineRegrbidlow.set_data(pricedata_stadistics_sup['y_pred_bidlow'].index,
                            pricedata_stadistics_sup['y_pred_bidlow'].values)

    lineRegrbidlow_proyeccion.set_data(pricedata_stadistics_proyeccion['y_pred_bidlow'].index,
                                       pricedata_stadistics_proyeccion['y_pred_bidlow'].values)

    lineRegrbidhigh_proyeccion.set_data(pricedata_stadistics_proyeccion['y_pred_bidhigh'].index,
                                        pricedata_stadistics_proyeccion['y_pred_bidhigh'].values)

    ax1.legend(loc='best', prop={'size': 7})
    ax1.relim()
    # ax1.autoscale_view(True, True, True)
    plt.draw()
    plt.pause(1)


def Prepare():
    global pricedata
    global pricedata_sup
    print("Requesting Initial Price Data...")
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    pricedata_sup = con.get_candles(symbol, period=timeframe_sup, number=numberofcandles_sup)
    print("Initial Price Data Received...")


def StrategyHeartBeat():
    Update()
    while True:
        currenttime = dt.datetime.now()
        if timeframe == "m1" and currenttime.second == 0:
            if getLatestPriceData():
                Update()
        elif timeframe == "m5" and currenttime.second == 0 and currenttime.minute % 5 == 0:
            if getLatestPriceData():
                Update()
            time.sleep(240)
        elif timeframe == "m15" and currenttime.second == 0 and currenttime.minute % 15 == 0:
            if getLatestPriceData():
                Update()
            time.sleep(840)
        elif timeframe == "m30" and currenttime.second == 0 and currenttime.minute % 30 == 0:
            if getLatestPriceData():
                Update()
            time.sleep(1740)
        elif currenttime.second == 0 and currenttime.minute == 0:
            if getLatestPriceData():
                Update()
            time.sleep(3540)
        UpdatePlotter()


def getLatestPriceData():
    try:
        global pricedata
        global pricedata_sup
        currenttime = dt.datetime.now()
        if currenttime.second == 0 and currenttime.minute == 0:
            pricedata_sup = con.get_candles(symbol, period=timeframe_sup, number=numberofcandles_sup)
            return True
        else:
            new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
        if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != pricedata.index.values[
            len(pricedata.index.values) - 1]:
            pricedata = new_pricedata
            return True
        else:
            print("No updated prices found, trying again in 10 seconds...")
            pricedata = new_pricedata
            return True
    except:
        print("An exception occurred Obtaining Prices")
        return False


def Update():
    print(str(dt.datetime.now()) + " " + timeframe + " Bar Closed - Running Update Function...")
    # *********************************************************************
    # ** Estadistica General - Regresion Lineal Simple 1
    # *********************************************************************

    pricedata_stadistics['bidclose'] = pricedata['bidclose'].values
    pricedata_stadistics['bidopen'] = pricedata['bidopen'].values
    pricedata_stadistics['date'] = pricedata['bidclose'].index
    # pricedata_stadistics['emaFast'] = pricedata_stadistics['bidclose'].rolling(window=fast_sma_periods).mean()
    # pricedata_stadistics['emaSlow'] = pricedata_stadistics['bidclose'].rolling(window=slow_sma_periods).mean()
    # Calculate Indicators
    iFastSMA = sma(pricedata['bidclose'], fast_sma_periods)
    iSlowSMA = sma(pricedata['bidclose'], slow_sma_periods)
    pricedata_stadistics['emaFast'] = iFastSMA
    pricedata_stadistics['emaSlow'] = iSlowSMA
    pricedata_stadistics.index = pricedata['bidclose'].index
    pricedata_stadistics['rowid'] = np.arange(len(pricedata_stadistics))


    # *********************************************************************
    # ** Estadistica General - Regresion Lineal
    # *********************************************************************
    pricedata_stadistics_sup['bidclose'] = pricedata_sup['bidclose'].values
    pricedata_stadistics_sup['date'] = pricedata_sup['bidclose'].index
    pricedata_stadistics_sup['bidhigh'] = pricedata_sup['bidhigh'].values
    pricedata_stadistics_sup['bidlow'] = pricedata_sup['bidlow'].values
    pricedata_stadistics_sup.index = pricedata_sup['bidclose'].index
    pricedata_stadistics_sup['rowid'] = np.arange(len(pricedata_stadistics_sup))

    # Regresion al mas Alto de las velas ======================
    regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['rowid'].tail(numberofregresion_sup).values)
    regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['bidhigh'].tail(numberofregresion_sup).values)
    regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup

    numberRegx = len(pricedata_stadistics_sup) - numberofregresion_sup
    posreg = 0
    for index, row in pricedata_stadistics_sup.iterrows():
        if numberRegx <= pricedata_stadistics_sup.loc[index, 'rowid']:
            pricedata_stadistics_sup.loc[index, 'y_pred_bidhigh'] = y_pred_sup[posreg]
            posreg = posreg + 1

    # Regresion al mas bajo de las velas ======================
    regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['rowid'].tail(numberofregresion_sup).values)
    regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['bidlow'].tail(numberofregresion_sup).values)
    regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup

    numberRegx = len(pricedata_stadistics_sup) - numberofregresion_sup
    posreg = 0
    for index, row in pricedata_stadistics_sup.iterrows():
        if numberRegx <= pricedata_stadistics_sup.loc[index, 'rowid']:
            pricedata_stadistics_sup.loc[index, 'y_pred_bidlow'] = y_pred_sup[posreg]
            posreg = posreg + 1

    # *********************************************************************
    # ***    Proyecion de Precios * Se puede Mejorar con Ciclo
    # *********************************************************************
    lv_index_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['date']
    lv_rowid_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['rowid']
    lv_y_pred_askhigh_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidhigh']
    lv_y_pred_asklow_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidlow']

    lv_index_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['date']
    lv_rowid_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['rowid']
    lv_y_pred_askhigh_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['y_pred_bidhigh']
    lv_y_pred_asklow_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['y_pred_bidlow']

    lv_index_base = lv_index_1 - lv_index_2
    lv_rowid_base = lv_rowid_1 - lv_rowid_2
    lv_y_pred_askhigh_base = lv_y_pred_askhigh_1 - lv_y_pred_askhigh_2
    lv_y_pred_asklow_base = lv_y_pred_asklow_1 - lv_y_pred_asklow_2

    pricedata_stadistics_proyeccion.loc[lv_index_1] = pd.Series(
        {'rowid': lv_rowid_1,
         'y_pred_bidhigh': lv_y_pred_askhigh_1,
         'y_pred_bidlow': lv_y_pred_asklow_1
         })

    pricedata_stadistics_proyeccion.loc[lv_index_1 + lv_index_base] = pd.Series(
        {'rowid': lv_rowid_1 + lv_rowid_base,
         'y_pred_bidhigh': lv_y_pred_askhigh_1 + lv_y_pred_askhigh_base,
         'y_pred_bidlow': lv_y_pred_asklow_1 + lv_y_pred_asklow_base
         })


    # Calculamos La tendencia con los valores de de la proyection las velas mas altas y mas bajas.
    lv_Tendency = "Lateral"
    if pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidhigh'] < \
            pricedata_stadistics_sup.iloc[1]['y_pred_bidhigh'] and \
            pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidlow'] < \
            pricedata_stadistics_sup.iloc[1]['y_pred_bidlow']:
        lv_Tendency = "Bajista"
    elif pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidhigh'] > \
            pricedata_stadistics_sup.iloc[1]['y_pred_bidhigh'] and \
            pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidlow'] > \
            pricedata_stadistics_sup.iloc[1]['y_pred_bidlow']:
        lv_Tendency = "Alcista"

    print("Tendencia Regresion Lineal: " + lv_Tendency)

    lv_posicion_venta = False
    lv_posicion_compra = False

    if lv_Tendency == "Bajista" and pricedata_stadistics_proyeccion.iloc[len(pricedata_stadistics_proyeccion) - 1][
        'y_pred_bidhigh'] < pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['bidclose']:
        lv_posicion_venta = True
        lv_posicion_compra = False
    elif lv_Tendency == "Alcista" and pricedata_stadistics_proyeccion.iloc[len(pricedata_stadistics_proyeccion) - 1][
        'y_pred_bidlow'] > pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['bidclose']:
        lv_posicion_venta = False
        lv_posicion_compra = True
    print("Posicion de Venta: " + str(lv_posicion_venta) + " Posicion de Compra: " + str(lv_posicion_compra))

    # Print Price/Indicators
    print("Close Price: " + str(pricedata['bidclose'][len(pricedata) - 1]))
    #print("Fast SMA: " + str(iFastSMA[len(iFastSMA) - 1]))
    #print("Slow SMA: " + str(iSlowSMA[len(iSlowSMA) - 1]))

    # TRADING LOGIC
    if crossesOver(iFastSMA, iSlowSMA) and lv_posicion_compra :
        print("	  BUY SIGNAL!")
        if countOpenTrades("S") > 0:
            print("	  Closing Sell Trade(s)...")
            exit("S")
        print("	  Opening Buy Trade...")
        enter("B")

    if crossesUnder(iFastSMA, iSlowSMA) and lv_posicion_venta:
        print("	  SELL SIGNAL!")
        if countOpenTrades("B") > 0:
            print("	  Closing Buy Trade(s)...")
            exit("B")
        print("	  Opening Sell Trade...")
        enter("S")
    print(str(dt.datetime.now()) + " " + timeframe + " Update Function Completed.\n")
    print("\n")


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


# Returns true if stream1 crossed under stream2 in most recent candle, stream2 can be integer/float or data array

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


def enter(BuySell):
    direction = True;
    if BuySell == "S":
        direction = False;
    try:
        opentrade = con.open_trade(symbol=symbol, is_buy=direction, amount=amount, time_in_force='GTC',
                                   order_type='AtMarket', is_in_pips=True, limit=limit, stop=stop)
    except:
        print("	  Error Opening Trade.")
    else:
        print("	  Trade Opened Successfully.")


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


if __name__ == '__main__':
    Prepare()  # Initialize strategy
    StrategyHeartBeat()  # Run strategy
