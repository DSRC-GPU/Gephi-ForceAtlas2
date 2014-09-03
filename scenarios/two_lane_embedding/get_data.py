#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np

from utils.data_reader import ProximityGraph, Fields

def plot(p1, p2):
    x = []
    y1 = []
    y2 = []
    time_intervals1 = p1.get_time_intervals()
    time_intervals2 = p2.get_time_intervals()
    for i in range(len(time_intervals1)):
        t1 = time_intervals1[i]
        t2 = time_intervals2[i]
        x.append(t1.end)
        y1.append(t1.success)
        y2.append(t2.success)
    print y1
    print y2

def main(argv=None):
    p1 = ProximityGraph("two_lane_12_0", False)
    p2 = ProximityGraph("two_lane_12_1", False)
    plot(p1, p2)

if __name__ == "__main__":
    main()
