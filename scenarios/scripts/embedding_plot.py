#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np

from utils.data_reader import ProximityGraph, Fields
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s results_dir [time_frame]"
        sys.exit(1)

    time_frame = None
    if len(argv) == 3:
        time_frame = int(argv[2])

    p = ProximityGraph(argv[1], True)
    data_feeder = DataFeeder(p, Fields.EMBEDDING_POS, time_frame)
    embedding = ScatterPlot(data_feeder)
    an = AnimatedScatter([embedding])
    #plt.axis('off')
    an.show()
    #an.get_animation().save('embedding_two_lanes_9.mov', fps = 5, extra_args=['-vcodec', 'libx264'], dpi = 170)

if __name__ == "__main__":
    main()
