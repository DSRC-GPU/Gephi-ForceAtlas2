#!/usr/bin/env python2.7


import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np

from utils.data_reader import ProximityGraph, Fields
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

rand_y = np.random.uniform(low=0, high=1000, size=1000)

class DataFeederPCA(DataFeeder):
    def get_data(self, time_interval):
        global rand_y
        colors = ('b', 'r', 'g', 'c', 'm', 'y', 'k')
        x = [n.get(self.field) for n in time_interval.nodes]
        y = rand_y[0:len(x)]
        c = [colors[n.get_attr(Fields.GROUP)] for n in time_interval.nodes]
        return x, y, c

def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s results_dir [time_frame]"
        sys.exit(1)

    time_frame = None
    if len(argv) == 3:
        time_frame = int(argv[2])

    p = ProximityGraph(argv[1], False)
    data_feeder_fine = DataFeederPCA(p, Fields.FINE_PCA, time_frame)
    data_feeder_coarse = DataFeederPCA(p, Fields.COARSE_PCA, time_frame)
    vec_fine = ScatterPlot(data_feeder_fine, 'o')
    vec_coarse = ScatterPlot(data_feeder_coarse, '^')
    AnimatedScatter([vec_fine, vec_coarse]).show()

if __name__ == "__main__":
    main()
