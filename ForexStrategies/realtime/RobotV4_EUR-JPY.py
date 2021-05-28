import configparser
import os

import fxcmpy
import time
import datetime as dt
# from pyti.exponential_moving_average import exponential_moving_average as ema
from pyti.simple_moving_average import simple_moving_average as sma
from pyti.relative_strength_index import relative_strength_index as rsi
from pyti.stochastic import percent_k as per_k
from pyti.stochastic import percent_d as per_d

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import Probabilidades.RegrsionLineal2 as regresionlineal2
import math

# Extraemos la Moneda del Nombre del Archivo
fileName = str(os.path.basename(__file__))
fileName = fileName.replace(".py", "")
fileName = fileName.replace("RobotV4_", "")
symbol = fileName.replace("-", "/")

config = configparser.ConfigParser()
config.read('RobotV4.ini')

time_frame_operations = config['timeframe']
# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = time_frame_operations['timeframe']

fast_sma_periods = int(time_frame_operations['fast_sma_periods'])
slow_sma_periods = int(time_frame_operations['slow_sma_periods'])

# too_slow_sma_periods = int(time_frame_operations['too_slow_sma_periods'])

amount = int(time_frame_operations['amount'])
stop = int(time_frame_operations['stop'])
limit = int(time_frame_operations['limit'])
trailing_step = int(time_frame_operations['trailing_step'])

angulo_plus = int(time_frame_operations['angulo_plus'])
angulo_minus = int(time_frame_operations['angulo_minus'])

rsi_periods = int(time_frame_operations['rsi_periods'])

upper_rsi = float(time_frame_operations['upper_rsi'])
lower_rsi = float(time_frame_operations['lower_rsi'])

data_per_k = int(time_frame_operations['data_per_k'])
data_per_d = int(time_frame_operations['data_per_d'])

upper_sto = float(time_frame_operations['upper_sto'])
lower_sto = float(time_frame_operations['lower_sto'])

# Global Variables
pricedata = None

numberofcandles = int(time_frame_operations['numberofcandles'])

message_text = ''
con = None
open_conexion = True

while open_conexion:
    try:
        print("Iniciando Conexion " + symbol)
        con = fxcmpy.fxcmpy(config_file='../fxcm.cfg', server='demo')

        print("Conexion Iniciada " + symbol)
        open_conexion = False
    except Exception as e:  # work on python 3.x
        open_conexion = True
        print("Reiniciando Conexion " + symbol + " ex: " + str(e))
        time.sleep(30)

pricedata_stadistics = pd.DataFrame([],
                                    columns=['x',
                                             'y',
                                             'y_pred'
                                             'x_pred'
                                             'y_bidhigh',
                                             'y_bidlow',
                                             'bidclose',
                                             'bidhigh',
                                             'bidlow',
                                             'y_pred_bidhigh',
                                             'y_pred_bidlow',
                                             'RSI',
                                             'RSI_middle',
                                             'upper_rsi',
                                             'lower_rsi',
                                             'istodata_per_k',
                                             'istodata_per_d',
                                             'upper_sto',
                                             'lower_sto',
                                             ])

plt.style.use('dark_background')
plt.ion()
plt.show(block=False)

fig = plt.figure()
# fig.suptitle('Different types of oscillations', fontsize=16)
mng = plt.get_current_fig_manager()
# mng.window.showMaximized()
# mng.window.SetSize((100, 100))
mng.set_window_title(symbol)

ax1 = fig.add_subplot(2, 1, 1)
ax1.clear()
# ax1.axis("equal")

# ax1.set_autoscale_on(True)

ax2 = fig.add_subplot(2, 1, 2)
ax2.clear()
#ax2.axis("equal")

# ax3 = fig.add_subplot(2, 1, 2)
# ax3.clear()
# ax3.axis("equal")

linePrice, = ax1.plot([], [], label='Precio ' + timeframe + ' ' + symbol)
lineRegrbidClose, = ax1.plot([], [], label='Regresion Lineal Precio ' + timeframe, color='silver', linestyle='--')
lineRegrbidCloseX, = ax1.plot([], [], label='Regresion Lineal Precio ' + timeframe, color='silver', linestyle='--')

