import random
from bokeh.plotting import figure,show


def got_sequences(throws):
    sequence = []

    for _ in range(throws):
        option_got = random.choice([1, 2, 3, 4, 5, 6])
        sequence.append(option_got)
    return sequence



def graph(sim,prob,sum_to_reach,throws):
    plot = figure(title=print(f'Probability of having {sum_to_reach} in {throws} throws'),
                    x_axis_label = "Attempts",
                    y_axis_label = "Probability")
    plot.line(sim,prob)
    show(plot)



def main(throws, sum_to_reach, number_of_tries):
    favorable_cases = 0
    simulations = []
    probabilities = []
    j = 1
    ranges_of_simulations= range(1,number_of_tries,100)
    for r in range(number_of_tries):
        i=0
        for i in range(throws):
            throw_sequences_1 = got_sequences(throws)
            throw_sequences_2 = got_sequences(throws)
            if (throw_sequences_1[i]+throw_sequences_2[i]) == sum_to_reach:
                favorable_cases += 1
                break
            else:
                i = +1
        probability = favorable_cases/number_of_tries
        if r in ranges_of_simulations:
            simulations.append(r)
            probabilities.append(probability)
    print(f'La probabilidad de tener el numero 12 en {number_of_tries} intentos es de {probability}')
    graph(simulations,probabilities,sum_to_reach,throws)

if __name__ == "__main__":
    throws = int(input("How many throws do you want?"))
    sum_to_reach = int(input("What's goind to be the sum?"))
    number_of_tries = int(input("How many tries do you want?"))
    main(throws, sum_to_reach, number_of_tries)