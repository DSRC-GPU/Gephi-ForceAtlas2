#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
from matplotlib.collections import LineCollection
import numpy as np

from utils.data_reader import ProximityGraph, Fields
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot
from matplotlib.mlab import PCA as mlabPCA
from sklearn.decomposition import PCA

class PcaDataFeeder(DataFeeder):
    def __init__(self, pg, time_frame):
        super(PcaDataFeeder, self).__init__(pg, Fields.VELOCITY_VECTOR, time_frame)
        self.eigen = None

    def get_limits(self):
        return [[-200, 200], [-200, 200]]

    def get_data(self, time_interval):
        x,y,c = super(PcaDataFeeder, self).get_data(time_interval)
        ax = plt.gca()

        velocities = np.array(zip(x,y))
        pca = PCA(n_components=1)
        pca.fit(velocities)
        w = pca.components_[0]
        line = [w * -100, w * 100]
        if not self.eigen:
            self.eigen = LineCollection([line], linewidths=2, color='k')
            ax.add_collection(self.eigen)
        else:
            self.eigen.set_segments([line])
        return x,y,c


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
    data_feeder = PcaDataFeeder(p, time_frame)
    velocity_vecs = ScatterPlot(data_feeder)
    AnimatedScatter([velocity_vecs]).show()

if __name__ == "__main__":
    main()
