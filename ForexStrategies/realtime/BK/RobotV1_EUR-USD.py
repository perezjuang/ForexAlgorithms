# Librerias Generales del Sistema
# ==============================================================================
import datetime as dt
import gc
import os
import time

import pandas

import Probabilidades.RegrsionLineal2 as regresionlineal2
import itertools
# Librerias Forex
# ==============================================================================
import fxcmpy
# Librerias Financieras
import matplotlib.pyplot as plt
import numpy as np

# Tratamiento de datos
# ==============================================================================
import pandas as pd
import configparser  # https://docs.python.org/3/library/configparser.html

gc.collect()
config = configparser.ConfigParser()
config.read('RobotV1ForexConfig.ini')

pd.set_option("display.max_rows", None, "display.max_columns", None)

# Extraemos la Moneda del Nombre del Archivo
fileName = str(os.path.basename(__file__))
fileName = fileName.replace(".py", "")
fileName = fileName.replace("RobotV1_", "")
symbol = fileName.replace("-", "/")

# Exraemos informacion para conexion
conectionConfig = config['conection']
token = conectionConfig['token']

# Configuracion del Frame
# Periodos : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframeConfig = config['timeframe']

timeframe = timeframeConfig['timeframe']
numberofcandles = int(timeframeConfig['numberofcandles'])

timeframe_sup = timeframeConfig['timeframe_sup']
numberofcandles_sup = int(timeframeConfig['numberofcandles_sup'])
numberofregresion_sup = int(timeframeConfig['numberofregresion_sup'])

# Configuracion de las SMA
fast_sma_periods = int(timeframeConfig['fast_sma_periods'])
slow_sma_periods = int(timeframeConfig['slow_sma_periods'])

numberofregresion = int(timeframeConfig['numberofregresion'])

# Configuracion Operaciones Trade
operationsConfig = config['operations']
amount = int(operationsConfig['amount'])
stop = int(operationsConfig['stop'])
limit = int(operationsConfig['limit'])
trailing_step = int(operationsConfig['trailing_step'])

# Global Variables
pricedata = None
pricedata_sup = None
y_pred = None
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
                                                 'bidclose', 'askhigh', 'asklow',
                                                 'y_pred_sup', 'y_pred_askhigh', 'y_pred_asklow', 'date'
                                                 ])

pricedata_stadistics_proyeccion = pd.DataFrame([],
                                               columns=['rowid',
                                                        'y_pred_sup', 'y_pred_askhigh', 'y_pred_asklow', 'date'
                                                        ])
pricedata_stadistics_sup_tmp = pricedata_stadistics_sup

print("Iniciando Conexion...")
con = fxcmpy.fxcmpy(access_token=token, log_level="error", log_file=None)

guiConfig = config['gui']
guishow = int(guiConfig['show'])

lv_posicion_venta = False
lv_posicion_compra = False

if guishow == 1:
    print("Iniciando Plotter")
    plt.style.use('dark_background')
    plt.ion()  # Enable interactive mode
    plt.show(block=False)

    fig = plt.figure()
    fig.canvas.set_window_title('Divisa - ' + symbol)

    ax1 = fig.add_subplot(1, 1, 1)
    ax1.clear()
    ax1.set_autoscale_on(True)

    linePrice, = ax1.plot([], [], label='Movimiento del Precio ' + timeframe)

    lineEmaFast, = ax1.plot([], [], label='EMA Fast ' + str(fast_sma_periods))
    # lineEmaSlow, = ax1.plot([], [], label='EMA Slow ' + str(slow_sma_periods))

    # lineBuys, = ax1.plot([], [], 'g^', label='Compras ' + timeframe, )
    # lineSells, = ax1.plot([], [], 'rv', label='Ventas ' + timeframe)
    # lineRegr, = ax1.plot([], [], label='Regresion Lineal bidclose ' + timeframe_sup)
    lineRegraskhigh, = ax1.plot([], [], label='Regresion Lineal askhigh ' + timeframe_sup)
    lineRegrasklow, = ax1.plot([], [], label='Regresion Lineal asklow ' + timeframe_sup)

    lineRegraskhigh_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal askhigh ' + timeframe_sup)
    lineRegrasklow_proyeccion, = ax1.plot([], [], label='Proyeccion Lineal asklow ' + timeframe_sup)

else:
    print("Iniciando sin Plotter")


