#!/usr/bin/env python2.7

import sys
import math

import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np

from utils.data_reader import Fields

class DataFeeder(object):
    def __init__(self, pg, field, time_frame):
        self.field = field
        self.pg = pg
        self.time_intervals = pg.get_time_intervals()
        self.__set_start_iterator(time_frame)

    def __set_start_iterator(self, time_frame):
        if time_frame == None:
            self.frame = self.time_intervals[0]
        else:
            self.frame = self.pg.get_time_interval(time_frame)

        if not self.frame:
            raise Exception("Unknown time frame " + time_frame)
        self.idx = self.time_intervals.index(self.frame)

    def get_limits(self):
        maxX = maxY = float("-inf")
        minX = minY = float("inf")
        for t in self.time_intervals:
            x, y, _ = self.get_data(t)
            maxX = max(max(x), maxX)
            maxY = max(max(y), maxY)
            minX = min(min(x), minX)
            minY = min(min(y), minY)
        return [minX, maxX], [minY, maxY]

    def get_data(self, time_interval):
        colors = ('b', 'r', 'g', 'c', 'm', 'y', 'k')
        vecs = [n.vectors[self.field] for n in time_interval.nodes]
        x = [v.x for v in vecs]
        y = [v.y for v in vecs]
        c = [colors[n.get(Fields.GROUP)] for n in time_interval.nodes]
        return x, y, c

    def prev(self):
        if self.idx > 1:
            self.idx -= 2

    def __iter__(self):
        print "AA"
        while self.idx < len(self.time_intervals):
            self.frame = self.time_intervals[self.idx]
            self.idx += 1
            yield self.get_data(self.frame)

class ScatterPlot(object):
    def __init__(self, feeder, marker = 'o'):
        self.marker = marker
        self.feeder = feeder
        self.stream = iter(self.feeder)

    def get_frame(self):
        return self.feeder.frame

    def get_limits(self):
        return self.feeder.get_limits()

    def setup_plot(self):
        x, y, c = next(self.stream)
        self.scat = plt.scatter(x, y, c = c, marker = self.marker)
        return self.scat

    def prev_state(self):
        self.feeder.prev()
        self.stream = iter(self.feeder)

    def update_plot(self):
        x, y, _ = next(self.stream)
        new_data = np.array(zip(x, y))
        self.scat.set_offsets(new_data)
        return self.scat

class AnimatedScatter(object):

    def __init__(self, scatter_plots):
        self.pause = True
        self.fig, self.ax = plt.subplots()
        self.scatter_plots = scatter_plots
        self.fig.canvas.mpl_connect('button_press_event', self.on_click)
        self.set_limits()
        self.ani = animation.FuncAnimation(self.fig, self.update_plot, interval=5,
                                               init_func=self.setup_plot, blit=False)

    def set_limits(self):
        x = []
        y = []
        for s in self.scatter_plots:
            xlim, ylim = s.get_limits()
            x += xlim
            y += ylim
        minX, maxX = min(x), max(x)
        minY, maxY = min(y), max(y)
        plt.xlim(minX, maxX)
        plt.ylim(minY, maxY)

    def on_click(self, event):
        if event.button == 3:
            [s.prev_state() for s in self.scatter_plots]
        self.pause = False

    def setup_plot(self):
        self.plots = [s.setup_plot() for s in self.scatter_plots]
        return self.plots

    def update_plot(self, i):
        self.ax.set_title("Time Frame: %d" % self.scatter_plots[0].get_frame().start)
        if not self.pause:
            self.plots = [s.update_plot() for s in self.scatter_plots]
            self.pause = True
        return self.plots

    def show(self):
        plt.show()
