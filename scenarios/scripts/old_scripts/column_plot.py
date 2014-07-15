#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
from mpl_toolkits.mplot3d import axes3d
import matplotlib.pyplot as plt
import numpy as np

from read_sim_data import *

colors = { 1:'b', 2:'r', 3:'y'}

def get_plot_data2D(i, time_intervals, idx):
    t = time_intervals[i]
    velocity_vecs = [n.vectors[idx] for n in t.nodes]
    x = [v.x for v in velocity_vecs]
    y = [v.y for v in velocity_vecs]
    c = [colors[n.group] for n in t.nodes]
    return (x, y, c)


def get_limits2D(time_intervals, idx):
    maxX = maxY = float("-inf")
    minX = minY = float("inf")
    for i in range(len(time_intervals)):
        x,y,c = get_plot_data2D(i, time_intervals, idx)
        maxX = max(max(x), maxX)
        maxY = max(max(y), maxY)
        minX = min(min(x), minX)
        minY = min(min(y), minY)
    return [(minX, maxX), (minY, maxY)]


def plot(time_intervals, data_idx):
    def update_plot(i, time_intervals, plt):
        x, y, c = get_plot_data2D(i, time_intervals, data_idx)
        '''
        maxX = max(x)
        minX = min(x)
        maxY = max(y)
        minY = min(y)
        plt.xlim(minX, maxX)
        plt.ylim(minY, maxY)
        '''

        scat = plt.scatter(x, y, c = c, marker = 'o', animated = True)
        return scat,

    x, y, c = get_plot_data2D(0, time_intervals, data_idx)
    xlim, ylim = get_limits2D(time_intervals, data_idx)

    fig = plt.figure()
    scat = plt.scatter(x, y, c = c, marker = 'o', animated = True)
    #scat = plt.scatter(x, y, c = c, marker = 'o')

    #plt.xlim(xlim)
    #plt.ylim(ylim)

    anim = animation.FuncAnimation(fig, update_plot, frames=len(time_intervals),
                                   fargs=(time_intervals, plt), blit=True, interval=100)
    plt.show()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    print(argv[1])
    plot(read_file(argv[1]), int(argv[2]))


if __name__ == "__main__":
    main()
