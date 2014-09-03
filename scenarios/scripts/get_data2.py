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

    fig, axes = plt.subplots(3, sharex=True, sharey=True)
    fig.tight_layout()
    p1 = ProximityGraph("two_lane_12_0", False)
    x1, y1 = get_data(p1)
    ax1 = axes[0]
    a1, = ax1.plot(x1, y1, 'b-o', zorder=10)
    ax1.set_title('Average Accuracy 0.93622')

    p2 = ProximityGraph("two_lane_12_1", False)
    x2, y2 = get_data(p2)
    ax2 = axes[1]
    a2, = ax2.plot(x2, y2, 'r-o', zorder = 10)
    ax2.set_title('Average Accuracy 0.93595')

    p3 = ProximityGraph("two_lane_12_2", False)
    x3, y3 = get_data(p3)
    ax3 = axes[2]
    a3, = ax3.plot(x3, y3, 'y-o', zorder=10)
    ax3.set_title('Average Accuracy 0.93514')

    for ax in axes:
        start, end = ax.get_xlim()
        plt.xlim(start, end)
        ax.xaxis.set_ticks(np.arange(start, end, 25))
        ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)
        ax.xaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)

    plt.ylim(0, 1.1)
    #plt.figlegend( (a4, a2, a3, a1, a5), ('Unbounded', '25', '15', '10', '1'), 'upper center')
    plt.legend( (a1, a2, a3), ('Edge lifespan ratio', 'Angular Similarity', 'No weighting'), loc = 'upper center', bbox_to_anchor = (0.3,2.6,1,1), prop=fontP, ncol=3)

    plt.xlabel('Time')
    plt.ylabel('Accuracy')
    plt.show()
    #plt.savefig('param_sweep_rw.pdf', format='PDF')

def main(argv=None):
    plot()

if __name__ == "__main__":
    main()
