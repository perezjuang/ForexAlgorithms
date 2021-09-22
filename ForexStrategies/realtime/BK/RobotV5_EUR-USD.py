import fxcmpy
import time
import datetime as dt
from pyti.simple_moving_average import simple_moving_average as sma
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import Probabilidades.RegrsionLineal2 as regresionlineal2
import math
import os
import configparser

# Extraemos la Moneda del Nombre del Archivo
fileName = str(os.path.basename(__file__))
fileName = fileName.replace(".py", "")
fileName = fileName.replace("RobotV5_", "")
symbol = fileName.replace("-", "/")
# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
config = configparser.ConfigParser()
config.read('RobotV5.ini')

time_frame_operations = config['timeframe']
# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = time_frame_operations['timeframe']

timeframe_sup = time_frame_operations['timeframe_sup']

fast_sma_periods = int(time_frame_operations['fast_sma_periods'])

slow_sma_periods = int(time_frame_operations['slow_sma_periods'])

slow_sma_periods2 = int(time_frame_operations['slow_sma_periods2'])

token = time_frame_operations['token']

amount = int(time_frame_operations['amount'])
stop = int(time_frame_operations['stop'])
limit = int(time_frame_operations['limit'])
trailing_step = int(time_frame_operations['trailing_step'])

# Global Variables
pricedata = None
pricedata_sup = None
pricedata_stadistics = pd.DataFrame([],
                                    columns=['index',
                                             'x', 'y'
                                                  'bidclose',
                                             'pos',
                                             'y_pred',
                                             'y_pred_print', 'x_pred_print'
                                             ])

pricedata_stadistics_sup = pd.DataFrame([],
                                        columns=['index',
                                                 'bidclose',
                                                 ])

numberofcandles = int(time_frame_operations['numberofcandles'])

numberofcandles_sup = int(time_frame_operations['numberofcandles_sup'])

con = fxcmpy.fxcmpy(access_token=token, log_level="error", log_file=None)

plt.style.use('dark_background')
plt.ion()
plt.show(block=False)

fig = plt.figure()

mng = plt.get_current_fig_manager()
mng.set_window_title(symbol)

ax1 = fig.add_subplot(2, 1, 1)
ax1.clear()

ax2 = fig.add_subplot(2, 1, 2)
ax2.clear()

linePrice, = ax1.plot([], [], label='Precio ' + timeframe + ' ' + symbol)
lineEmaFast, = ax1.plot([], [], label='EMA Fast ' + str(fast_sma_periods))
lineEmaSlow, = ax1.plot([], [], label='EMA Slow ' + str(slow_sma_periods), color='green')
lineEmaSlow2, = ax1.plot([], [], label='EMA Slow 2:  ' + str(slow_sma_periods2), color='red')

lineRegrbidClose, = ax2.plot([], [], label='Regresion Lineal Precio ' + timeframe, color='silver', linestyle='--')


def UpdatePlotter():
    global pricedata_stadistics
    global pricedata_stadistics_sup

    linePrice.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['bidclose'].values)

    lineEmaFast.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['emaFast'].values)
    lineEmaSlow.set_data(pricedata_stadistics['index'].values, pricedata_stadistics['emaSlow'].values)


    lineEmaSlow2.set_data(pricedata_stadistics_sup['index'].values, pricedata_stadistics_sup['emaSlow2'].values)

    lineRegrbidClose.set_data(pricedata_stadistics['x'].values,
                              pricedata_stadistics['y_pred'].values)

    ax1.autoscale_view(True, True, True)
    ax1.legend(loc='best', prop={'size': 7})
    ax1.relim()

    ax2.autoscale_view(True, True, True)
    ax2.legend(loc='best', prop={'size': 7})
    ax2.relim()

    plt.draw()
    plt.pause(0.5)


def Prepare():
    global pricedata
    global pricedata_sup
    print("Requesting Initial Price Data...")
    pricedata_sup = con.get_candles(symbol, period=timeframe_sup, number=numberofcandles_sup)
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    print(pricedata)
    print("=========================================================================")
    print(pricedata_sup)
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
        # time.sleep(1)
        UpdatePlotter()


def getLatestPriceData():
    global pricedata
    global pricedata_sup

    open_conexion = True
    while open_conexion:
        currenttime = dt.datetime.now()
        try:
            if currenttime.second == 0 and currenttime.minute == 0:
                new_pricedata_sup = con.get_candles(symbol, period=timeframe_sup, number=numberofcandles_sup)

                if new_pricedata_sup.index.values[len(new_pricedata_sup.index.values) - 1] != \
                        pricedata_sup.index.values[
                            len(pricedata_sup.index.values) - 1]:
                    pricedata_sup = new_pricedata_sup
            else:
                new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
                if len(new_pricedata) > 0:
                    if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != pricedata.index.values[
                        len(pricedata.index.values) - 1]:
                        pricedata = new_pricedata

            return True
        except Exception as e:
            print("\n1.An exception occurred Obtaining Prices: " + symbol + " Exception " + str(e))
            open_conexion = True
            time.sleep(120)


