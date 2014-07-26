#!/usr/bin/env python2.7

import sys
import math

from collections import defaultdict
import matplotlib.animation as animation
import matplotlib.pyplot as plt
from matplotlib.collections import PolyCollection
from scipy.spatial import ConvexHull
import numpy as np

from utils.convex_hull import convex_hull
from utils.data_reader import ProximityGraph, Fields
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

class DataFeederCC(DataFeeder):
    def get_data(self, time_interval):
        vecs = [n.vectors[self.field] for n in time_interval.nodes]
        x = [v.x for v in vecs]
        y = [v.y for v in vecs]
        c = [n.get(Fields.CC) for n in time_interval.nodes]
        return x, y, c

class ScatterPlotCC(ScatterPlot):

    def get_polygon(self, x, y, c):
        color_map = ('c', 'r', 'b', 'm', 'y', 'g')
        d = defaultdict(list)

        for p, cc in zip(zip(x, y), c):
            d[cc].append(p)

        polygons = []
        colors = []
        for k in set(c):
            if len(d[k]) == 2:
                pt1 = d[k][0]
                pt2 = d[k][1]
                dist = math.sqrt((pt1[0] - pt2[0]) ** 2 + (pt1[1] - pt2[1]) ** 2)
                xmid = (pt1[0] + pt2[0]) / 2
                ymid = (pt1[1] + pt2[1]) / 2
                polygons.append(d[k])
                colors.append(color_map[k])
            elif len(d[k]) == 1:
                pass
            else:
                ch = ConvexHull(d[k])
                points = ch.points
                pts = zip(points[ch.vertices, 0], points[ch.vertices, 1])
                polygons.append(pts)
                colors.append(color_map[k])

        return polygons, colors

    def setup_plot(self):
        ax = plt.gca()
        x, y, c = next(self.stream)
        po, co = self.get_polygon(x, y, c)
        self.poly = PolyCollection(po, facecolors = co, edgecolors='none')

        ax.add_collection(self.poly)
        self.scat = ax.scatter(x, y, c='k', marker = self.marker, s = 25)
        return self.scat

    def update_plot(self):
        x, y, c = next(self.stream)
        new_data = np.array(zip(x, y))
        po, co = self.get_polygon(x, y, c)
        self.poly.set_facecolor(co)
        self.poly.set_verts(po)
        self.scat.set_offsets(new_data)
        return self.scat

def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s results_dir [time_frame]"
        sys.exit(1)

    time_frame = None
    if len(argv) == 3:
        time_frame = int(argv[2])

    p = ProximityGraph(argv[1], True)
    data_feeder = DataFeederCC(p, Fields.EMBEDDING_POS, time_frame)
    embedding = ScatterPlotCC(data_feeder)
    an = AnimatedScatter([embedding], framePause = False)
    plt.axis('off')
    an.get_animation().save('demo.mov', fps = 5, extra_args=['-vcodec', 'libx264'], dpi = 170)
    #an.show()

if __name__ == "__main__":
    main()
