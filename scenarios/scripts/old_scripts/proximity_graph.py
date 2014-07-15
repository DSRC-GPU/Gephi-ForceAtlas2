#!/usr/bin/env python2.7

import sys
import random

import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib.lines import Line2D

from read_sim_data import *

NO_EMBEDDING = True
old_lines = []
nodePlt = None

def set_random_nodes(time_intervals):
    random.seed(42)
    d = {}
    for i in range(len(time_intervals)):
        t = time_intervals[i]
        for n in t.nodes:
            coords = d.get(n.no, None)
            if coords:
                n.x = coords[0]
                n.y = coords[1]
            else:
                n.x = ((0.01 + random.random()) * 1000) - 500
                n.y = ((0.01 + random.random()) * 1000) - 500
                d[n.no] = (n.x, n.y)
            n.c = 'b' if n.group == 1 else 'r'



def set_plot_data2D(i, time_intervals):
    if NO_EMBEDDING:
        return

    t = time_intervals[i]
    velocity_vecs = [n.vectors[0] for n in t.nodes]
    for n in t.nodes:
        v = n.vectors[0]
        n.x = v.x
        n.y = v.y
        n.c = 'b' if n.group == 1 else 'r'

def get_limits2D(time_intervals):
    maxX = maxY = float("-inf")
    minX = minY = float("inf")
    for i in range(len(time_intervals)):
        set_plot_data2D(i, time_intervals)
        x, y, _ = get_node_data(time_intervals, i)
        maxX = max(max(x), maxX)
        maxY = max(max(y), maxY)
        minX = min(min(x), minX)
        minY = min(min(y), minY)
    return [(minX, maxX), (minY, maxY)]

def isEdgePresent(time_interval, e):
    return e in time_interval.edges

def plot_edge(time_interval, e):
    n1, n2 = time_interval.get_nodes(e)
    assert n1.no == e.src
    assert n2.no == e.dst
    p, =  plt.plot([n1.x, n2.x], [n1.y, n2.y], 'k-', lw = 0.2)
    p.e = e
    return p

def plot_edges(plt, time_intervals, i):
    global old_lines
    t = time_intervals[i]
    for item in old_lines:
        item.set_visible(False)

    old_lines = []
    for e in t.edges:
        p = plot_edge(t, e)
        old_lines.append(p)
    return old_lines

def get_node_data(time_intervals, i):
    nodes = time_intervals[i].nodes
    x = [n.x for n in nodes]
    y = [n.y for n in nodes]
    c = [n.c for n in nodes]
    return x, y, c

def plot_nodes(time_intervals, i):
    global nodePlt
    x, y, _ = get_node_data(time_intervals, i)
    data = np.array(zip(x,y))
    nodePlt.set_offsets(data)

def color_edges(time_intervals, i):
    t = time_intervals[i]
    for item in old_lines:
        if not isEdgePresent(t, item.e):
            item.set_color("r")
            item.set_linewidth(2)

    for e in t.edges:
        if not any([item.e == e for item in old_lines]):
            p = plot_edge(t, e)
            p.set_color("b")
            p.set_linewidth(2)
            old_lines.append(p)
    return old_lines

def plot(time_intervals):
    def update_plot(i, time_intervals):
        global nodePlt
        p = i / 2
        #print i
        if i % 2 == 0:
            artists = color_edges(time_intervals, p)
        else:
            plot_nodes(time_intervals, p)
            artists = plot_edges(plt, time_intervals, p)
        return artists + [nodePlt]

    def init():
        global nodePlt
        x, y, c = get_node_data(time_intervals, 0)
        nodePlt = plt.scatter(x, y, c = c, marker = 'o', s = 17)
        artists = plot_edges(plt, time_intervals, 0)
        t = time_intervals[0]
        return artists + [nodePlt]

    fig = plt.figure()

    if NO_EMBEDDING:
        set_random_nodes(time_intervals)
    xlim, ylim = get_limits2D(time_intervals)
    plt.xlim(xlim)
    plt.ylim(ylim)


    print len(time_intervals)
    anim = animation.FuncAnimation(fig, update_plot, init_func = init, frames=len(time_intervals),
                                   fargs=(time_intervals,), blit=False, interval=100)

    #anim.save('random.ogg', fps = 5, extra_args=['-vcodec', 'libx264'], dpi = 170)
    plt.show()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    print(argv[1])
    plot(read_file(argv[1]))

if __name__ == "__main__":
    main()
