#!/usr/bin/env python2
# a bar plot with errorbars
import numpy as np
import matplotlib.pyplot as plt

N = 3
runningTime = (210, 235, 200)
avgRunningTime= (210.0 / 49.0, 235.0 / 56.0, 200.0 / 49.0)

ind = np.arange(N)  # the x locations for the groups
width = 0.35       # the width of the bars

fig, ax = plt.subplots()
rects1 = ax.bar(ind, runningTime, width, color='r')

rects2 = ax.bar(ind+width, avgRunningTime, width, color='y')

# add some
ax.set_ylabel('Running Time (s)')
ax.set_xticks(ind+width)
ax.set_xticklabels( ('Two lanes', 'Three lanes', 'Two lanes crossing') )

ax.legend( (rects1[0], rects2[0]), ('Total Running Time', 'Average running time per pipeline step'), loc='upper center', bbox_to_anchor=(0.5, 1.13) )

def autolabel(rects):
    # attach some text labels
    for rect in rects:
        height = rect.get_height()
        ax.text(rect.get_x()+rect.get_width()/2., 1.001*height, '%d'%int(height),
                ha='center', va='bottom')

autolabel(rects1)
autolabel(rects2)

plt.savefig('time.pdf', format='PDF')
