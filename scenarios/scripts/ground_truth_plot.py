#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
from mpl_toolkits.mplot3d import axes3d
import matplotlib.pyplot as plt
from igraph import Clustering, compare_communities
import numpy as np

from read_sim_data import *


def get_plot_data(i, time_intervals):
    t = time_intervals[i]
    ground_truth = Clustering([n.group for n in t.nodes])
    cc = Clustering([n.cc for n in t.nodes])
    return compare_communities(ground_truth, cc, method = "danon")


def plot(time_intervals):
    x = [t.end for t in time_intervals]
    data = [get_plot_data(i, time_intervals) for i in range(len(time_intervals))]
    line, = plt.plot(x, data, '-o')
    average = sum(data) / len(data)
    plt.ylim(0, 1.1)
    plt.title("Average clustering: " + str(average))
    plt.show()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    print(argv[1])
    plot(read_file(argv[1]))


if __name__ == "__main__":
    main()
