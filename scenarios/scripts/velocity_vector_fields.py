#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np
from numpy import linalg, polyfit, poly1d, linspace
from scipy import stats

from utils.data_reader import ProximityGraph, Fields, ParamNames, Params
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

class VectoFieldsDataFeeder(DataFeeder):
    def __init__(self, pg, time_frame):
        super(VectoFieldsDataFeeder, self).__init__(pg, None, time_frame)

    def get_data(self, time_interval):
        colors = ('b', 'r', 'g', 'c', 'm', 'y', 'k')
        vecs = [n.vectors[Fields.VELOCITY_VECTOR] for n in time_interval.nodes]
        pos = [n.vectors[Fields.EMBEDDING_POS] for n in time_interval.nodes]
        vec_x = [v.x for v in vecs]
        vec_y = [v.y for v in vecs]
        x = [v.x for v in pos]
        y = [v.y for v in pos]
        c = [colors[n.get(Fields.GROUP)] for n in time_interval.nodes]
        return x, y, vec_x, vec_y, c

    def get_limits(self):
        maxX = maxY = float("-inf")
        minX = minY = float("inf")
        for t in self.time_intervals:
            x, y, _, _, _ = self.get_data(t)
            maxX = max(max(x), maxX)
            maxY = max(max(y), maxY)
            minX = min(min(x), minX)
            minY = min(min(y), minY)
        return [minX, maxX], [minY, maxY]

class VectorFieldPlot(ScatterPlot):
    def setup_plot(self):
        x, y, vec_x, vec_y, c = next(self.stream)
        ax = plt.gca()
        self.scat = ax.quiver(x, y, vec_x, vec_y, color=c, pivot="tail", linewidths=(0.2,) )
        return self.scat

    def update_plot(self):
        x, y, vec_x, vec_y, c = next(self.stream)
        new_data = np.array(zip(x, y))
        self.scat.set_offsets(new_data)
        self.scat.set_UVC(vec_x, vec_y)
        return self.scat



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
    vel = VectoFieldsDataFeeder(p, time_frame)
    plot = VectorFieldPlot(vel)
    an = AnimatedScatter([plot])
    plt.axis('off')
    plt.savefig('1.pdf', format='PDF')
    #an.show()

if __name__ == "__main__":
    main()
