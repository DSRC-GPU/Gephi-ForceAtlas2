#!/usr/bin/env python2.7
import os
import sys

from mpl_toolkits.mplot3d import Axes3D
from scipy.interpolate import griddata

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
        ts = get_param(param_path, "PhiFine")
        tw = get_param(param_path, "PhiCoarse")
        ts = float(ts)
        tw = float(tw)
        res = float(get_param(res_path, "Avg Success Rate")) / 100
        X.append(tw)
        Y.append(ts)
        Z.append(res)

def image_plot(x, y, z):
    #from matplotlib import pyplot as P
    #from matplotlib import colors, ticker, cm
    from matplotlib.colors import LogNorm
    ax = plt.gca()
    ax.set_xlabel('Phi Low')
    ax.set_ylabel('Phi High')
    csf = ax.contourf(x, y, z, 200, cmap=plt.cm.jet)
    cbar = plt.colorbar(csf)
    plt.show()
    #s = P.contourf(x, y, z, 50, locator=ticker.LogLocator(), cmap=plt.cm.jet)
    #cbar = P.colorbar()
    #P.show()

def surface_plot(x, y, z):
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.set_xlabel('Phi High')
    ax.set_ylabel('Phi Low')
    ax.set_zlabel('Accuracy')
    surf = ax.plot_surface(x, y, z, cmap=plt.cm.jet, vmin=np.nanmin(z), vmax=np.nanmax(z), linewidth = 0.1)
    cbar = plt.colorbar(surf)
    plt.show()

X = np.array(X)
Y = np.array(Y)
Z = np.array(Z)

X = np.log10(X)
Y = np.log10(Y)

xi = np.linspace(X.min(),X.max(), 1000)
yi = np.linspace(Y.min(),Y.max(), 1000)
zi = griddata((X, Y), Z, (xi[None,:], yi[:,None]), method='cubic')
xig, yig = np.meshgrid(xi, yi)
#surface_plot(xig, yig, zi)
image_plot(xi, yi, zi)