# lineRegrbidhigh, = ax1.plot([], [], label='Regresion Lineal bidhigh ' + timeframe)
# lineRegrbidlow, = ax1.plot([], [], label='Regresion Lineal bidlow ' + timeframe)


lineEmaFast, = ax1.plot([], [], label='EMA Fast ' + str(fast_sma_periods))
lineEmaSlow, = ax1.plot([], [], label='EMA Slow ' + str(slow_sma_periods))
# lineEmaTooSlow, = ax1.plot([], [], label='EMA Too Slow ' + str(too_slow_sma_periods))

lineRSI, = ax2.plot([], [], label='RSI', color='red')
lupper_rsi, = ax2.plot([], [], label='UpperRSI', color='pink')
llower_rsi, = ax2.plot([], [], label='LowerRSI', color='pink')
RSI_middle, = ax2.plot([], [], label='RSI_middle', color='pink')

# lineSTOK, = ax3.plot([], [], label='K', color='green')
# lineSTOD, = ax3.plot([], [], label='D', color='red')
# lupper_sto, = ax3.plot([], [], label='UpperSTO', color='pink')
# llower_sto, = ax3.plot([], [], label='LowerSTO', color='pink')

# lineRegrbidhigh, = ax1.plot([], [], label='Regresion Lineal bidhigh ' + timeframe_sup)
# lineRegrbidlow, = ax1.plot([], [], label='Regresion Lineal bidlow ' + timeframe_sup)

# lineRegrbidhigh_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal bidhigh ' + timeframe_sup)
# lineRegrbidlow_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal bidlow ' + timeframe_sup)

# lineRegrbidhigh_proyeccion_tend, = ax1.plot([], [], label='Proyeccion Lineal bidhigh tend ' + timeframe_sup)
# lineRegrbidlow_proyeccion_tend, = ax1.plot([], [], label='Proyeccion Lineal bidlow tend' + timeframe_sup)

# lineATriangulo, = ax1.plot([], [], label='LineaA', color='silver', linestyle='--')
# lineBTriangulo, = ax1.plot([], [], label='LineaB', color='silver', linestyle='--')

lrsiPosCompra = False
lrsiPosVenta = False

lstoPosBuy = False
lstoPosSell = False

lregrPosSell = False
lregrPosBuy = False


