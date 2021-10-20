from ZRobotOOP import ZRobotOOP
import fxcmpy
import configparser
import threading, time
import asyncio
import threading
import time

#########################################################
### PORTAFOLIO
#########################################################
from realtime.ZRobotOOP import ZRobotOOP

config = configparser.ConfigParser()
config.read('RobotV5.ini')
time_frame_operations = config['timeframe']
token = time_frame_operations['token']

open_conexion = True
con = None
while open_conexion:
        print("Opening Conection")
        con = fxcmpy.fxcmpy(access_token=token, server='demo', log_level="error", log_file=None)

        thread1 = ZRobotOOP("USD/JPY", con)
        thread1.start()
        thread2 = ZRobotOOP("AUD/JPY", con)
        thread2.start()
        thread3 = ZRobotOOP("AUD/USD", con)
        thread3.start()
        thread4 = ZRobotOOP("GBP/USD", con)
        thread4.start()
        thread5 = ZRobotOOP("EUR/JPY", con)
        thread5.start()
        #thread6 = ZRobotOOP("EUR/USD", con)
        #thread6.start()
        #thread7 = ZRobotOOP("NZD/USD", con)
        #thread7.start()
        #thread8 = ZRobotOOP("USD/CAD", con)
        #thread8.start()
        #thread9 = ZRobotOOP("NZD/JPY", con)
        #thread9.start()

        open_conexion2 = True
        while open_conexion2:
            print("***********************************************")
            print("Status Conexion:  " + con.connection_status)
            print("***********************************************")
            if con.connection_status == "aborted":
                thread1.exitFlag = True
                thread2.exitFlag = True
                thread3.exitFlag = True
                thread4.exitFlag = True
                #thread5.exitFlag = True
                #thread6.exitFlag = True
                #thread7.exitFlag = True
                #thread8.exitFlag = True
                #thread9.exitFlag = True
                open_conexion2 = False
            time.sleep(60)

        open_conexion = True