def Update():
    global pricedata_stadistics
    global pricedata_stadistics_sup

    print(str(dt.datetime.now()) + " " + timeframe + " Bar Closed - Running Update Function...")

    pricedata_stadistics['index'] = pricedata['bidclose'].index
    pricedata_stadistics['bidclose'] = pricedata['bidclose'].values

    pricedata_stadistics_sup['index'] = pricedata_sup['bidclose'].index
    pricedata_stadistics_sup['bidclose'] = pricedata_sup['bidclose'].values

    # Calculate Indicators
    iFastSMA = sma(pricedata['bidopen'], fast_sma_periods)
    iSlowSMA = sma(pricedata['bidclose'], slow_sma_periods)

    iSlowSMA2 = sma(pricedata_sup['bidopen'], slow_sma_periods2)

    pricedata_stadistics['emaFast'] = iFastSMA
    pricedata_stadistics['emaSlow'] = iSlowSMA


    pricedata_stadistics_sup['emaSlow2'] = iSlowSMA2

    # Print Price/Indicators
    print("Close Price: " + str(pricedata_stadistics['bidclose'][len(pricedata) - 1]))
    #print("Fast SMA: " + str(iFastSMA[len(iFastSMA) - 1]))
    #print("Slow SMA Open SUP: " + str(iSlowSMA[len(iSlowSMA) - 1]))
    #print("Slow SMA Open SUP2: " + str(iSlowSMA2[len(iSlowSMA2) - 1]))


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

    # Recreacion del Eje X para Presentacion de la Regresion.
    # for index, row in pricedata_stadistics.iterrows():
    #    pricedata_stadistics.loc[index, 'x_pred'] = pricedata_stadistics.loc[0, 'y_pred']

    # Calculo de Angulo
    vx = np.array(pricedata_stadistics['x'])
    vy = np.array(pricedata_stadistics['y_pred'])

    x1 = vx[0]
    y1 = vy[0]

    x2 = vx[-1]
    y2 = vy[-1]

    x = x2 - x1
    y = y2 - y1

    angle = math.atan2(y, x) * (180.0 / math.pi)
    angle = round(angle, 2)
    # angle2 = np.rad2deg(np.arctan2(vy[-1] - vy[0], vx[-1] - vx[0]))

    print("\nAngulo: " + str(angle))

    lv_Tendency = "Lateral"
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
    print("\nTendencia Regresion Lineal: " + lv_Tendency)

    # # TRADING LOGIC
    #

    # print("Slow SMA Open SUP: " + str(iSlowSMA[len(iSlowSMA) - 1]))
    # print("Slow SMA Open SUP2: " + str(iSlowSMA2[len(iSlowSMA2) - 1]))

    message_text = ""
    #if crossesOver(iFastSMA, iSlowSMA) and angle >= 1:
    if crossesOver(iFastSMA, iSlowSMA):# and lv_Tendency == "Alcista":
        message_text = message_text + "\n	  BUY SIGNAL!"
        if countOpenTrades("S") > 0:
            message_text = message_text + "\n	  Closing Sell Trade(s)..."
            exit("S")
        if countOpenTrades("B") == 0:
            message_text = message_text + "\n	  Opening Buy Trade..."
            enter("B")

    #if crossesUnder(iFastSMA, iSlowSMA) and angle <= -1:
    if crossesUnder(iFastSMA, iSlowSMA):# and lv_Tendency == "Bajista":
        message_text = message_text + "\n	  SELL SIGNAL!"
        if countOpenTrades("B") > 0:
            message_text = message_text + "\n	  Closing Buy Trade(s)..."
            exit("B")
        if countOpenTrades("S") == 0:
            message_text = message_text + "\n	  Opening Sell Trade..."
            enter("S")

    print(message_text)
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
    direction = True
    if BuySell == "S":
        direction = False
    try:
        opentrade = con.open_trade(symbol=symbol, is_buy=direction, amount=amount, time_in_force='GTC',
                                   order_type='AtMarket', is_in_pips=True, limit=limit, stop=stop,
                                   trailing_step=trailing_step)
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


Prepare()  # Initialize strategy
StrategyHeartBeat()  # Run strategy
