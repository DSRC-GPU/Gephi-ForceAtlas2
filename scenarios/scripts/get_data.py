#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np
from igraph import compare_communities

from utils.data_reader import ProximityGraph, Fields

def get_data(p):
    time_intervals = p.get_time_intervals()
    x = []
    y = []
    for t in time_intervals:
        x.append(t.end)
        y.append(t.success / 100)

        gt_ids = []
        pl_ids = []
        for n in t.nodes:
            gt_ids.append(n.get(Fields.GROUP))
            pl_ids.append(n.get(Fields.CC))
        assert (compare_communities(gt_ids, pl_ids, method="danon") - (t.success / 100)) < 0.001
    return x, y

def plot():
    from matplotlib.font_manager import FontProperties
    fontP = FontProperties()
    fontP.set_size('small')

    fig, axes = plt.subplots(5, sharex=True, sharey=True)
    fig.tight_layout()
    p1 = ProximityGraph("two_lane_12_0", False)
    x1, y1 = get_data(p1)
    ax1 = axes[3]
    a1, = ax1.plot(x1, y1, 'b-o', zorder=10)
    ax1.set_title('Average Accuracy 0.90689')

    p2 = ProximityGraph("two_lane_12_1", False)
    x2, y2 = get_data(p2)
    ax2 = axes[1]
    a2, = ax2.plot(x2, y2, 'r-o', zorder = 10)
    ax2.set_title('Average Accuracy 0.93514')

    p3 = ProximityGraph("two_lane_12_2", False)
    x3, y3 = get_data(p3)
    ax3 = axes[2]
    a3, = ax3.plot(x3, y3, 'y-o', zorder=10)
    ax3.set_title('Average Accuracy 0.93741')

    p4 = ProximityGraph("two_lane_12_3", False)
    x4, y4 = get_data(p4)
    ax4 = axes[0]
    a4, = ax4.plot(x4, y4, 'g-o', zorder=10)
    ax4.set_title('Average Accuracy 0.93514')

    p5 = ProximityGraph("two_lane_12_4", False)
    x5, y5 = get_data(p5)
    ax5 = axes[4]
    a5, = ax5.plot(x5, y5, 'k-o', zorder=10)
    ax5.set_title('Average Accuracy 0.78860')

    for ax in axes:
        start, end = ax.get_xlim()
        plt.xlim(start, end)
        ax.xaxis.set_ticks(np.arange(start, end, 25))
        ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)
        ax.xaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)

    plt.ylim(0, 1.1)
    #plt.figlegend( (a4, a2, a3, a1, a5), ('Unbounded', '25', '15', '10', '1'), 'upper center')
    plt.legend( (a4, a2, a3, a1, a5), ('Unbounded', '25', '15', '10', '1'), loc = 'upper center', bbox_to_anchor = (0.40,6.0,1,1), prop=fontP, ncol=5)

    plt.xlabel('Time')
    plt.ylabel('Accuracy')
    plt.show()
    #plt.savefig('param_sweep_rw.pdf', format='PDF')

def main(argv=None):
    plot()

if __name__ == "__main__":
    main()
