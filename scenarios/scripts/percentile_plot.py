#!/usr/bin/env python2.7

import sys

from matplotlib import mlab
import matplotlib.pyplot as plt
import numpy as np
from pylab import *
import statsmodels.api as sm


def plot(fname):
    data = np.genfromtxt(fname, dtype=float)
    new_data = np.sort(data.flatten())
    #yvals=np.arange(len(new_data))/float(len(new_data))

    ecdf = sm.distributions.ECDF(new_data)
    x = np.linspace(min(new_data), max(new_data))
    y = ecdf(x)

    fig = plt.figure()
    ax1 = fig.add_subplot(212)
    ax1.plot(1 - y, x, 'b-')
    ax1.plot(1-y, x, 'ro', ms=5)
    ax1.set_ylabel('Success Rate')
    ax1.set_xlabel('CDF')
    #ax1.set_xlim(0,101)
    ax1.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)
    ax1.set_axisbelow(True)

    num_samples = len(data[:,0])
    pos = np.arange(num_samples) + 1
    x = range(1, num_samples + 1)
    mean_all = np.array([np.mean(data[i]) for i in xrange(len(data))])
    std_all = np.array([np.std(data[i]) for i in xrange(len(data))])
    std_all /= 2

    ax2 = fig.add_subplot(211)
    #ax2.set_title("Three Lanes Scenario")
    ax2.set_xlabel('Pipeline step')
    ax2.set_ylabel('Success Rate')
    top = 120
    ax2.set_ylim(0, top)
    ax2.yaxis.grid(True, linestyle='-', which='major', color='lightgrey', alpha=1)
    ax2.set_axisbelow(True)

    ax2.errorbar(x, mean_all, yerr=std_all, fmt='b-o')
    upperLabels = [str(np.round(s, 2)) for s in mean_all]
    for tick in range(num_samples):
        k = tick % 2
        ax2.text(pos[tick], top-(top*0.05), upperLabels[tick], horizontalalignment='center', size='x-small')

    plt.show()


def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s results_dir [time_frame]"
        sys.exit(1)

    plot(argv[1])

if __name__ == "__main__":
    main()
