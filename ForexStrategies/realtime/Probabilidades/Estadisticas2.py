import random
import statistics

if __name__ == '__main__':

    edades = [random.randint(1,35) for i in range (20)]
    print(edades)
    print(statistics.mean(edades))