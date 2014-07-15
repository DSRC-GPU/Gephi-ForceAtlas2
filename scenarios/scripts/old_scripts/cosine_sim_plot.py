#!/usr/bin/env python2.7

import sys

import numpy as np
import matplotlib.pyplot as plt

num_group = None

class Group(object):
    def __init__(self, group_id):
        self.group_id = group_id

class Record(object):
    def __init__(self, group, cosine_sim_samegroup, cosine_sim_othergroup, stddev_samegroup, stddev_othergroup):
        self.cosine_sim_othergroup = cosine_sim_othergroup
        self.cosine_sim_samegroup = cosine_sim_samegroup
        self.stddev_othergroup = stddev_othergroup
        self.stddev_samegroup = stddev_samegroup
        self.group = group

class TimeInterval(object):
    def __init__(self, start, end):
        self.end = end
        self.start = start
        self.records = []

    def add_record(self, record):
        self.records.append(record)

def read_file(fname):
    global num_group

    def read_time_interval(line):
        parts = line.split()
        assert len(parts) == 4
        assert parts[0] == "from"
        assert parts[2] == "to"
        start = float(parts[1])
        end = float(parts[3])
        return TimeInterval(start, end)

    def read_record(line):
        parts = line.split()
        assert len(parts) == 5
        group = int(parts[0])
        cs_samegroup = float(parts[1])
        cs_othergroup = float(parts[3])
        stddev_samegroup = float(parts[2])
        stddev_othergroup = float(parts[4])
        return Record(group, cs_samegroup, cs_othergroup, stddev_samegroup, stddev_othergroup)

    time_intervals = []

    with open(fname) as f:
        content = f.readlines()

    i = 0
    while i < len(content):
        t = read_time_interval(content[i])
        num_entries = int(content[i + 1].split(':')[1])
        if not num_group:
            num_group = num_entries
        for j in range(num_entries):
            r = read_record(content[i + 2 + j])
            t.add_record(r)
        time_intervals.append(t)
        i += (num_entries + 2)

    return time_intervals

def plot(time_intervals):
    no_groups = num_group
    x = []
    interval_len = []

    f, axes = plt.subplots(no_groups, sharex=True, sharey=True)
    #f.subplots_adjust(hspace=0)

    groups = {i : Group(i) for i in range(1, no_groups + 1)}
    for i in range(1, no_groups + 1):
        g = groups[i]
        g.y1 = []
        g.y2 = []
        g.stddev1 = []
        g.stddev2 = []
        g.ax = axes[i - 1]

    for t in time_intervals:
        x.append(t.end)
        for r in t.records:
            g = groups[r.group]
            g.y1.append(r.cosine_sim_samegroup)
            g.y2.append(r.cosine_sim_othergroup)
            g.stddev1.append(r.stddev_samegroup  / 2)
            g.stddev2.append(r.stddev_othergroup / 2)

    for i in range(1, no_groups + 1):
        g = groups[i]
        g.ax.set_title('Group %d' % i)
        g.ax.errorbar(x, g.y1, yerr=g.stddev1, fmt="b-o")
        g.ax.errorbar(x, g.y2, yerr=g.stddev2, fmt="r-o")

    plt.xlabel('Time')
    plt.ylabel('Cosine Similarity')
    plt.ylim(-1.1, 1.1)
    plt.show()

def main(argv=None):
    if argv is None:
        argv = sys.argv

    print(argv[1])
    plot(read_file(argv[1]))


if __name__ == "__main__":
    main()