def UpdatePlotter():
    global pricedata
    global pricedata_stadistics
    linePrice.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['y'].values)
    lineRegrbidClose.set_data(pricedata_stadistics['x'].values,
                              pricedata_stadistics['y_pred'].values)

    # lineRegrbidhigh.set_data(pricedata_stadistics['x'].values,
    #                         pricedata_stadistics['y_pred_bidhigh'].values)

    # lineRegrbidlow.set_data(pricedata_stadistics['x'].values,
    #                        pricedata_stadistics['y_pred_bidlow'].values)

    lineRegrbidCloseX.set_data(pricedata_stadistics['x'].values,
                               pricedata_stadistics['x_pred'].values)

    lineEmaFast.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['emaFast'].values)
    lineEmaSlow.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['emaSlow'].values)
    # lineEmaTooSlow.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['emaTooSlow'].values)

    #
    lineRSI.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['RSI'].values)
    lupper_rsi.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['upper_rsi'].values)
    llower_rsi.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['lower_rsi'].values)
    RSI_middle.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['RSI_middle'].values)

    # lineSTOK.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['istodata_per_k'].values)
    # lineSTOD.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['istodata_per_d'].values)
    # lupper_sto.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['upper_sto'].values)
    # llower_sto.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['lower_sto'].values)

    # lineRegrbidhigh.set_data(pricedata_stadistics_sup['x_pred_bidhigh'].values,
    #                         pricedata_stadistics_sup['y_pred_bidhigh'].values)

    # lineRegrbidlow.set_data(pricedata_stadistics_sup['x_pred_bidlow'].values,
    #                        pricedata_stadistics_sup['y_pred_bidlow'].values)

    # lineRegrbidlow_proyeccion.set_data(pricedata_stadistics_proyeccion['date_timestamp'].values,
    #                                   pricedata_stadistics_proyeccion['y_pred_bidlow'].values)
    # lineRegrbidhigh_proyeccion.set_data(pricedata_stadistics_proyeccion['date_timestamp'].values,
    #                                    pricedata_stadistics_proyeccion['y_pred_bidhigh'].values)
    # lineRegrbidlow_proyeccion_tend.set_data(pricedata_stadistics_proyeccion_tenden['date_timestamp'].values,
    #                                        pricedata_stadistics_proyeccion_tenden['y_pred_bidlow'].values)
    # lineRegrbidhigh_proyeccion_tend.set_data(pricedata_stadistics_proyeccion_tenden['date_timestamp'].values,
    #                                         pricedata_stadistics_proyeccion_tenden['y_pred_bidhigh'].values)

    # lineATriangulo.set_data(pricedata_stadistics_sup['date_timestamp'].values, pricedata_stadistics_sup['y_pred_bidhigh'].values)
    # lineBTriangulo.set_data(pricedata_stadistics_sup['date_timestamp'].values, pricedata_stadistics_sup['y_pred_LineXHigh'].values)

    # vx = np.array(pricedata_stadistics_sup['rowid'].index)
    # vy = np.array(pricedata_stadistics_sup['y_pred_bidhigh'].values)
    # plt.plot(vx, vy, color='green', linestyle='--')
    #
    # vxLine = vx
    # vyLine = []
    # for ly in vy:
    #     vyLine.append(vy[0])
    #
    # plt.plot(vxLine, vyLine, color='green', linestyle='--')
    # plt.xlabel('x')
    # plt.ylabel('y')
    #
    # x1 = vx[0]
    # y1 = vy[0]
    #
    # x2 = vx[-1]
    # y2 = vy[-1]
    #
    # x = x2 - x1
    # y = y2 - y1
    #
    # angle = math.atan2(y, x) * (180.0 / math.pi)
    # print(angle)
    #
    #
    # # create circle
    # c = plt.Circle((x1, y1), radius=10, color='red', alpha=.3)
    # plt.gca().add_artist(c)
    #
    # #plt.text(x1, y1, str(round(angle, 2)) + ' °')

    ax1.legend(loc='best', prop={'size': 7})
    ax1.relim()

    ax2.legend(loc='best', prop={'size': 7})
    ax2.relim()
    # ax1.autoscale_view(True, True, True)
    # ax3.legend(loc='best', prop={'size': 7})
    # ax3.relim()

    plt.draw()

    plt.pause(0.5)


def Prepare():
    global pricedata
    global message_text
    global con

    open_conexion = True
    while open_conexion:
        message_text = message_text + ("\nRequesting Initial Price Data..." + symbol)
        try:
            print("Recibiendo Precios Iniciales " + symbol)
            pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
            print("Precios Iniciales Recibidos " + symbol)
            open_conexion = False
        except Exception as e:  # work on python 3.x
            open_conexion = True
            print(str(e))
            print("Precios no recibidos.... Reintento en 10 Segundos " + symbol)
            time.sleep(60)

    message_text = message_text + "\nInitial Price Data Received..." + symbol


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
    global message_text
    message_text = message_text + "\nUpdating prices " + symbol + "=========================================="
    global pricedata
    try:
        new_pricedata = None
        open_conexion = True
        while open_conexion:
            try:
                new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
                open_conexion = False
            except Exception as e:
                message_text = message_text + (
                        "\n1.An exception occurred Obtaining Prices: " + symbol + " Exception " + str(e))
                open_conexion = True
                time.sleep(5)

        if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != pricedata.index.values[
            len(pricedata.index.values) - 1]:
            pricedata = new_pricedata
            return True
        else:
            message_text = message_text + ("\nNo updated prices found, trying again in 10 seconds..." + symbol)
            pricedata = new_pricedata
            return True
    except Exception as e:
        message_text = message_text + (
                "\n2.An exception occurred Obtaining Prices: " + symbol + " Exception " + str(e))
        return False


