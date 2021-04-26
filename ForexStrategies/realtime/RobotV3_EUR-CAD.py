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
import math
from datetime import datetime

# Extraemos la Moneda del Nombre del Archivo
fileName = str(os.path.basename(__file__))
fileName = fileName.replace(".py", "")
fileName = fileName.replace("RobotV3_", "")
symbol = fileName.replace("-", "/")

# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = "m1"

fast_sma_periods = 10
slow_sma_periods = 30
too_slow_sma_periods = 100

amount = 5
stop = -5
limit = 15

# Global Variables
pricedata = None

numberofcandles = 1440

con = fxcmpy.fxcmpy(config_file='../fxcm.cfg')

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
                                             ])

plt.style.use('dark_background')

plt.ion()  # Enable interactive mode
plt.show(block=False)
fig = plt.figure()

ax1 = fig.add_subplot(1, 1, 1)
ax1.clear()
ax1.axis("equal")

# ax1.set_autoscale_on(True)

# ax2 = fig.add_subplot(2, 2, 4)
# ax2.clear()
# ax2.axis("equal")
# create circle


linePrice, = ax1.plot([], [], label='Precio ' + timeframe + ' ' + symbol)
lineRegrbidClose, = ax1.plot([], [], label='Regresion Lineal Precio ' + timeframe, color='silver', linestyle='--')
lineRegrbidCloseX, = ax1.plot([], [], label='Regresion Lineal Precio ' + timeframe, color='silver', linestyle='--')

# lineRegrbidhigh, = ax1.plot([], [], label='Regresion Lineal bidhigh ' + timeframe)
# lineRegrbidlow, = ax1.plot([], [], label='Regresion Lineal bidlow ' + timeframe)


lineEmaFast, = ax1.plot([], [], label='EMA Fast ' + str(fast_sma_periods))
lineEmaSlow, = ax1.plot([], [], label='EMA Slow ' + str(slow_sma_periods))
lineEmaTooSlow, = ax1.plot([], [], label='EMA Too Slow ' + str(too_slow_sma_periods))


# lineRegrbidhigh, = ax1.plot([], [], label='Regresion Lineal bidhigh ' + timeframe_sup)
# lineRegrbidlow, = ax1.plot([], [], label='Regresion Lineal bidlow ' + timeframe_sup)

# lineRegrbidhigh_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal bidhigh ' + timeframe_sup)
# lineRegrbidlow_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal bidlow ' + timeframe_sup)

# lineRegrbidhigh_proyeccion_tend, = ax1.plot([], [], label='Proyeccion Lineal bidhigh tend ' + timeframe_sup)
# lineRegrbidlow_proyeccion_tend, = ax1.plot([], [], label='Proyeccion Lineal bidlow tend' + timeframe_sup)

# lineATriangulo, = ax1.plot([], [], label='LineaA', color='silver', linestyle='--')
# lineBTriangulo, = ax1.plot([], [], label='LineaB', color='silver', linestyle='--')


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
    lineEmaTooSlow.set_data(pricedata_stadistics['x'].values, pricedata_stadistics['emaTooSlow'].values)
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
    # ax1.autoscale_view(True, True, True)
    plt.draw()
    plt.pause(1)


def Prepare():
    global pricedata
    print("Requesting Initial Price Data...")
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
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
    global pricedata
    try:
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
    iTooSlowSMA = sma(pricedata_stadistics['y'], too_slow_sma_periods)
    pricedata_stadistics['emaFast'] = iFastSMA
    pricedata_stadistics['emaSlow'] = iSlowSMA
    pricedata_stadistics['emaTooSlow'] = iTooSlowSMA

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
    #angle2 = np.rad2deg(np.arctan2(vy[-1] - vy[0], vx[-1] - vx[0]))

    print("Angulo: " + str(angle) )
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

    print("Tendencia Regresion Lineal: " + lv_Tendency)

    lv_posicion_venta = False
    lv_posicion_compra = False

    if lv_Tendency == "Bajista" and (pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['emaTooSlow'] > pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['y']):

        lv_posicion_venta = True
        lv_posicion_compra = False

    elif lv_Tendency == "Alcista" and (pricedata_stadistics.iloc[len(pricedata_stadistics) - 1][
                                           'emaTooSlow'] < pricedata_stadistics.iloc[len(pricedata_stadistics) - 1][
                                           'y']):
        lv_posicion_venta = False
        lv_posicion_compra = True

    print("Posicion de Venta: " + str(lv_posicion_venta) + " Posicion de Compra: " + str(lv_posicion_compra))

    # # Print Price/Indicators
    # print("Close Price: " + str(pricedata['bidclose'][len(pricedata) - 1]))
    # # print("Fast SMA: " + str(iFastSMA[len(iFastSMA) - 1]))
    # # print("Slow SMA: " + str(iSlowSMA[len(iSlowSMA) - 1]))
    #
    # # TRADING LOGIC
    #
    if crossesOver(iFastSMA, iSlowSMA):
        print("	  BUY SIGNAL!")
        if countOpenTrades("S") > 0:
            print("	  Closing Sell Trade(s)...")
            exit("S")
        if countOpenTrades("B") == 0 and lv_posicion_compra and angle >= 20:
            print("	  Opening Buy Trade...")
            enter("B")

    if crossesUnder(iFastSMA, iSlowSMA):
        print("	  SELL SIGNAL!")
        if countOpenTrades("B") > 0:
            print("	  Closing Buy Trade(s)...")
            exit("B")
        if countOpenTrades("S") == 0 and lv_posicion_venta and angle <= -20:
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
