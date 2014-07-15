#!/usr/bin/env python2.7

import sys
import math
from collections import defaultdict

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np
from numpy import linalg, polyfit, poly1d, linspace
from scipy import stats

from utils.data_reader import ProximityGraph, Fields, ParamNames, Params
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

global window_size

class DataFeederDist(DataFeeder):
    def __init__(self, pg, time_frame):
        super(DataFeederDist, self).__init__(pg, Fields.EMBEDDING_POS, time_frame)
        self.rolling_window = []

    def get_limits(self):
        maxX = maxY = float("-inf")
        minX = minY = float("inf")
        for t in self.time_intervals:
            vecs = [n.vectors[self.field] for n in t.nodes]
            x = [v.x for v in vecs]
            y = [v.y for v in vecs]
            maxX = max(max(x), maxX)
            maxY = max(max(y), maxY)
            minX = min(min(x), minX)
            minY = min(min(y), minY)
        return [minX, maxX], [minY, maxY]

    def get_data(self, time_interval):
        if len(self.rolling_window) == window_size:
            self.rolling_window.pop()

        n = time_interval.get_node(70)
        vec = n.vectors[self.field]
        self.rolling_window.insert(0, vec)

        x = [v.x for v in self.rolling_window]
        y = [v.y for v in self.rolling_window]
        c = ['b'] * len(x)
        '''
        if len(x) > 2:
            z5 = polyfit(x, y, 5)
            p5 = poly1d(z5)
            xx = linspace(-3000, 0, 100)
            plt.plot(xx, p5(xx),'-b')
            slope, intercept, r_value, p_value, std_err = stats.linregress(x, y)
            line = slope * np.array(x) + intercept
            plt.plot(x, line, 'r-')
        '''
        return x, y, c


def compute_distances(pg):
    def dist(a, b):
        return math.sqrt((a.x - b.x) ** 2 + (a.y - b.y) ** 2)

    nodes = defaultdict(int)
    intervals = pg.get_time_intervals()
    for prev, crr in zip(intervals, intervals[1:]):
        for n in crr.nodes:
            n_id = n.get(Fields.ID)
            print n_id
            prev_n = prev.get_node(n_id)

            crr_embedding = n.vectors[Fields.EMBEDDING_POS]
            prev_embedding = prev_n.vectors[Fields.EMBEDDING_POS]
            nodes[n_id] += dist(crr_embedding, prev_embedding)

def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s results_dir [time_frame]"
        sys.exit(1)

    time_frame = None
    if len(argv) == 3:
        time_frame = int(argv[2])

    #compute_distances(p)
    p = ProximityGraph(argv[1], False)
    data_feeder = DataFeederDist(p, time_frame)
    embedding = ScatterPlot(data_feeder)

    global window_size
    window_size = int(Params(argv[1]).get(ParamNames.RollingWindowSize))

    AnimatedScatter([embedding]).show()

if __name__ == "__main__":
    main()
