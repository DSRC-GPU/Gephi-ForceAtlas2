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

    p = ProximityGraph(argv[1], False)
    data_feeder_fine = DataFeeder(p, Fields.FINE_SMOOTH_VEL_VECTOR, time_frame)
    data_feeder_coarse = DataFeeder(p, Fields.COARSE_SMOOTH_VEL_VECTOR, time_frame)
    vec_fine = ScatterPlot(data_feeder_fine, 'o')
    vec_coarse = ScatterPlot(data_feeder_coarse, '^')
    AnimatedScatter([vec_fine, vec_coarse]).show()

if __name__ == "__main__":
    main()
