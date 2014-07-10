#!/usr/bin/env python2.7

import sys
import random

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib.lines import Line2D

from read_sim_data import *

time_val = 0
sc1 = None
sc2 = None
rand_y = np.random.uniform(low=0,high=1000,size=1000)
colors = { 1:'b', 2:'r', 3:'y'}

def get_node_data_fine(time_intervals, i):
    nodes  = time_intervals[i].nodes
    x = [n.vectors[3].x for n in nodes]
    y = [0] * len(x)
    c = [colors[n.group] for n in nodes]
    return x, y, c


def get_node_data_coarse(time_intervals, i):
    nodes  = time_intervals[i].nodes
    x = [n.vectors[3].y for n in nodes]
    y = [0] * len(x)
    c = [colors[n.group] for n in nodes]
    return x, y, c

def plot(time_intervals):

    def update_plot(i, time_intervals):
        global sc1
        global sc2
        global rand_y
        x, _, _ = get_node_data_fine(time_intervals, i)
        y = rand_y[0:len(x)]
        sc1.set_offsets(np.array(zip(x,y)))
        x, _, _ = get_node_data_coarse(time_intervals, i)
        sc2.set_offsets(np.array(zip(x,y)))
        return sc1, sc2

    def init():
        global sc1
        global sc2
        global rand_y
        global time_val
        print time_val

        x, _, c = get_node_data_fine(time_intervals, time_val)
        y = rand_y[0:len(x)]
        sc1 = plt.scatter(x, y, c = c, marker = 'o', s = 35)

        x, _, c = get_node_data_coarse(time_intervals, time_val)
        y = rand_y[len(x):2*len(x)]
        sc2 = plt.scatter(x, y, c = c, marker = '^', s = 35)
        '''
        labels = ['{0}'.format(i) for i in range(len(x))]
        for l,x,y in zip(labels, x , y):
            plt.annotate(l, xy = (x, y))
        '''
        return sc1, sc2

    fig = plt.figure()
    #init()
    anim = animation.FuncAnimation(fig, update_plot, init_func = init, frames=len(time_intervals),
                                   fargs=(time_intervals,), blit=False, interval=100)
    plt.show()

def main(argv=None):
    global time_val
    if argv is None:
        argv = sys.argv

    print(argv[1])
    time_val = int(argv[2])
    plot(read_file(argv[1]))

if __name__ == "__main__":
    main()
