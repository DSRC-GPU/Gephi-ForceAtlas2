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
    data_feeder = DataFeeder(p, Fields.VELOCITY_VECTOR, time_frame)
    velocity_vecs = ScatterPlot(data_feeder)
    AnimatedScatter([velocity_vecs]).show()

if __name__ == "__main__":
    main()
