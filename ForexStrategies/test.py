import numpy as np
import matplotlib.pyplot as plt
import math

plt.axis("equal")

vx = np.array([0, 1, 5])
vy = np.array([0, 1, 5])

plt.plot(vx, vy, color='green', linestyle='--')

vxLine = vx
vyLine = []
for ly in vy:
    vyLine.append(vy[0])

plt.plot(vxLine, vyLine, color='green', linestyle='--')
plt.xlabel('x')
plt.ylabel('y')


x1 = vx[0]
y1 = vy[0]

x2 = vx[-1]
y2 = vy[-1]

x = x2 - x1
y = y2 - y1
angle = math.atan2(y, x) * (180.0 / math.pi)

# create circle
c = plt.Circle((x1, y1), radius=0.1, color='red', alpha=.3)
plt.gca().add_artist(c)

plt.text(x1, y1, str(angle) + '°')
plt.show()
