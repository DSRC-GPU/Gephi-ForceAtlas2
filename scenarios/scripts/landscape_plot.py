#!/usr/bin/env python2

import sys
import math

from mpl_toolkits.mplot3d import Axes3D
from scipy.interpolate import griddata

from matplotlib.collections import LineCollection
import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np
from sklearn.decomposition import PCA

from utils.data_reader import ProximityGraph, Fields
from utils.animator import AnimatedScatter, DataFeeder, ScatterPlot

class DataFeederPCA(DataFeeder):
    def get_data(self, time_interval):
        return [n.get(self.field) for n in time_interval.nodes]

def surface_plot(x, y, z):
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.set_xlabel('x')
    ax.set_ylabel('y')
    ax.set_zlabel('pca')
    surf = ax.plot_surface(x, y, z, cmap=plt.cm.jet, vmin=np.nanmin(z), vmax=np.nanmax(z), linewidth = 0.1)
    cbar = plt.colorbar(surf)
    plt.show()

def image_plot(X, Y, C, x, y, z, edges):
    ax = plt.gca()
    c = map(lambda x: 'g' if x == 'r' else 'r', C)
    lines = LineCollection(edges, linewidths=0.2, colors= 'k')
    lines.set_alpha(0.5)
    csf = ax.contourf(x, y, z, 50, cmap=plt.cm.jet)
    CS = plt.contour(x,y,z,50,linewidths=0.0,colors='k')
    CS.set_alpha(0)
    zc = CS.collections[22]
    plt.setp(zc, linewidth=2, alpha=1)
    ax.scatter(X, Y, marker = 'o', c = c, s=20, edgecolors='none', zorder = 10)
    ax.add_collection(lines)
    cbar = plt.colorbar(csf)
    plt.axis('off')
    plt.savefig('1.pdf', format='PDF')
    #plt.show()


def get_pca_fine(p, time_frame):
    pca_feeder_fine = DataFeederPCA(p, Fields.FINE_PCA, time_frame)
    pca_feeder_coarse = DataFeederPCA(p, Fields.COARSE_PCA, time_frame)
    x_fine = iter(pca_feeder_fine).next()
    x_coarse = iter(pca_feeder_coarse).next()
    return np.array(x_coarse) - np.array(x_fine)

def get_pca(p, time_frame):
    velocity_feeder = DataFeeder(p, Fields.VELOCITY_VECTOR, time_frame)
    stream = iter(velocity_feeder)
    x, y, _ = next(stream)
    data = np.array(zip(x,y))
    pca = PCA(n_components=1)
    return np.array(pca.fit_transform(data)).flatten()

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
    pos_feeder = DataFeeder(p, Fields.EMBEDDING_POS, time_frame)
    x, y, c = iter(pos_feeder).next()
    edges = pos_feeder.get_current_edges()

    X = np.array(x)
    Y = np.array(y)
    Z = get_pca_fine(p, time_frame)

    xi = np.linspace(X.min(),X.max(), 1000)
    yi = np.linspace(Y.min(),Y.max(), 1000)
    zi = griddata((X, Y), Z, (xi[None,:], yi[:,None]), method='cubic')
    xig, yig = np.meshgrid(xi, yi)

    #surface_plot(xig, yig, zi)
    image_plot(x, y, c, xi, yi, zi, edges)

if __name__ == "__main__":
    main()
