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

start = dt.datetime(2017, 7, 15)
stop = dt.datetime(2017, 8, 1)
pricedata = con.get_candles('EUR/USD', period='D1', start=start, stop=stop)
#pricedata = con.get_candles(symbol, period=timeframe, number=numberofcandles)


print(pricedata)
print("Precios Iniciales Recibidos")

breakpoint()

data_per_k = per_k(pricedata['bidclose'], 14)
data_per_d = per_d(pricedata['bidclose'], 3)

print(data_per_k)