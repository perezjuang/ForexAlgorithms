import configparser
import datetime as dt
import os
import time
import fxcmpy
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from pyti.simple_moving_average import simple_moving_average as sma
from pyti.stochastic import percent_k as per_k
from pyti.stochastic import percent_d as per_d
from pyti.relative_strength_index import relative_strength_index as rsi
import Probabilidades.RegrsionLineal2 as regresionlineal2
import asyncio
import threading
import sys


class ZRobotOOP(threading.Thread):
    # instance attributes
    def __init__(self, symbol, con):
        threading.Thread.__init__(self)
        self.symbol = symbol
        self.config = configparser.ConfigParser()
        self.config.read('RobotV5.ini')
        self.time_frame_operations = self.config['timeframe']
        # Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
        self.timeframe = self.time_frame_operations['timeframe']

        self.token = self.time_frame_operations['token']

        self.fast_sma_periods = int(self.time_frame_operations['fast_sma_periods'])
        self.slow_sma_periods = int(self.time_frame_operations['slow_sma_periods'])

        self.stoD = int(self.time_frame_operations['stoD'])
        self.stoK = int(self.time_frame_operations['stoK'])

        self.amount = int(self.time_frame_operations['amount'])
        self.stop = int(self.time_frame_operations['stop'])
        self.limit = int(self.time_frame_operations['limit'])
        self.trailing_step = int(self.time_frame_operations['trailing_step'])
        self.SHOWGUI = int(self.time_frame_operations['SHOWGUI'])
        self.macdSlow = 26
        self.macdFast = 12
        self.macdSmooth = 9

        symbolConfig = self.symbol.replace("/", "_")
        self.macdsub = float(self.time_frame_operations["macdsub" + symbolConfig])
        self.macduper = float(self.time_frame_operations["macduper" + symbolConfig])

        # Global Variables
        self.pricedata = None
        self.numberofcandles = int(self.time_frame_operations['numberofcandles'])
        self.operacionventa = False
        self.operacioncompra = False

        self.open_conexion = True
        self.con = con
        self.pricedata_stadistics = pd.DataFrame([],
                                                 columns=['index', 'indexdates'
                                                                   'x', 'y'
                                                                        'bidclose',
                                                          'pos',
                                                          'y_pred',
                                                          'y_pred_self.logMessages = self.logMessages + "\n" + ',
                                                          'x_pred_self.logMessages = self.logMessages + "\n" + ',
                                                          'tickqty', 'per_k', 'per_d',
                                                          'lower_sto', 'upper_sto', "n_high", "n_low"
                                                                                              'macd', 'signal', 'hist',
                                                          'macdline0',
                                                          'macdoperSale', 'macdoperBuy'
                                                          ])
        self.openpositions = self.con.get_open_positions(kind='list')

        self.exitFlag = False
        self.logMessages = ""

    def getLatestPriceData(self):
        self.logMessages = self.logMessages + "\n" + (
                "Operacion Compra: " + str(self.operacioncompra) + " - Operacion Venta " + str(self.operacionventa))
        if self.con.connection_status == "established":
            try:
                self.logMessages = self.logMessages + "\n" + ("Geting Prices")
                new_pricedata = self.con.get_candles(self.symbol, period=self.timeframe, number=self.numberofcandles)
                self.logMessages = self.logMessages + "\n" + ("Prices ")
                self.logMessages = self.logMessages + "\n" + str(new_pricedata)

                self.logMessages = self.logMessages + "\n" + ("Prices Recived")
                self.logMessages = self.logMessages + "\n" + str(len(new_pricedata.index))
                if len(new_pricedata.index) == 0:
                    self.logMessages = self.logMessages + "\n" + ("Prices not Updated and not recived")
                    return False
                else:
                    if new_pricedata.index.values[len(new_pricedata.index.values) - 1] != self.pricedata.index.values[
                        len(self.pricedata.index.values) - 1]:
                        self.pricedata = new_pricedata
                    else:
                        self.logMessages = self.logMessages + "\n" + ("Prices not Updated")
                return True
            except Exception as e:
                self.logMessages = self.logMessages + "\n" + (
                        "\n1.An exception occurred Obtaining Prices: " + self.symbol + " Exception: " + str(e))
                return False
        else:
            return False

    def Prepare(self):
        self.logMessages = self.logMessages + "\n" + ("Solicitando Precios...")
        self.pricedata = self.con.get_candles(self.symbol, period=self.timeframe, number=self.numberofcandles)
        self.logMessages = self.logMessages + "\n" + ("Precios Iniciales Recibidos")
        if self.countOpenTrades("B") > 0:
            self.operacioncompra = True
        if self.countOpenTrades("S") > 0:
            self.operacionventa = True
        self.logMessages = self.logMessages + "\n" + (
                "Operacion Compra: " + str(self.operacioncompra) + " - Operacion Venta " + str(self.operacionventa))
        return True

    def countOpenTrades(self, BuySell=None):
        self.openpositions = self.con.get_open_positions(kind='list')
        isbuy = True
        counter = 0
        if BuySell == "S":
            isbuy = False
        for position in self.openpositions:
            if position['currency'] == self.symbol:
                if BuySell is None or position['isBuy'] == isbuy:
                    counter += 1
        return counter

    def crossesOver(self, stream1, stream2):
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
    def crossesUnder(self, stream1, stream2):
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
    def enter(self, BuySell=None):
        opentrade = None
        direction = True
        if BuySell == "S":
            direction = False
        try:
            # opentrade = con.open_trade(symbol=symbol, is_buy=direction,amount=amount, time_in_force='GTC',order_type='AtMarket',is_in_pips=True,limit=limit, stop=stop, trailing_step=1)
            opentrade = self.con.open_trade(symbol=self.symbol,
                                            is_buy=direction,
                                            amount=self.amount,
                                            time_in_force='GTC',
                                            order_type='AtMarket',
                                            is_in_pips=True,
                                            limit=self.limit,
                                            stop=self.stop, trailing_step=self.trailing_step)

        except:
            self.logMessages = self.logMessages + "\n" + ("	  Error Abriendo la Operacion.")
        else:
            self.logMessages = self.logMessages + "\n" + ("	  Operacion Abierta Exitosamente.")
        return opentrade

    def exit(self, BuySell=None):
        closetrade = None
        self.openpositions = self.con.get_open_positions(kind='list')
        isbuy = True
        if BuySell == "S":
            isbuy = False
        for position in self.openpositions:
            if position['currency'] == self.symbol:
                if BuySell is None or position['isBuy'] == isbuy:
                    self.logMessages = self.logMessages + "\n" + ("	  Cerrando Operacion: " + position['tradeId'])
                    try:
                        closetrade = self.con.close_trade(trade_id=position['tradeId'], amount=position['amountK'])
                    except:
                        self.logMessages = self.logMessages + "\n" + ("	  Error cerrando la operacion.")
                    else:
                        self.logMessages = self.logMessages + "\n" + ("	  Operacion Cerrada Satisfactoriamente.")
        return closetrade

    def Update(self):
        self.logMessages = self.logMessages + "\n" + (
                str(dt.datetime.now()) + " " + self.timeframe + " Vela Formada - Analizando -  Running Update Function...")

        self.pricedata_stadistics['index'] = self.pricedata['bidclose'].index
        self.pricedata_stadistics['bidclose'] = self.pricedata['bidclose'].values
        self.pricedata_stadistics['bidhigh'] = self.pricedata['bidhigh'].values
        self.pricedata_stadistics['askclose'] = self.pricedata['askclose'].values
        self.pricedata_stadistics['askhigh'] = self.pricedata['askhigh'].values
        self.pricedata_stadistics['asklow'] = self.pricedata['asklow'].values
        self.pricedata_stadistics['askclose'] = self.pricedata['askclose'].values
        self.pricedata_stadistics['bidlow'] = self.pricedata['bidlow'].values
        self.pricedata_stadistics['tickqty'] = self.pricedata['tickqty'].values

        # Calculate Indicators
        iFastSMA = sma(self.pricedata['bidclose'], self.fast_sma_periods)
        iSlowSMA = sma(self.pricedata['bidclose'], self.slow_sma_periods)

        self.pricedata_stadistics['emaFast'] = iFastSMA
        self.pricedata_stadistics['emaSlow'] = iSlowSMA

        # Adds a "n_high" column with max value of previous 14 periods
        self.pricedata_stadistics['n_high'] = self.pricedata_stadistics['bidhigh'].rolling(self.stoK).max()
        # Adds an "n_low" column with min value of previous 14 periods
        self.pricedata_stadistics['n_low'] = self.pricedata_stadistics['bidlow'].rolling(self.stoK).min()
        # Uses the min/max values to calculate the %k (as a percentage)

        self.pricedata_stadistics['per_k'] = \
            (
                    (self.pricedata_stadistics['bidclose'] - self.pricedata_stadistics['n_low']) / \
                    (self.pricedata_stadistics['n_high'] - self.pricedata_stadistics['n_low'])
            ) * 100

        # Uses the %k to calculates a SMA over the past 3 values of %k
        self.pricedata_stadistics['per_d'] = self.pricedata_stadistics['per_k'].rolling(self.stoD).mean()

        # data_per_k = per_k(pricedata['bidclose'], stoK)
        # data_per_d = per_d(pricedata['bidclose'], stoD)

        data_per_k = self.pricedata_stadistics['per_k']
        data_per_d = self.pricedata_stadistics['per_d']
        # pricedata_stadistics['per_k'] = data_per_k
        # pricedata_stadistics['per_d'] = data_per_d
        # self.pricedata_stadistics.loc[index, 'lower_sto'] = 20
        # self.pricedata_stadistics.loc[index, 'upper_sto'] = 80

        self.logMessages = self.logMessages + "\n" + ("STO K " + str(data_per_k[len(data_per_k) - 1]))
        self.logMessages = self.logMessages + "\n" + ("STO D " + str(data_per_d[len(data_per_d) - 1]))

        # Calcular Indicador
        iRSI = rsi(self.pricedata_stadistics['bidclose'], 15)
        self.logMessages = self.logMessages + "\n" + ("RSI: " + str(iRSI[len(iRSI) - 1]))

        for index, row in self.pricedata_stadistics.iterrows():
            self.pricedata_stadistics.loc[index, 'lower_sto'] = 20
            self.pricedata_stadistics.loc[index, 'upper_sto'] = 80
            self.pricedata_stadistics.loc[index, 'macdline0'] = 0.00

        # ***********************************************************
        # *  Regresion al precio de cierre las velas ================
        # ***********************************************************
        self.pricedata_stadistics['x'] = np.arange(len(self.pricedata_stadistics))
        # ************* Calcular la poscion Relativa Y
        for index, row in self.pricedata_stadistics.iterrows():
            self.pricedata_stadistics.loc[index, 'y'] = int(
                '{:.5f}'.format((self.pricedata_stadistics.loc[index, 'bidclose'])).replace('.', ''))

        max_value = max(np.array(self.pricedata_stadistics['y'].values))
        min_value = min(np.array(self.pricedata_stadistics['y'].values))
        for index, row in self.pricedata_stadistics.iterrows():
            value = self.pricedata_stadistics.loc[index, 'y'] - min_value
            NewPricePosition = ((value * 100) / max_value) * 100
            self.pricedata_stadistics.loc[index, 'y'] = NewPricePosition

        # ***********  Calcular la poscion Relativa X
        max_value = max(np.array(self.pricedata_stadistics['x'].values))
        min_value = min(np.array(self.pricedata_stadistics['x'].values))
        for index, row in self.pricedata_stadistics.iterrows():
            value = self.pricedata_stadistics.loc[index, 'x'] - min_value
            NewPricePosition = ((value * 100) / max_value)
            self.pricedata_stadistics.loc[index, 'x'] = NewPricePosition

        regresionLineal_xx = np.array(self.pricedata_stadistics['x'].values)
        regresionLineal_yy = np.array(self.pricedata_stadistics['y'].values)

        regresionLineal_bb = regresionlineal2.estimate_b0_b1(regresionLineal_xx, regresionLineal_yy)
        y_pred_sup = regresionLineal_bb[0] + regresionLineal_bb[1] * regresionLineal_xx
        self.pricedata_stadistics['y_pred'] = y_pred_sup

        if self.pricedata_stadistics.iloc[len(self.pricedata_stadistics) - 1]['y_pred'] < \
                self.pricedata_stadistics.iloc[1]['y_pred'] and \
                self.pricedata_stadistics.iloc[len(self.pricedata_stadistics) - 1]['y_pred'] < \
                self.pricedata_stadistics.iloc[1]['y_pred']:
            lv_Tendency = "Bajista"
        elif self.pricedata_stadistics.iloc[len(self.pricedata_stadistics) - 1]['y_pred'] > \
                self.pricedata_stadistics.iloc[1]['y_pred'] and \
                self.pricedata_stadistics.iloc[len(self.pricedata_stadistics) - 1]['y_pred'] > \
                self.pricedata_stadistics.iloc[1]['y_pred']:
            lv_Tendency = "Alcista"

        # MACD        ########################################################################
        exp1 = self.pricedata_stadistics['bidclose'].ewm(span=self.macdFast, adjust=False).mean()
        exp2 = self.pricedata_stadistics['bidclose'].ewm(span=self.macdSlow, adjust=False).mean()
        macd = exp1 - exp2
        self.pricedata_stadistics['macd'] = self.pricedata_stadistics.index.map(macd)
        self.pricedata_stadistics['signal'] = pd.DataFrame(
            self.pricedata_stadistics['macd'].ewm(span=self.macdSmooth, adjust=False).mean())
        self.pricedata_stadistics['hist'] = pd.DataFrame(
            self.pricedata_stadistics['macd'] - self.pricedata_stadistics['signal'])

        # Imprimir Precio/Indicador
        # self.logMessages = self.logMessages + "\n" + ("Precio Cierre: " + str(pricedata['bidclose'][len(pricedata) - 1]))

        # self.logMessages = self.logMessages + "\n" + ("Tendencia Regresion Lineal: " + lv_Tendency)
        lv_signal = self.pricedata_stadistics.iloc[len(self.pricedata_stadistics) - 1]['signal']
        self.logMessages = self.logMessages + "\n" + (
                "MACD Signal: " + str(lv_signal) + " SubValuacion:  " + str(self.macdsub) + " SobreValuacion:  " + str(
            self.macduper))

        self.logMessages = self.logMessages + "\n" + ("RSI " + str(iRSI[len(iRSI) - 1]))

        # data_per_d['lower_sto']
        if self.crossesOver(data_per_k, data_per_d) and lv_signal <= self.macdsub:
            self.logMessages = self.logMessages + "\n" + ("	 SEÑAL DE COMPRA ! \n")
            self.logMessages = self.logMessages + "\n" + ('''        
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
            self.logMessages = self.logMessages + "\n" + "	 SEÑAL DE COMPRA !"
            if self.countOpenTrades("S") > 0:
                self.logMessages = self.logMessages + "\n" + "	  Cerrando Ventas Abiertas..."
                self.exit("S")
            self.logMessages = self.logMessages + "\n" + "	  Abrir Operacion de Compra..."
            if self.countOpenTrades("B") == 0:
                self.enter("B")
                self.operacioncompra = True

        # Verifica el Cruce del SMA para Abajo.
        # if crossesUnder(data_per_d, 0.80):
        # if crossesUnder(pricedata_stadistics['signal'], 0.0004):
        if self.crossesUnder(data_per_k, data_per_d) and lv_signal >= self.macduper:
            self.logMessages = self.logMessages + "\n" + "	  SEÑAL DE VENTA ! \n"
            self.logMessages = self.logMessages + "\n" + ('''
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
            self.logMessages = self.logMessages + "\n" + "	  SEÑAL DE VENTA ! "
            if self.countOpenTrades("B") > 0:
                self.logMessages = self.logMessages + "\n" + "	  Cerrando Operacion de Compras..."
                self.exit("B")
            self.logMessages = self.logMessages + "\n" + "	  Abrir Operacion de Venta..."
            if self.countOpenTrades("S") == 0:
                self.enter("S")
                self.operacionventa = True

        # Cerrar Ventas #########################################
        if self.operacionventa and self.crossesOver(data_per_k, data_per_d):
            if self.countOpenTrades("S") > 0:
                self.logMessages = self.logMessages + "\n" + "	  Cerrando Ventas Abiertas..."
                self.operacionventa = False
                self.exit("S")

        # Cerrar Compras #########################################
        if self.operacioncompra and self.crossesUnder(data_per_k, data_per_d):
            if self.countOpenTrades("B") > 0:
                self.logMessages = self.logMessages + "\n" + "	  Cerrando Compras Abiertas..."
                self.operacioncompra = False
                self.exit("B")
        self.logMessages = self.logMessages + "\n" + (str(dt.datetime.now()) + " " + self.timeframe + "Verificacion "
                                                                                                      "Realizada.\n")


    def StrategyStart(self):
        self.Update()
        while True:
            currenttime = dt.datetime.now()
            if currenttime.second == 0:
                self.logMessages = self.logMessages + "\n" + (str(currenttime))
                print(str(currenttime) + " - " + self.symbol)

            if self.timeframe == "m1" and currenttime.second == 0:
                if self.getLatestPriceData():
                    self.Update()
                # if SHOWGUI == 0:
                # time.sleep(10)
            elif self.timeframe == "m5" and currenttime.second == 0 and currenttime.minute % 5 == 0:
                if self.getLatestPriceData():
                    self.Update()
                # if SHOWGUI == 0:
                #    time.sleep(240)
            elif self.timeframe == "m15" and currenttime.second == 0 and currenttime.minute % 15 == 0:
                if self.getLatestPriceData():
                    self.Update()
                # if SHOWGUI == 0:
                #    time.sleep(840)
            elif self.timeframe == "m30" and currenttime.second == 0 and currenttime.minute % 30 == 0:
                if self.getLatestPriceData():
                    self.Update()
                # if SHOWGUI == 0:
                #    time.sleep(1740)
            elif currenttime.second == 0 and currenttime.minute == 0:
                if self.getLatestPriceData():
                    self.Update()
                # if SHOWGUI == 0:
                #    time.sleep(3540)

            if currenttime.second == 0:
                a_file = open("Log_" + self.symbol.replace("/", "_") + ".txt", "a")
                a_file.write(self.logMessages + "\n")
                self.logMessages = ""

            if self.exitFlag:
                self.logMessages = self.logMessages + "\n" + "Cerrar"
                sys.exit()

            time.sleep(1)

    def run(self):
        self.logMessages = self.logMessages + "\n" + ("Preparate Start:" + self.symbol)
        self.Prepare()  # Perar la Estrategia
        self.StrategyStart()  # Iniciar la Estrategia
