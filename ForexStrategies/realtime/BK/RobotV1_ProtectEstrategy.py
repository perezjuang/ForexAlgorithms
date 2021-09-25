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

token = '25e7bb74fafe7aab29efd848d4d1f8b4e79bc483'

config = configparser.ConfigParser()
config.read('RobotV5.ini')
time_frame_operations = config['timeframe']
# Available periods : 'm1', 'm5', 'm15', 'm30', 'H1', 'H2', 'H3', 'H4', 'H6', 'H8','D1', 'W1', or 'M1'.
timeframe = time_frame_operations['timeframe']
amount = int(time_frame_operations['amount'])
stop = int(time_frame_operations['stop'])
limit = int(time_frame_operations['limit'])
trailing_step = int(time_frame_operations['trailing_step'])

# Global Variables
list_price_operations = None
con = fxcmpy.fxcmpy(access_token=token, log_level="error", log_file=None)

def StrategyHeartBeat():
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
        time.sleep(1)


def getLatestPriceData():
    global list_price_operations
    open_conexion = True
    while open_conexion:
        try:
            list_price_operations = con.get_open_positions(kind='list')
            return True
        except Exception as e:
            message_text = message_text + (
                    "\n1.An exception occurred Obtaining list operations Exception " + str(e))
            open_conexion = True
            time.sleep(120)


def Update():
    global list_price_operations
    print(str(dt.datetime.now()) + " Bar Closed - Running Update Function...")
    print(list_price_operations)



    for position in list_price_operations:
        print(position['grossPL'])
        print(position['tradeId'])
        id = position['tradeId']
        if position['grossPL'] <= 10:
            con.change_order_stop_limit(order_id=id, stop=-10)
            print("Position Actualizada " + str(position['tradeId']))
    print("=========================================================================\n")


def enter(BuySell, lvSymbol):
    direction = True
    if BuySell == "S":
        direction = False
    try:
        opentrade = con.open_trade(symbol=lvSymbol, is_buy=direction, amount=amount, time_in_force='GTC',
                                   order_type='AtMarket', is_in_pips=True, limit=limit, stop=stop,
                                   trailing_step=1)
    except:
        print("	  Error Opening Trade.")
    else:
        print("	  Trade Opened Successfully.")


def exit(BuySell=None, lvSymbol='none'):
    openpositions = con.get_open_positions(kind='list')
    isbuy = True
    if BuySell == "S":
        isbuy = False
    for position in openpositions:
        if position['currency'] == lvSymbol:
            if BuySell is None or position['isBuy'] == isbuy:
                print("	  Closing tradeID: " + position['tradeId'])
                try:
                    closetrade = con.close_trade(trade_id=position['tradeId'], amount=position['amountK'])
                except:
                    print("	  Error Closing Trade.")
                else:
                    print("	  Trade Closed Successfully.")


StrategyHeartBeat()  # Run strategy