def UpdatePlotter():
    linePrice.set_data(pricedata['bidclose'].index, pricedata_stadistics['bidclose'].values)
    lineEmaFast.set_data(pricedata_stadistics['emaFast'].index, pricedata_stadistics['emaFast'].values)
    # lineEmaSlow.set_data(pricedata_stadistics['emaSlow'].index, pricedata_stadistics['emaSlow'].values)

    # lineRegr.set_data(pricedata_stadistics['bidclose'].index, pricedata_stadistics['y_pred'].values)
    lineRegraskhigh.set_data(pricedata_stadistics_sup['y_pred_askhigh'].index,
                             pricedata_stadistics_sup['y_pred_askhigh'].values)

    lineRegrasklow.set_data(pricedata_stadistics_sup['y_pred_asklow'].index,
                            pricedata_stadistics_sup['y_pred_asklow'].values)

    lineRegrasklow_proyeccion.set_data(pricedata_stadistics_proyeccion['y_pred_asklow'].index,
                                       pricedata_stadistics_proyeccion['y_pred_asklow'].values)

    lineRegraskhigh_proyeccion.set_data(pricedata_stadistics_proyeccion['y_pred_askhigh'].index,
                                        pricedata_stadistics_proyeccion['y_pred_askhigh'].values)

    # lineRegrInf.set_data(pricedata_stadistics['bidclose'].index, pricedata_stadistics['y_pred_inf'].values)
    # Compras
    # buys = pricedata_stadistics.iloc[np.where(pricedata_stadistics['position'] == 1.0)]
    # lineBuys.set_data(buys['bidclose'].index, buys['bidclose'].values)

    # Ventas
    # sells = pricedata_stadistics.iloc[np.where(pricedata_stadistics['position'] == -1.0)]
    # lineSells.set_data(sells['bidclose'].index, sells['bidclose'].values)

    ax1.legend(loc='lower center', prop={'size': 7})
    ax1.relim()
    ax1.autoscale_view(True, True, True)
    plt.draw()
    plt.pause(0.5)

def Prepare():
    global pricedata
    global pricedata_sup

    print("Solicitando Precios...")
    pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    pricedata_sup = con.get_candles(symbol, period=timeframe_sup, number=numberofcandles_sup)
    print("Precios Iniciales Recibidos")


def StrategyStart():
    Update()
    while True:
        currenttime = dt.datetime.now()
        if timeframe == "m1" and currenttime.second == 0:
            if getLatestPriceData():
                Update()
        elif timeframe == "m5" and currenttime.second == 0 and currenttime.minute % 5 == 0:
            if getLatestPriceData():
                Update()
        elif timeframe == "m15" and currenttime.second == 0 and currenttime.minute % 15 == 0:
            if getLatestPriceData():
                Update()
        elif timeframe == "m30" and currenttime.second == 0 and currenttime.minute % 30 == 0:
            if getLatestPriceData():
                Update()
        elif currenttime.second == 0 and currenttime.minute == 0:
            if getLatestPriceData():
                Update()
        if guishow == 1:
            UpdatePlotter()


def getLatestPriceData():
    global pricedata
    global pricedata_sup
    global con
    currenttime = dt.datetime.now()
    print("\n" + str(currenttime) + " " + timeframe + " ------------------------------------ ")
    print("Solicitando Precios...")
    try:
        if currenttime.second == 0 and currenttime.minute == 0:
            pricedata_sup = con.get_candles(symbol, period=timeframe_sup, number=numberofcandles_sup)
        else:
            new_pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)
            if len(new_pricedata.index.values) > 0:
                pricedata = new_pricedata
        print("Precios Recibidos...")
        return True
    except Exception as e:
        print(str(e))
        print("Error Solicitando Precios  - Reiniciando Conexion... 2 Min")
        time.sleep(120)
        return False


