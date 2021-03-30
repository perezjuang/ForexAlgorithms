# barajas.py
#############################
# Libraries
import random
import collections

# Valores de los palos
PALOS = ['espada', 'corazon', 'rombo', 'trebol']
# Valores de las cartas
VALORES = ['as', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'jota', 'reina', 'rey']
# Array de escalera real
ESCALERA_REAL = ['as', '10', 'jota', 'reina', 'rey']


# Creacion de la baraja con PALOS y VALORES
def crear_baraja():
    barajas = []
    for palo in PALOS:
        for valor in VALORES:
            barajas.append((palo, valor))
    return barajas


# Obtener mano
def obtener_mano(baraja, tamano_mano):
    # Random sample -> obtener valores sin repeticion
    mano = random.sample(baraja, tamano_mano)
    return mano


# Calcular probabilidades
def calc_prob(manos, intentos):
    ##########Combinaciones en el poker#########
    escalera_real = 0  # formada por un as, un rey, una reina, una jota y un diez, todos del mismo palo.
    escalera_de_colores = 0  # cinco cartas en orden numérico, todas del mismo palo.
    poker = 0  # cuatro cartas del mismo valor y una carta no emparda o "kicker".
    full = 0  # tres cartas del mismo valor y un par de un mismo valor diferente al anterior
    color = 0  # cinco cartas del mismo palo.
    escalera = 0  # cinco cartas consecutivas.
    trio = 0  # tres cartas del mismo valor y dos cartas no empardas.
    par = 0  # dos cartas del mismo valor
    ##########Combinaciones en el poker#########

    for mano in manos:
        ##########Logica#########
        mismo_palo = True
        valores = []
        palo = []
        # print(f'mano: {mano} ')
        for carta in mano:
            # print(f'carta: {carta}')
            # Dividir la mano en valores y palos
            valores.append(carta[1])
            palo.append(carta[0])
            ##Comprobar si pertenencen a un mismo palo
            if carta[0] == mano[0][0]:
                pass
            else:
                mismo_palo = False
        # print(f'valores: {valores} ')
        # print(f'palo: {palo} ')
        ##Ordenar valores ascendentes
        mano_ordenada = []
        for posicion in VALORES:
            for valor in valores:
                if posicion == valor:
                    mano_ordenada.append(posicion)
                else:
                    pass
        # print(f'Por orden : {mano_ordenada}')

        ##Obtener el valor de inicio de la escalera
        start = -1
        for valor in mano_ordenada:
            if start == -1:
                for i in range(len(VALORES)):
                    if valor == VALORES[i]:
                        start = i
                        break
            else:
                break
        # print(f'carta: {VALORES[start]}')
        # print(f'Posicion: {i}')

        ##Verificar si son valores concecutivos de la mano
        valores_concecutivos = 0
        concecutivo = True
        i = 0
        for valor in mano_ordenada:
            if concecutivo == True:
                for posicion in VALORES[start + i:]:
                    if valor == posicion:
                        valores_concecutivos += 1
                        i += 1
                        break
                    else:
                        concecutivo = False
                        break
            else:
                break
        # print(f'Valores concecutivos: {valores_concecutivos}')
        ##########Logica#########

        ##########Conteo de combinaciones#########
        if mismo_palo == True:
            # ESCALERA REAL
            if mano_ordenada == ESCALERA_REAL:
                escalera_real += 1
            # ESCALERA DE COLORES
            elif valores_concecutivos == 5:
                escalera_de_colores += 1
            # COLOR
            else:
                color += 1
        else:
            # ESCALERA
            if valores_concecutivos == 5:
                escalera += 1

        # Contar cartas del mismo valor
        counter = dict(collections.Counter(valores))
        # Verificar el full
        full_trio = False
        full_par = False
        # Contar las cartas
        for val in counter.values():
            # POKER
            if val == 4:
                poker += 1
                break
            # TRIO
            if val == 3:
                trio += 1
                full_trio = True
            # PAR
            if val == 2:
                par += 1
                full_par = True
        # FULL
        if full_trio and full_par:
            full += 1
            ##########Conteo de combinaciones#########

    ##########Mostrar apariciones#########
    print(f'Intentos: {intentos}')
    print(f'Probabilidad de escalera_real: {escalera_real / intentos}')
    print(f'Probabilidad de escalera_de_colores: {escalera_de_colores / intentos}')
    print(f'Probabilidad de poker: {poker / intentos}')
    print(f'Probabilidad de full: {full / intentos}')
    print(f'Probabilidad de color: {color / intentos}')
    print(f'Probabilidad de escalera: {escalera / intentos}')
    print(f'Probabilidad de trio: {trio / intentos}')
    print(f'Probabilidad de par: {par / intentos}')
    ##########Mostrar apariciones#########


# main
def main(tamano_mano, intentos):
    # Crear la baraja
    baraja = crear_baraja()
    # Crear array para guardar las manos que salgan
    manos = []
    # Obenter mano
    for _ in range(intentos):
        mano = obtener_mano(baraja, tamano_mano)
        manos.append(mano)
    # Calular probabilidades
    calc_prob(manos, intentos)


# entry point
if __name__ == '__main__':
    # Tamaño de mano
    tamano_mano = 5
    # Veces que se corre la simulacion
    intentos = 1000000
    # Llamar main
    main(tamano_mano, intentos)
