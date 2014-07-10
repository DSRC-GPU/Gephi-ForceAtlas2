#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
from mpl_toolkits.mplot3d import axes3d
import matplotlib.pyplot as plt
import numpy as np

from read_sim_data import *

colors = { 1:'b', 2:'r', 3:'y'}
initial_time_interval = None


def get_limits3D(time_intervals, idx):
    maxX = maxY = maxZ = float("-inf")
    minX = minY = minZ = float("inf")
    for i in range(len(time_intervals)):
        x,y,z,c = get_plot_data3D(i, time_intervals, idx)
        maxX = max(max(x), maxX)
        maxY = max(max(y), maxY)
        maxZ = max(max(z), maxZ)
        minX = min(min(x), minX)
        minY = min(min(y), minY)
        minZ = min(min(z), minZ)
    return [(minX, maxX), (minY, maxY), (minZ, maxZ)]


def get_limits2D(time_intervals, idx):
    maxX = maxY = float("-inf")
    minX = minY = float("inf")
    for i in range(len(time_intervals)):
        x,y,c = get_plot_data2D(i, time_intervals, idx)
        maxX = max(max(x), maxX)
        maxY = max(max(y), maxY)
        minX = min(min(x), minX)
        minY = min(min(y), minY)
    return [(minX, maxX), (minY, maxY)]


def get_plot_data3D(i, time_intervals, idx):
    t = time_intervals[i]
    coords =        [n.vectors[ORIGINAL_POS] for n in t.nodes]
    velocity_vecs = [n.vectors[idx] for n in t.nodes]
    c = [colors[n.group] for n in t.nodes]
    x = [v.x for v in coords]
    y = [coords[i].y + velocity_vecs[i].x for i in range(len(coords))]
    z = [v.y for v in velocity_vecs]
    return (x, y, z, c)


def get_plot_data2D(i, time_intervals, idx):
    t = time_intervals[i]
    velocity_vecs = [n.vectors[idx] for n in t.nodes]
    x = [v.x for v in velocity_vecs]
    y = [v.y for v in velocity_vecs]
    c = [colors[n.group] for n in t.nodes]
    return (x, y, c)


def plot3(time_intervals):
    global initial_time_interval

    fig = plt.figure()
    ax1 = fig.add_subplot(111)

    xlim1, ylim1 = get_limits2D(time_intervals, VELOCITY_VEC_PHI1)
    xlim2, ylim2 = get_limits2D(time_intervals, VELOCITY_VEC_PHI2)
    minX = min(xlim1[0], xlim2[0])
    maxX = max(xlim1[1], xlim2[1])
    minY = min(ylim1[0], ylim2[0])
    maxY = max(ylim1[1], ylim2[1])

    ax1.set_xlim(minX, maxX)
    ax1.set_ylim(minY, maxY)


    def update_plot(i, time_intervals, ax1):
        x1, y1, c1 = get_plot_data2D(i, time_intervals, VELOCITY_VEC_PHI1)
        x2, y2, c2 = get_plot_data2D(i, time_intervals, VELOCITY_VEC_PHI2)
        sc1 = ax1.scatter(x1, y1, c = c1, marker = 'o', animated = True)
        sc2 = ax1.scatter(x2, y2, c = c2, marker = '^', animated = True)
        return sc1, sc2

    def init():
        x1, y1, c1 = get_plot_data2D(initial_time_interval, time_intervals, VELOCITY_VEC_PHI1)
        x2, y2, c2 = get_plot_data2D(initial_time_interval, time_intervals, VELOCITY_VEC_PHI2)
        sc1 = ax1.scatter(x1, y1, c = c1 , marker = 'o')
        sc2 = ax1.scatter(x2, y2, c = c2 , marker = '^')

    if initial_time_interval == None:
        initial_time_interval = 0
        anim = animation.FuncAnimation(fig, update_plot, init_func = init, frames=len(time_intervals),
                                       fargs=(time_intervals, ax1), blit=True, interval=100)
    else:
        init()

    plt.show()


def plot2(time_intervals):
    def update_plot(i, time_intervals, scat):
        x,y,z,c = get_plot_data3D(i, time_intervals, VELOCITY_VEC_PHI1)
        data = ((np.array(x), np.array(y), np.array(z)))
        scat._offsets3d = data
        return scat,

    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    x,y,z,c = get_plot_data(0, time_intervals)

    xlim, ylim, zlim = get_limits3D(time_intervals, VELOCITY_VEC_PHI1)
    ax.set_xlim3d(xlim)
    ax.set_ylim3d(ylim)
    ax.set_zlim3d(zlim)
    scat = ax.scatter(x, y, z, c=c)

    anim = animation.FuncAnimation(fig, update_plot, frames=len(time_intervals),
                                   fargs=(time_intervals, scat), blit=False, interval=100)

    #anim.save('animation.mp4', fps=30)
    plt.show()


def plot(time_intervals):
    global initial_time_interval

    '''
    X, Y = np.meshgrid(x, y)
    zs = np.array([fun(x,y) for x,y in zip(np.ravel(X), np.ravel(Y))])
    Z = zs.reshape(X.shape)
    ax.contour(np.array(x), np.array(y), np.array(z))
    X, Y, Z = axes3d.get_test_data(0.05)
    ax.plot_wireframe(X, Y, Z, rstride=10, cstride=10)
    '''

    def update_plot(i, time_intervals, scat):
        x,y,z,c = get_plot_data(i, time_intervals)
        coords = np.array([[x[i], y[i]] for i in range(len(x))])
        scat.set_offsets(coords)
        scat.set_array(np.array(c))
        scat._sizes = z
        return scat,

    def init():
        x,y,z,c = get_plot_data(0, initial_time_interval)
        scat = plt.scatter(x,y, c=c, s=z)

    fig = plt.figure()
    xlim, ylim = get_limits(time_intervals)
    plt.xlim(xlim)
    plt.ylim(ylim)

    anim = animation.FuncAnimation(fig, update_plot, init_func = init, frames=len(time_intervals),
                                  fargs=(time_intervals, scat), blit=False, interval=100)

    #anim.save('animation.mp4', fps=30)
    plt.show()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 3:
        global initial_time_interval
        initial_time_interval = int(argv[2])

    print(argv[1])
    plot3(read_file(argv[1]))


if __name__ == "__main__":
    main()