def Update():
    global message_text

    global lrsiPosCompra
    global lrsiPosVenta

    global lstoPosSell
    global lstoPosBuy

    global lregrPosSell
    global lregrPosBuy
    message_text = message_text + (
            '\n' + str(dt.datetime.now()) + " " + timeframe + " Bar Closed - Running Update Function..." + symbol)
    # *****************************************************************************
    # # Calcular Indicador RSI

    iRSI = rsi(pricedata['bidclose'], rsi_periods)
    message_text = message_text + '\n' + "RSI: " + str(iRSI[len(iRSI) - 1])
    pricedata_stadistics['RSI'] = iRSI

    # # Lbuy_operacion_rsi = crossesOver(lower_rsi, iRSI)
    # # Lsell_operacion_rsi = crossesUnder(upper_rsi, iRSI)
    Lbuy_operacion_rsi = False
    Lsell_operacion_rsi = False

    if iRSI[len(iRSI) - 1] < lower_rsi:
         message_text = message_text + '\n' + "RSI Posicion Compra: "
         lrsiPosCompra = True
    if lrsiPosCompra:
        if iRSI[len(iRSI) - 1] > lower_rsi:
           lrsiPosCompra = False
           Lbuy_operacion_rsi = True

    if iRSI[len(iRSI) - 1] > upper_rsi:
         message_text = message_text + '\n' + "RSI Posicion Venta: "
         lrsiPosVenta = True
    if lrsiPosVenta:
         if iRSI[len(iRSI) - 1] < upper_rsi:
             lrsiPosVenta = False
             Lsell_operacion_rsi = True

    message_text = message_text + "\n rsi buy:" + str(Lbuy_operacion_rsi)
    message_text = message_text + "\n rsi sell:" + str(Lsell_operacion_rsi)

    # Calcular STO
    # istodata_per_k = per_k(pricedata['bidclose'], data_per_k)
    # istodata_per_d = per_d(pricedata['bidclose'], data_per_d)
    # pricedata_stadistics['istodata_per_k'] = istodata_per_k
    # pricedata_stadistics['istodata_per_d'] = istodata_per_d
    #
    # # lbuy_Operacion_sto = crossesOver(istodata_per_d, lower_sto)
    # # lsell_Operacion_sto = crossesUnder(istodata_per_d, upper_sto)
    #
    # message_text = message_text + '\n' + "STO D: " + str(istodata_per_d[len(istodata_per_d) - 1])
    # lbuy_Operacion_sto = False
    # lsell_Operacion_sto = False
    # if istodata_per_d[len(istodata_per_d) - 1] < lower_sto:
    #     message_text = message_text + '\n' + "STO Posicion Compra: "
    #     lstoPosBuy = True
    # if lstoPosBuy:
    #     if istodata_per_d[len(istodata_per_d) - 1] > lower_sto:
    #         lstoPosBuy = False
    #         lbuy_Operacion_sto = True
    #
    # if istodata_per_d[len(istodata_per_d) - 1] > upper_sto:
    #     message_text = message_text + '\n' + "STO Posicion Venta: "
    #     lstoPosSell = True
    #
    # if lstoPosSell:
    #     if istodata_per_d[len(istodata_per_d) - 1] < upper_sto:
    #         lstoPosSell = False
    #         lsell_Operacion_sto = True
    #
    # message_text = message_text + "\n sto buy:" + str(lbuy_Operacion_sto)
    # message_text = message_text + "\n sto sell:" + str(lsell_Operacion_sto)
    #
    #
    #
    #
    for index, row in pricedata_stadistics.iterrows():
         pricedata_stadistics.loc[index, 'upper_rsi'] = upper_rsi
         pricedata_stadistics.loc[index, 'lower_rsi'] = lower_rsi
    #     pricedata_stadistics.loc[index, 'lower_sto'] = lower_sto
    #     pricedata_stadistics.loc[index, 'upper_sto'] = upper_sto
         pricedata_stadistics.loc[index, 'RSI_middle'] = 50

    # *********************************************************************
    # ** Estadistica General - Regresion Lineal Simple 1
    # *********************************************************************
    pricedata_stadistics.iloc[0:0]
    pricedata_stadistics['bidclose'] = pricedata['bidclose'].values
    pricedata_stadistics['bidopen'] = pricedata['bidopen'].values
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

    # ***********************************************************
    # *  EMA'S================
    # ***********************************************************
    iFastSMA = sma(pricedata_stadistics['y'], fast_sma_periods)
    iSlowSMA = sma(pricedata_stadistics['y'], slow_sma_periods)
    # iTooSlowSMA = sma(pricedata_stadistics['y'], too_slow_sma_periods)
    pricedata_stadistics['emaFast'] = iFastSMA
    pricedata_stadistics['emaSlow'] = iSlowSMA
    ##pricedata_stadistics['emaTooSlow'] = iTooSlowSMA

    lbuy_sma = crossesOver(iFastSMA, iSlowSMA)
    lsell_sma = crossesUnder(iFastSMA, iSlowSMA)

    message_text = message_text + "\n sma buy:" + str(lbuy_sma)
    message_text = message_text + "\n sma sell:" + str(lsell_sma)

    # ***********************************************************
    # *  Regresion al precio de cierre las velas ================
    # ***********************************************************
    regresionLineal_xx = np.array(pricedata_stadistics['x'].values)
    regresionLineal_yy = np.array(pricedata_stadistics['y'].values)
    regresionLineal_bb = regresionlineal2.estimate_b0_b1(regresionLineal_xx, regresionLineal_yy)
    y_pred_sup = regresionLineal_bb[0] + regresionLineal_bb[1] * regresionLineal_xx
    pricedata_stadistics['y_pred'] = y_pred_sup

    # Recreacion del Eje X para Presentacion de la Regresion.
    for index, row in pricedata_stadistics.iterrows():
        pricedata_stadistics.loc[index, 'x_pred'] = pricedata_stadistics.loc[0, 'y_pred']

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

    message_text = message_text + "\nAngulo: " + str(angle)
    #
    # pricedata_stadistics['y_bidhigh'] = pricedata['bidhigh'].values
    # pricedata_stadistics['y_bidlow'] = pricedata['bidlow'].values
    # # ************* Calcular la poscion Relativa Y
    # for index, row in pricedata_stadistics.iterrows():
    #     pricedata_stadistics.loc[index, 'y_bidhigh'] = int(
    #         '{:.5f}'.format((pricedata_stadistics.loc[index, 'y_bidhigh'])).replace('.', ''))
    #     pricedata_stadistics.loc[index, 'y_bidlow'] = int(
    #         '{:.5f}'.format((pricedata_stadistics.loc[index, 'y_bidlow'])).replace('.', ''))
    #
    # max_value = max(np.array(pricedata_stadistics['y_bidhigh'].values))
    # min_value = min(np.array(pricedata_stadistics['y_bidhigh'].values))
    # for index, row in pricedata_stadistics.iterrows():
    #     value = pricedata_stadistics.loc[index, 'y_bidhigh'] - min_value
    #     NewPricePosition = ((value * 100) / max_value) * 100
    #     pricedata_stadistics.loc[index, 'y_bidhigh'] = NewPricePosition
    #
    # max_value = max(np.array(pricedata_stadistics['y_bidlow'].values))
    # min_value = min(np.array(pricedata_stadistics['y_bidlow'].values))
    # for index, row in pricedata_stadistics.iterrows():
    #     value = pricedata_stadistics.loc[index, 'y_bidlow'] - min_value
    #     NewPricePosition = ((value * 100) / max_value) * 100
    #     pricedata_stadistics.loc[index, 'y_bidlow'] = NewPricePosition
    #
    # # Regresion al precio mas alto velas ======================
    # regresionLineal_xx = np.array(pricedata_stadistics['x'].values)
    # regresionLineal_yy = np.array(pricedata_stadistics['y_bidhigh'].values)
    # regresionLineal_bb = regresionlineal2.estimate_b0_b1(regresionLineal_xx, regresionLineal_yy)
    #
    # y_pred_sup = regresionLineal_bb[0] + regresionLineal_bb[1] * regresionLineal_xx
    # pricedata_stadistics['y_pred_bidhigh'] = y_pred_sup
    #
    # # Regresion al precio de cierre las velas ======================
    # regresionLineal_xx = np.array(pricedata_stadistics['x'].values)
    # regresionLineal_yy = np.array(pricedata_stadistics['y_bidlow'].values)
    # regresionLineal_bb = regresionlineal2.estimate_b0_b1(regresionLineal_xx, regresionLineal_yy)
    # y_pred_sup = regresionLineal_bb[0] + regresionLineal_bb[1] * regresionLineal_xx
    # pricedata_stadistics['y_pred_bidlow'] = y_pred_sup

    # # *********************************************************************
    # # ** Estadistica General - Regresion Lineal
    # # *********************************************************************
    # pricedata_stadistics_sup.iloc[0:0]
    # pricedata_stadistics_sup['bidclose'] = pricedata_sup['bidclose'].values
    # pricedata_stadistics_sup['bidhigh'] = pricedata_sup['bidhigh'].values
    # pricedata_stadistics_sup['bidlow'] = pricedata_sup['bidlow'].values
    # pricedata_stadistics_sup['rowid'] = np.arange(len(pricedata_stadistics_sup))
    #
    # # *************BIDHIGH
    # # ************* Calcular la poscion Relativa Y
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     pricedata_stadistics_sup.loc[index, 'Y_bidhigh'] = int(
    #         '{:.5f}'.format((pricedata_stadistics_sup.loc[index, 'bidhigh'])).replace('.', ''))
    #
    # max_value = max(np.array(pricedata_stadistics_sup['Y_bidhigh'].values))
    # min_value = min(np.array(pricedata_stadistics_sup['Y_bidhigh'].values))
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     value = pricedata_stadistics_sup.loc[index, 'Y_bidhigh'] - min_value
    #     NewPricePosition = (value * 100) / max_value
    #     pricedata_stadistics_sup.loc[index, 'Y_bidhigh'] = NewPricePosition
    #
    # # ***********  Calcular la poscion Relativa X
    # max_value = max(np.array(pricedata_stadistics_sup['rowid'].values))
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     value = pricedata_stadistics_sup.loc[index, 'rowid']
    #     NewPricePosition = (value * 100) / max_value
    #     pricedata_stadistics_sup.loc[index, 'X_bidhigh'] = NewPricePosition
    #
    # # Regresion al precio mas Alto de las velas ======================
    # regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['X_bidhigh'].values)
    # regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['Y_bidhigh'].values)
    # regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    # y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup
    #
    # pricedata_stadistics_sup['y_pred_bidhigh'] = y_pred_sup
    # pricedata_stadistics_sup['x_pred_bidhigh'] = regresionLineal_xx_sup
    #
    #
    #

    #
    # # *************BIDLOW
    # # ************* Calcular la poscion Relativa Y
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     pricedata_stadistics_sup.loc[index, 'Y_bidlow'] = int(
    #         '{:.5f}'.format((pricedata_stadistics_sup.loc[index, 'bidlow'])).replace('.', ''))
    #
    # max_value = max(np.array(pricedata_stadistics_sup['Y_bidlow'].values))
    # min_value = min(np.array(pricedata_stadistics_sup['Y_bidlow'].values))
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     value = pricedata_stadistics_sup.loc[index, 'Y_bidlow'] - min_value
    #     NewPricePosition = (value * 100) / max_value
    #     pricedata_stadistics_sup.loc[index, 'Y_bidlow'] = NewPricePosition
    #
    # # ***********  Calcular la poscion Relativa X
    # max_value = max(np.array(pricedata_stadistics_sup['rowid'].values))
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     value = pricedata_stadistics_sup.loc[index, 'rowid']
    #     NewPricePosition = (value * 100) / max_value
    #     pricedata_stadistics_sup.loc[index, 'X_bidlow'] = NewPricePosition
    #
    #
    #
    #
    # # Regresion al precio mas Alto de las velas ======================
    # regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['X_bidlow'].values)
    # regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['Y_bidhigh'].values)
    # regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    # y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup
    #
    # pricedata_stadistics_sup['y_pred_bidlow'] = y_pred_sup
    # pricedata_stadistics_sup['x_pred_bidlow'] = regresionLineal_xx_sup
    #
    #
    #

    #
    #
    # # create circle
    # c = plt.Circle((x1, y1), radius=10, color='red', alpha=.3)
    # plt.gca().add_artist(c)
    #
    # #plt.text(x1, y1, str(round(angle, 2)) + ' °')

    # # Regresion al mas bajo de las velas ======================
    # regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['rowid'].tail(numberofregresion_sup).values)
    # regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['bidlow'].tail(numberofregresion_sup).values)
    # regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    # y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup
    #
    # numberRegx = len(pricedata_stadistics_sup) - numberofregresion_sup
    # posreg = 0
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     if numberRegx <= pricedata_stadistics_sup.loc[index, 'rowid']:
    #         pricedata_stadistics_sup.loc[index, 'y_pred_bidlow'] = y_pred_sup[posreg]
    #         posreg = posreg + 1
    #
    # # *********************************************************************
    # # ***    Proyecion de Precios * Se puede Mejorar con Ciclo
    # # *********************************************************************
    # lv_index_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['date']
    # lv_rowid_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['rowid']
    # lv_y_pred_askhigh_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidhigh']
    # lv_y_pred_asklow_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_bidlow']
    #
    # lv_index_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['date']
    # lv_rowid_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['rowid']
    # lv_y_pred_askhigh_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['y_pred_bidhigh']
    # lv_y_pred_asklow_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['y_pred_bidlow']
    #
    # lv_index_base = lv_index_1 - lv_index_2
    # lv_rowid_base = lv_rowid_1 - lv_rowid_2
    # lv_y_pred_askhigh_base = lv_y_pred_askhigh_1 - lv_y_pred_askhigh_2
    # lv_y_pred_asklow_base = lv_y_pred_asklow_1 - lv_y_pred_asklow_2
    #
    # pricedata_stadistics_proyeccion.iloc[0:0]
    # for proyect_times in range(2):
    #     pricedata_stadistics_proyeccion.loc[lv_index_1] = pd.Series(
    #         {'rowid': lv_rowid_1,
    #          'y_pred_bidhigh': lv_y_pred_askhigh_1,
    #          'y_pred_bidlow': lv_y_pred_asklow_1
    #          })
    #     lv_index_1 = lv_index_1 + lv_index_base
    #     lv_rowid_1 = lv_rowid_1 + lv_rowid_base
    #     lv_y_pred_askhigh_1 = lv_y_pred_askhigh_1 + lv_y_pred_askhigh_base
    #     lv_y_pred_asklow_1 = lv_y_pred_asklow_1 + lv_y_pred_asklow_base
    #
    # pricedata_stadistics_proyeccion_tenden.iloc[0:0]
    # for proyect_times in range(3):
    #     pricedata_stadistics_proyeccion_tenden.loc[lv_index_1] = pd.Series(
    #         {'rowid': lv_rowid_1,
    #          'y_pred_bidhigh': lv_y_pred_askhigh_1,
    #          'y_pred_bidlow': lv_y_pred_asklow_1
    #          })
    #     lv_index_1 = lv_index_1 + lv_index_base
    #     lv_rowid_1 = lv_rowid_1 + lv_rowid_base
    #     lv_y_pred_askhigh_1 = lv_y_pred_askhigh_1 + lv_y_pred_askhigh_base
    #     lv_y_pred_asklow_1 = lv_y_pred_asklow_1 + lv_y_pred_asklow_base
    #

    # Calculamos La tendencia con los valores de de la proyection las velas mas altas y mas bajas.
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
    message_text = message_text + "\nTendencia Regresion Lineal: " + lv_Tendency

    # lv_posicion_venta = False
    # lv_posicion_compra = False
    #
    # if lv_Tendency == "Bajista" and (pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['emaTooSlow'] >
    #                                  pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y']):
    #     lv_posicion_venta = True
    #     lv_posicion_compra = False
    #
    # elif lv_Tendency == "Alcista" and (pricedata_stadistics.iloc[len(pricedata_stadistics) - 1][
    #                                        'emaTooSlow'] < pricedata_stadistics.iloc[len(pricedata_stadistics) - 1][
    #                                        'y']):
    #     lv_posicion_venta = False
    #     lv_posicion_compra = True
    #
    # message_text = message_text + "\nPosicion de Venta: " + str(lv_posicion_venta) + " Posicion de Compra: " + str(
    #     lv_posicion_compra)

    # # Print Price/Indicators
    # print("Close Price: " + str(pricedata['bidclose'][len(pricedata) - 1]))
    # # print("Fast SMA: " + str(iFastSMA[len(iFastSMA) - 1]))
    # # print("Slow SMA: " + str(iSlowSMA[len(iSlowSMA) - 1]))

    lregr_Operacion_buy = False
    lregr_Operacion_sell = False
    if pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] > pricedata_stadistics.loc[0, 'y_pred']:
        message_text = message_text + '\n' + "  - > Angulo Posicion Venta: "
        lregrPosSell = True

    if lregrPosSell:
        if pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] < pricedata_stadistics.loc[0, 'y_pred']:
            lregrPosSell = False
            lregr_Operacion_sell = True


    if pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] < pricedata_stadistics.loc[0, 'y_pred']:
        message_text = message_text + '\n' + "  - > Angulo Posicion Compra: "
        lregrPosBuy = True

    if lregrPosBuy:
        if pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y_pred'] > pricedata_stadistics.loc[0, 'y_pred']:
            lregrPosBuy = False
            lregr_Operacion_buy = True



    message_text = message_text + "\n Regresion  buy:" + str(lregrPosBuy)  + " Operacion:" + str(lregr_Operacion_buy)
    message_text = message_text + "\n Regresion  sell:" + str(lregrPosSell) + " Operacion:" + str(lregr_Operacion_sell)


    # # TRADING LOGIC
    #
    if Lbuy_operacion_rsi or (crossesOver(iFastSMA, iSlowSMA) and angle >= angulo_plus):  # or lbuy_sma or Lbuy_operacion_rsi) and angle >= angulo_plus:
        # if (lbuy_Operacion_sto) and angle >= angulo_plus:# or lbuy_sma or Lbuy_operacion_rsi) and angle >= angulo_plus:
        # if crossesOver(iFastSMA, iSlowSMA) and angle >= angulo_plus:
        # if crossesOver(iFastSMA, iSlowSMA) and lv_posicion_compra and angle >= angulo_plus:
        # if Lbuy_operacion_rsi and angle >= angulo_plus:
        message_text = message_text + "\n	  BUY SIGNAL!"
        if countOpenTrades("S") > 0:
            message_text = message_text + "\n	  Closing Sell Trade(s)..."
            exit("S")
        if countOpenTrades("B") == 0:
            message_text = message_text + "\n	  Opening Buy Trade..."
            enter("B")

    if Lsell_operacion_rsi or (crossesUnder(iFastSMA, iSlowSMA) and angle <= angulo_minus):  # or lsell_sma or Lsell_operacion_rsi) and :
        # if (lsell_Operacion_sto) and angle <= angulo_minus:# or lsell_sma or Lsell_operacion_rsi) and :
        # if crossesUnder(iFastSMA, iSlowSMA) and angle <= angulo_minus:
        # if crossesUnder(iFastSMA, iSlowSMA) and lv_posicion_venta and angle <= angulo_minus:
        # if Lsell_ris and angle <= angulo_minus:
        message_text = message_text + "\n	  SELL SIGNAL!"
        if countOpenTrades("B") > 0:
            message_text = message_text + "\n	  Closing Buy Trade(s)..."
            exit("B")
        if countOpenTrades("S") == 0:
            message_text = message_text + "\n	  Opening Sell Trade..."
            enter("S")

    message_text = message_text + "\n" + str(
        dt.datetime.now()) + " " + timeframe + " Update Function Completed.========= \n"
    print(message_text)
    message_text = ''


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


if __name__ == '__main__':
    Prepare()  # Initialize strategy
    StrategyHeartBeat()  # Run strategy
