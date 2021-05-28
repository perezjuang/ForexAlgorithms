import configparser
import os

import fxcmpy
import numpy as np
import datetime as dt

#"con = fxcmpy.fxcmpy(config_file='../fxcm.cfg')
#con = fxcmpy.fxcmpy(access_token ='25e7bb74fafe7aab29efd848d4d1f8b4e79bc483', log_level='error',log_file=None)
con = fxcmpy.fxcmpy(access_token='d1a05f623d2a396ec7f7ce33ee90721f0acdf456', log_level='debug', server='demo', log_file='log.txt')
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

numberofcandles = int(time_frame_operations['numberofcandles'])

print(con.get_subscribed_symbols())

def print_price(symbol):
    df = con.get_candles(symbol, period=timeframe, number=numberofcandles)
    filename = symbol.replace("/", "-")
    df.to_csv(filename + '.csv', index=False, header=True)

    # df = pd.read_csv('historical_data.csv', index_col = 0)


if __name__ == '__main__':
    print_price('GBP/JPY')
