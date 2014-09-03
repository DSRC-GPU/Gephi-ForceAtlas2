#!/usr/bin/env python2.7

import sys
import math
from collections import defaultdict
from itertools import combinations, product

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np
from scipy import spatial

from utils.data_reader import ProximityGraph, Fields
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

def get_inner_group_cs(groups):
    inner_cs = {}
    for key in groups.keys():
        cs_group = []
        for n1, n2 in combinations(groups[key], 2):
            v1 = n1.vectors[Fields.VELOCITY_VECTOR]
            v2 = n2.vectors[Fields.VELOCITY_VECTOR]
            cs = 1 - spatial.distance.cosine([v1.x, v1.y], [v2.x, v2.y])
            cs_group.append(cs)
        mean, std = np.mean(cs_group), np.std(cs_group)
        inner_cs[key] = (mean, std)
    return inner_cs

def get_other_groups(groups, gr):
    others = []
    for k,v in groups.items():
        if k == gr:
            continue
        others += v
    return others

def get_inter_group_cs(groups):
    inter_cs = {}
    for k,v in groups.items():
        cs_group = []
        others = get_other_groups(groups, k)
        for n1, n2 in product(v, others):
            v1 = n1.vectors[Fields.VELOCITY_VECTOR]
            v2 = n2.vectors[Fields.VELOCITY_VECTOR]
            cs = 1 - spatial.distance.cosine([v1.x, v1.y], [v2.x, v2.y])
            cs_group.append(cs)
        mean, std = np.mean(cs_group), np.std(cs_group)
        inter_cs[k] = (mean, std)
    return inter_cs

def get_groups(time_interval):
    groups = defaultdict(list)
    for n in time_interval.nodes:
        gr = n.get(Fields.GROUP)
        groups[gr].append(n)
    return groups

def get_meet_depart_time(pg):
    first_edge = None
    last_edge = None
    for t in pg.time_intervals:
        if first_edge == None:
            for e in t.edges:
                src, dst = t.get_nodes(e)
                if src.get(Fields.GROUP) != dst.get(Fields.GROUP):
                    first_edge = t.end
        elif last_edge == None:
            for e in t.edges:
                src, dst = t.get_nodes(e)
                if src.get(Fields.GROUP) != dst.get(Fields.GROUP):
                    break
            else:
                last_edge = t.end

    print first_edge, last_edge



def cosine_plot(pg):
    name = 'tl'
    #name = pg.name.split('/')[-2]

    inner_group_mean = defaultdict(list)
    inner_group_std = defaultdict(list)
    inter_group_mean = defaultdict(list)
    inter_group_std = defaultdict(list)
    x = []
    time_intervals = pg.get_time_intervals()
    groups = get_groups(time_intervals[0]).keys()
    print groups

    for t in time_intervals:
        g = get_groups(t)
        x.append(t.end)
        for k,v in get_inner_group_cs(g).items():
            mean, std = v
            inner_group_mean[k].append(mean)
            inner_group_std[k].append(std / 2)
        for k,v in get_inter_group_cs(g).items():
            mean, std = v
            inter_group_mean[k].append(mean)
            inter_group_std[k].append(std / 2)

    _, axes = plt.subplots(len(groups), sharex=True, sharey=True)
    for g in groups:
        ax = axes[g - 1]
        ax.set_title('Group %d' % g)
        inner = ax.errorbar(x, inner_group_mean[g], yerr=inner_group_std[g], fmt="b-o")
        inter = ax.errorbar(x, inter_group_mean[g], yerr=inter_group_std[g], fmt="r-o")
        ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)
        ax.xaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)
        plt.figlegend( (inner, inter), ('Inner Group Similarity', 'Inter Group Similarity'), 'upper center')
        ax.axvline(x=25, ls='-', color='k', linewidth=4)
        ax.axvline(x=225, ls='-', color='k', linewidth=4)

    # two lane 25.0 225.0
    # three lanes 55.0 270.0
    # crossing 30.0 440.0

    plt.xlabel('Time')
    plt.ylabel('Cosine Similarity')
    plt.ylim(-1.1, 1.1)
    plt.savefig('cs_%s.pdf' % name, format='PDF')
    #plt.show()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s results_dir [time_frame]"
        sys.exit(1)

    time_frame = None
    if len(argv) == 3:
        time_frame = int(argv[2])

    print argv[1]
    p = ProximityGraph(argv[1], True)
    cosine_plot(p)
    #get_meet_depart_time(p)

if __name__ == "__main__":
    main()
