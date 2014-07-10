#!/usr/bin/env python2.7

import os
import sys
import math

ORIGINAL_POS = 0
VELOCITY_VEC = 1
VELOCITY_VEC_PHI1 = 2
VELOCITY_VEC_PHI2 = 3
PCA_VALUES = 4

NODES_FILE = "nodes.txt"
EDGES_FILE = "edges.txt"

edges = {}
nodes = {}

def getEdge(node_src, node_dst):
    global edges
    idn = str(node_src) + str(node_dst)
    return edges.get(idn, None)

def getNode(node_id):
    global nodes
    return nodes.get(node_id, None)

class Vector(object):
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def get_len(self):
        return math.sqrt(self.x * self.x + self.y * self.y)

    def __repr__(self):
        return "(%f %f)" % (self.x, self.y)


class Edge(object):
    def __init__(self, src, dst):
        self.src = src
        self.dst = dst
        global edges
        edges[str(src) + str(dst)] = self

    def __repr__(self):
        return "(%d %d)" % (self.src, self.dst)


class Node(object):
    def __init__(self, no, group, cc):
        global nodes
        self.no = no
        self.group = group
        self.cc = cc
        self.vectors = []

    def add_vector(self, v):
        self.vectors.append(v)

    def __repr__(self):
        return "[id = %d group = %d]" % (self.no, self.group)


class TimeInterval(object):
    def __init__(self, start, end):
        self.end = end
        self.start = start
        self.nodes = []
        self.edges = []

    def get_nodes(self, edge):
        return self.nodes[edge.src], self.nodes[edge.dst]

    def get_edge(self, idx):
        return self.edges[idx]

    def add_edge(self, e):
        self.edges.append(e)

    def add_node(self, n):
        self.nodes.append(n)

    def __repr__(self):
        return "[%f %f - NumNodes: %d]" % (self.start, self.end, len(self.nodes))


def read_file(dname):
    def read_time_interval(line):
        parts = line.split()
        assert len(parts) == 4
        assert parts[0] == "from"
        assert parts[2] == "to"
        start = float(parts[1])
        end = float(parts[3])
        return TimeInterval(start, end)

    def read_vector(line):
        line = line.replace("(","").replace(")", "");
        parts = line.split(",")
        assert len(parts) == 2
        x = float(parts[0])
        y = float(parts[1])
        return Vector(x, y)

    def read_node(line):
        parts = line.split()
        assert len(parts) == 7
        no = int(parts[0])
        group = int(parts[1])
        cc = int(parts[2])
        n = Node(no, group, cc)
        n.add_vector(read_vector(parts[3]))
        n.add_vector(read_vector(parts[4]))
        n.add_vector(read_vector(parts[5]))
        n.add_vector(read_vector(parts[6]))
        #n.add_vector(read_vector(parts[7]))
        return n

    def read_edge(line):
        parts = line.split()
        assert len(parts) == 2
        src = int(parts[0])
        dst = int(parts[1])
        e = getEdge(src, dst)
        return e if e else Edge(src, dst)

    def read_nodes_file(fpath, time_intervals):
        with open(fpath) as f:
            content = f.readlines()
        i = 0
        while i < len(content):
            timeInterval = read_time_interval(content[i])
            time_intervals.append(timeInterval)
            parts = content[i+1].split()
            assert len(parts) == 2
            assert parts[0] == "NumNodes:"
            numNodes = int(parts[1])
            i += 2
            for j in range(i, i + numNodes):
                n = read_node(content[j])
                timeInterval.add_node(n)
            i += numNodes

    def read_edges_file(fpath, time_intervals):
        with open(fpath) as f:
            content = f.readlines()
        i = 0
        k = 0
        while i < len(content):
            timeInterval = time_intervals[k]
            parts = content[i+1].split()
            assert len(parts) == 2
            assert parts[0] == "NumEdges:"
            numEdges = int(parts[1])
            i += 2
            for j in range(i, i + numEdges):
                e = read_edge(content[j])
                timeInterval.add_edge(e)
            i += numEdges
            k += 1


    nodes_file = os.path.join(dname, NODES_FILE)
    edge_file = os.path.join(dname, EDGES_FILE)

    if not os.path.isdir(dname):
        raise ValueError(dname + " directory does not exist")
    if not os.path.isfile(nodes_file):
        raise ValueError(nodes_file + " file does not exist")
    if not os.path.isfile(edge_file):
        raise ValueError(edge_file + " file does not exist")

    time_intervals = []
    read_nodes_file(nodes_file, time_intervals)
    read_edges_file(edge_file, time_intervals)


    return time_intervals
