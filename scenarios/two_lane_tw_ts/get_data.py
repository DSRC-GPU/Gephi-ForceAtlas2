#!/usr/bin/env python2.7
import os
import sys

from mpl_toolkits.mplot3d import Axes3D
from scipy.interpolate import griddata

from matplotlib.collections import LineCollection
import matplotlib.animation as animation
import matplotlib.pyplot as plt
import numpy as np

def get_param(path, name):
    for l in open(path):
        pname, val = l.strip().split(":")
        if pname == name:
            return val
    raise ValueError('could not find param %s in ' % (name))


X = []
Y = []
Z = []
for root, dirs, _ in os.walk("."):
    for d in dirs:
        param_path= os.path.join(d, "params.txt")
        res_path= os.path.join(d, "results.txt")
        ts = get_param(param_path, "Step")
        tw = get_param(param_path, "WindowSize")
        ts = float(ts)
        tw = float(tw)
        res = float(get_param(res_path, "Avg Success Rate")) / 100
        X.append(tw)
        Y.append(ts)
        Z.append(res)

def surface_plot(x, y, z):
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.set_xlabel('Sliding Window Size')
    ax.set_ylabel('Time Step Increment')
    ax.set_zlabel('Accuracy')
    surf = ax.plot_surface(x, y, z, cmap=plt.cm.jet, vmin=np.nanmin(z), vmax=np.nanmax(z), linewidth = 0.1)
    cbar = plt.colorbar(surf)
    plt.show()

X = np.array(X)
Y = np.array(Y)
Z = np.array(Z)

xi = np.linspace(X.min(),X.max(), 1000)
yi = np.linspace(Y.min(),Y.max(), 1000)
zi = griddata((X, Y), Z, (xi[None,:], yi[:,None]), method='cubic')
xig, yig = np.meshgrid(xi, yi)
surface_plot(xig, yig, zi)
