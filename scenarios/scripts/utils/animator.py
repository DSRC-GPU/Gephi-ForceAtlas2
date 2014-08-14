#!/usr/bin/env python2.7

import sys
import math

from pylab import *
import matplotlib.animation as animation
from matplotlib.collections import LineCollection
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
            raise Exception("Unknown time frame " + str(time_frame))
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

    def get_current_edges(self):
        if not self.pg.read_edges:
            return []
        lines = []
        for e in self.frame.edges:
            line = []
            src, dst = self.frame.get_nodes(e)
            line.append(src.vectors[self.field].get())
            line.append(dst.vectors[self.field].get())
            lines.append(line)
        return lines

    def __iter__(self):
        while self.idx < len(self.time_intervals):
            self.frame = self.time_intervals[self.idx]
            self.idx += 1
            yield self.get_data(self.frame)

class ScatterPlot(object):
    def __init__(self, feeder, marker = 'o'):
        self.marker = marker
        self.feeder = feeder
        self.stream = iter(self.feeder)

    def get_frames_len(self):
        return len(self.feeder.time_intervals) - 1

    def get_frame(self):
        return self.feeder.frame

    def get_limits(self):
        return self.feeder.get_limits()

    def setup_plot(self):
        x, y, c = next(self.stream)
        ax = plt.gca()
        self.scat = ax.scatter(x, y, c = c, marker = self.marker, s = 25)
        return self.scat

    def setup_plot_edges(self):
        lines = self.feeder.get_current_edges()
        ax = plt.gca()
        self.lines = LineCollection(lines, linewidths=0.1)
        ax.add_collection(self.lines)

    def update_edges(self):
        lines = self.feeder.get_current_edges()
        self.lines.set_segments(lines)

    def prev_state(self):
        self.feeder.prev()
        self.stream = iter(self.feeder)

    def update_plot(self):
        x, y, _ = next(self.stream)
        new_data = np.array(zip(x, y))
        self.scat.set_offsets(new_data)
        return self.scat

class AnimatedScatter(object):

    def __init__(self, scatter_plots, framePause = True):
        self.pause = framePause
        self.stop = False
        self.framePause = framePause
        self.fig, self.ax = plt.subplots()
        self.scatter_plots = scatter_plots
        self.fig.canvas.mpl_connect('button_press_event', self.on_click)
        self.set_limits()
        self.ani = animation.FuncAnimation(self.fig, self.update_plot, interval=1000, frames = self.scatter_plots[0].get_frames_len(),
                                               init_func=self.setup_plot, blit=False)

    def get_animation(self):
        return self.ani

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
        for s in self.scatter_plots:
            s.setup_plot_edges()
        return self.plots

    def update_plot(self, i):
        #self.ax.set_title("Time Frame: %d" % self.scatter_plots[0].get_frame().start)
        if not self.pause:
            self.plots = [s.update_plot() for s in self.scatter_plots]
            for s in self.scatter_plots:
                s.update_edges()
            self.pause = self.framePause
        return self.plots

    def show(self):
        plt.show()
