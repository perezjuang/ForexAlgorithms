import matplotlib.pyplot as plt

X1 = [1, 2, 3, 4, 5]
Y1 = [2, 4, 6, 8, 10]
plt.plot(X1, Y1, label="plot 1")
X2 = [1, 2, 3, 4, 5]
Y2 = [1, 4, 9, 16, 25]
plt.plot(X2, Y2, label="plot 2")
plt.xlabel('X-axis')
plt.ylabel('Y-axis')
plt.title('Two plots on the same graph')
plt.legend()