def Update():
    global pricedata
    global pricedata_sup
    global y_pred
    global lv_posicion_venta
    global lv_posicion_compra
    # *********************************************************************
    # ** Estadistica General - Regresion Lineal Simple 1
    # *********************************************************************
    pricedata_stadistics['bidclose'] = pricedata['bidclose'].values
    pricedata_stadistics['bidopen'] = pricedata['bidopen'].values
    pricedata_stadistics['date'] = pricedata['bidclose'].index
    pricedata_stadistics['emaFast'] = pricedata_stadistics['bidclose'].rolling(window=fast_sma_periods).mean()
    # pricedata_stadistics['emaSlow'] = pricedata_stadistics['bidclose'].rolling(window=slow_sma_periods).mean()
    # pricedata_stadistics['signal'] = np.where((pricedata_stadistics['emaFast'] > pricedata_stadistics['emaSlow']), 1, 0)
    # pricedata_stadistics['position'] = pricedata_stadistics['signal'].diff()
    pricedata_stadistics.index = pricedata['bidclose'].index
    pricedata_stadistics['rowid'] = np.arange(len(pricedata_stadistics))

    # *********************************************************************
    # ** Estadistica General - Precio
    # *********************************************************************
    regresionLineal_xx = np.array(pricedata_stadistics['rowid'].tail(numberofregresion).values)
    regresionLineal_yy = np.array(pricedata_stadistics['bidclose'].tail(numberofregresion).values)
    regresionLineal_bb = regresionlineal2.estimate_b0_b1(regresionLineal_xx, regresionLineal_yy)
    y_pred = regresionLineal_bb[0] + regresionLineal_bb[1] * regresionLineal_xx

    numberRegx = len(pricedata_stadistics) - numberofregresion
    posreg = 0
    for index, row in pricedata_stadistics.iterrows():
        if numberRegx <= pricedata_stadistics.loc[index, 'rowid']:
            pricedata_stadistics.loc[index, 'y_pred'] = y_pred[posreg]
            posreg = posreg + 1

    # *********************************************************************
    # ** Estadistica General - Regresion Lineal
    # *********************************************************************

    pricedata_stadistics_sup['bidclose'] = pricedata_sup['bidclose'].values
    pricedata_stadistics_sup['date'] = pricedata_sup['bidclose'].index
    pricedata_stadistics_sup['askhigh'] = pricedata_sup['askhigh'].values
    pricedata_stadistics_sup['asklow'] = pricedata_sup['asklow'].values
    pricedata_stadistics_sup.index = pricedata_sup['bidclose'].index
    pricedata_stadistics_sup['rowid'] = np.arange(len(pricedata_stadistics_sup))

    # Regresion al Precio del Cierre bidclose
    # regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['rowid'].tail(numberofregresion_sup).values)
    # regresionLineal_yy_sup = np.array(pricedata_stadistics['bidclose'].tail(numberofregresion_sup).values)
    # regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    # y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup
    #
    # numberRegx = len(pricedata_stadistics_sup) - numberofregresion_sup
    # posreg = 0
    # for index, row in pricedata_stadistics_sup.iterrows():
    #     if numberRegx <= pricedata_stadistics_sup.loc[index, 'rowid']:
    #         pricedata_stadistics_sup.loc[index, 'y_pred_sup'] = y_pred_sup[posreg]
    #         posreg = posreg + 1

    # Regresion al mas Alto de las velas ======================
    regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['rowid'].tail(numberofregresion_sup).values)
    regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['askhigh'].tail(numberofregresion_sup).values)
    regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup

    numberRegx = len(pricedata_stadistics_sup) - numberofregresion_sup
    posreg = 0
    for index, row in pricedata_stadistics_sup.iterrows():
        if numberRegx <= pricedata_stadistics_sup.loc[index, 'rowid']:
            pricedata_stadistics_sup.loc[index, 'y_pred_askhigh'] = y_pred_sup[posreg]
            posreg = posreg + 1

    # Regresion al mas bajo de las velas ======================
    regresionLineal_xx_sup = np.array(pricedata_stadistics_sup['rowid'].tail(numberofregresion_sup).values)
    regresionLineal_yy_sup = np.array(pricedata_stadistics_sup['asklow'].tail(numberofregresion_sup).values)
    regresionLineal_bb_sup = regresionlineal2.estimate_b0_b1(regresionLineal_xx_sup, regresionLineal_yy_sup)
    y_pred_sup = regresionLineal_bb_sup[0] + regresionLineal_bb_sup[1] * regresionLineal_xx_sup

    numberRegx = len(pricedata_stadistics_sup) - numberofregresion_sup
    posreg = 0
    for index, row in pricedata_stadistics_sup.iterrows():
        if numberRegx <= pricedata_stadistics_sup.loc[index, 'rowid']:
            pricedata_stadistics_sup.loc[index, 'y_pred_asklow'] = y_pred_sup[posreg]
            posreg = posreg + 1





    # *********************************************************************
    # ***    Proyecion de Precios * Se puede Mejorar con Ciclo
    # *********************************************************************
    lv_index_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['date']
    lv_rowid_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['rowid']
    lv_y_pred_askhigh_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_askhigh']
    lv_y_pred_asklow_1 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_asklow']

    lv_index_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['date']
    lv_rowid_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['rowid']
    lv_y_pred_askhigh_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['y_pred_askhigh']
    lv_y_pred_asklow_2 = pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 2]['y_pred_asklow']

    lv_index_base = lv_index_1 - lv_index_2
    lv_rowid_base = lv_rowid_1 - lv_rowid_2
    lv_y_pred_askhigh_base = lv_y_pred_askhigh_1 - lv_y_pred_askhigh_2
    lv_y_pred_asklow_base = lv_y_pred_asklow_1 - lv_y_pred_asklow_2
    pricedata_stadistics_proyeccion.iloc[0:0]

    pricedata_stadistics_proyeccion.loc[lv_index_1] = pandas.Series(
        {'rowid': lv_rowid_1,
         'y_pred_askhigh': lv_y_pred_askhigh_1,
         'y_pred_asklow': lv_y_pred_asklow_1
         })

    pricedata_stadistics_proyeccion.loc[lv_index_1 + lv_index_base] = pandas.Series(
        {'rowid': lv_rowid_1 + lv_rowid_base,
         'y_pred_askhigh': lv_y_pred_askhigh_1 + lv_y_pred_askhigh_base,
         'y_pred_asklow': lv_y_pred_asklow_1 + lv_y_pred_asklow_base
         })

    # pricedata_stadistics_proyeccion.loc[lv_index_1 + lv_index_base + lv_index_base] = pandas.Series(
    #     {'rowid': lv_rowid_1 + lv_rowid_base + lv_rowid_base,
    #      'y_pred_askhigh': lv_y_pred_askhigh_1 + lv_y_pred_askhigh_base + lv_y_pred_askhigh_base,
    #      'y_pred_asklow': lv_y_pred_asklow_1 + lv_y_pred_asklow_base + lv_y_pred_asklow_base
    #      })

    # pricedata_stadistics_proyeccion.loc[lv_index_1 + lv_index_base + lv_index_base + lv_index_base] = pandas.Series(
    #     {'rowid': lv_rowid_1 + lv_rowid_base + lv_rowid_base + lv_rowid_base,
    #      'y_pred_askhigh': lv_y_pred_askhigh_1 + lv_y_pred_askhigh_base + lv_y_pred_askhigh_base + lv_y_pred_askhigh_base,
    #      'y_pred_asklow': lv_y_pred_asklow_1 + lv_y_pred_asklow_base + lv_y_pred_asklow_base + lv_y_pred_asklow_base
    #      })


    print(pricedata_stadistics_proyeccion)
    # Calculamos La tendencia con los valores de de la proyection las velas mas altas y mas bajas.
    lv_Tendency = "Lateral"
    if pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_askhigh'] < \
            pricedata_stadistics_sup.iloc[1]['y_pred_askhigh'] and \
            pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_asklow'] < \
            pricedata_stadistics_sup.iloc[1]['y_pred_asklow']:
        lv_Tendency = "Bajista"
    elif pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_askhigh'] > \
            pricedata_stadistics_sup.iloc[1]['y_pred_askhigh'] and \
            pricedata_stadistics_sup.iloc[len(pricedata_stadistics_sup) - 1]['y_pred_asklow'] > \
            pricedata_stadistics_sup.iloc[1]['y_pred_asklow']:
        lv_Tendency = "Alcista"

    print("Tendencia Regresion Lineal: " + lv_Tendency)

    if lv_Tendency == "Bajista" and pricedata_stadistics_proyeccion.iloc[len(pricedata_stadistics_proyeccion) - 1][
        'y_pred_askhigh'] < pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['emaFast']:
        lv_posicion_venta = True
        lv_posicion_compra = False
    elif lv_Tendency == "Alcista" and pricedata_stadistics_proyeccion.iloc[len(pricedata_stadistics_proyeccion) - 1][
        'y_pred_asklow'] > pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['emaFast']:
        lv_posicion_venta = False
        lv_posicion_compra = True

    lv_accion = 'NA'
    if lv_Tendency == "Bajista" and lv_posicion_venta == True and \
            pricedata_stadistics_proyeccion.iloc[len(pricedata_stadistics_proyeccion) - 1]['y_pred_askhigh'] > \
            pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['emaFast']:
        lv_accion = 'Venta'
    elif lv_Tendency == "Alcista" and lv_posicion_compra == True and \
            pricedata_stadistics_proyeccion.iloc[len(pricedata_stadistics_proyeccion) - 1]['y_pred_asklow'] < \
            pricedata_stadistics.iloc[len(pricedata_stadistics) - 1]['emaFast']:
        lv_accion = 'Compra'

    print("Posicion de Venta: " + str(lv_posicion_venta) + " Posicion de Compra: " + str(lv_posicion_compra))
    print("Operacion: " + lv_accion)

    if lv_accion == 'Compra':
        lv_posicion_venta = False
        lv_posicion_compra = False
        print("	 SEÑAL DE COMPRA ! \n")
        if countOpenTrades("S") > 0:
            print("	  Cerrando Ventas Abiertas...\n")
            exit("S")
        if countOpenTrades("B") == 0:
            enter("B")
        print("	  Abrir Operacion de Compra...\n")

    if lv_accion == 'Venta':
        lv_posicion_venta = False
        lv_posicion_compra = False
        print("	  SEÑAL DE VENTA ! \n")
        if countOpenTrades("B") > 0:
            print("	  Cerrando Operacion de Compras...\n")
            exit("B")
        if countOpenTrades("S") == 0:
            enter("S")
        print("	  Abrir Operacion de Venta...\n")
    gc.collect()
    print(str(dt.datetime.now()) + " " + timeframe + "\n")


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
    time.sleep(3)


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
    time.sleep(3)


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
    Prepare()  # Perar la Estrategia
    StrategyStart()  # Iniciar la Estrategia
