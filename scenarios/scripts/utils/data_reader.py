#!/usr/bin/env python2.7

import os
import sys
import csv

class Fields:
    ID = "ID"
    GROUP = "GROUP"
    CC = "CC"
    EMBEDDING_POS = "EMBEDDING_POS"
    VELOCITY_VECTOR = "VELOCITY_VECTOR"
    FINE_SMOOTH_VEL_VECTOR = "FINE_SMOOTH_VEL_VECTOR"
    COARSE_SMOOTH_VEL_VECTOR = "COARSE_SMOOTH_VEL_VECTOR"
    FINE_PCA = "FINE_PCA"
    COARSE_PCA = "COARSE_PCA"

class ParamNames:
    UseGroundTruth = "UseGroundTruth"
    InitialEmbeddingSeed = "InitialEmbeddingSeed"
    NoIters = "NoIters"
    EmbeddingType = "EmbeddingType"
    UseEdgeWeights = "UseEdgeWeights"
    RollingWindowSize = "RollingWindowSize"
    WeightsType = "WeightsType"
    NoRoundsCoarse = "NoRoundsCoarse"
    NoRoundsFine = "NoRoundsFine"
    PhiCoarse = "PhiCoarse"
    PhiFine = "PhiFine"

class Vector(object):
    def __init__(self, x, y):
        self.x = x
        self.y = y

    def get(self):
        return self.x, self.y

    def __repr__(self):
        return "(%f %f)" % (self.x, self.y)

class TimeInterval(object):
    def __init__(self, line):
        parts = line.split()
        assert len(parts) == 5
        assert parts[0] == "from"
        assert parts[2] == "to"
        start = float(parts[1])
        end = float(parts[3])
        self.success = float(parts[4])
        self.start = start
        self.end = end
        self.nodes = []
        self.edges = []

    def get_success_rate(self):
        return self.success

    def get_nodes(self, edge):
        src = self.nodes[edge.src]
        dst = self.nodes[edge.dst]
        assert src.get(Fields.ID) == edge.src
        assert dst.get(Fields.ID) == edge.dst
        return self.nodes[edge.src], self.nodes[edge.dst]

    def get_edge(self, idx):
        return self.edges[idx]

    def add_edge(self, e):
        self.edges.append(e)

    def add_node(self, n):
        self.nodes.append(n)

    def get_node(self, id_no):
        res = filter(lambda x: x.get(Fields.ID) == id_no, self.nodes)
        return res[0] if res else None

    def __repr__(self):
        return "[%f %f - NumNodes: %d]" % (self.start, self.end, len(self.nodes))

class Node(object):
    def __init__(self, valdict):
        self.vectors = {}
        self.attr = {}
        self.attr[Fields.ID]         = int(valdict[Fields.ID])
        self.attr[Fields.CC]         = int(valdict[Fields.CC])
        self.attr[Fields.GROUP]      = int(valdict[Fields.GROUP])
        self.attr[Fields.FINE_PCA]   = float(valdict[Fields.FINE_PCA])
        self.attr[Fields.COARSE_PCA] = float(valdict[Fields.COARSE_PCA])

        self.__read_vector(valdict, Fields.EMBEDDING_POS)
        self.__read_vector(valdict, Fields.VELOCITY_VECTOR)
        #self.__read_vector(valdict, Fields.FINE_SMOOTH_VEL_VECTOR)
        #self.__read_vector(valdict, Fields.COARSE_SMOOTH_VEL_VECTOR)

    def get(self, name):
        return self.attr[name]

    def __read_attribute(self, valdict, name, type):
        val = valdict[name].split()

    def __read_vector(self, valdict, name):
        x, y = valdict[name].split()
        v = Vector(float(x), float(y))
        self.vectors[name] = v

    def __repr__(self):
        return "[id = %d group = %d]" % (self.no, self.group)

class Edge(object):
    def __init__(self, src, dst):
        self.src = src
        self.dst = dst

    def __repr__(self):
        return "(%d %d)" % (self.src, self.dst)

class Params(object):
    PARAM_FILE = "params.txt"

    def __init__(self, dname):
        self.params = {}
        if not os.path.isdir(dname):
            raise ValueError(dname + " directory does not exist")
        self.__read_params(dname)

    def __read_params(self, dname):
        params_file = os.path.join(dname, self.PARAM_FILE)
        if not os.path.isfile(params_file):
            raise ValueError(params_file + " file does not exist")

        with open(params_file) as f:
            for line in f:
                parts = line.strip().split(":")
                name = parts[0]
                val = parts[1].strip()
                self.params[name] = val

    def get(self, name):
        return self.params.get(name, None)


class ProximityGraph(object):
    NODES_FILE = "nodes.txt"
    EDGES_FILE = "edges.txt"

    def __init__(self, dname, read_edges):
        if not os.path.isdir(dname):
            raise ValueError(dname + " directory does not exist")
        self.time_intervals_start = {}
        self.time_intervals = []
        self.read_edges = read_edges
        self.__read_nodes_file(dname)
        if read_edges:
            self.__read_edges_file(dname)

    def __read_nodes_file(self, dname):
        nodes_file = os.path.join(dname, self.NODES_FILE)
        if not os.path.isfile(nodes_file):
            raise ValueError(nodes_file + " file does not exist")

        with open(nodes_file) as f:
            header = f.readline().strip()
            while True:
                line = f.readline().strip()
                if not line:
                    break
                time_interval = self.__get_new_time_interval(line)
                self.time_intervals.append(time_interval)
                num_nodes = f.readline().split(':')[1]
                num_nodes = int(num_nodes)
                node_data = [f.readline().strip() for _ in range(num_nodes)]
                csv_reader = csv.DictReader([header] + node_data)
                for line in csv_reader:
                    time_interval.add_node(Node(line))

    def __read_edges_file(self, dname):
        edges_file = os.path.join(dname, self.EDGES_FILE)
        if not os.path.isfile(edges_file):
            raise ValueError(edges_file + " file does not exist")

        with open(edges_file) as f:
            for time_interval in self.time_intervals:
                time_period = f.readline()
                num_edges = f.readline().split(':')[1]
                num_edges = int(num_edges)
                edge_data = [f.readline().strip() for _ in range(num_edges)]
                for line in edge_data:
                    parts = line.strip().split()
                    src = int(parts[0])
                    dst = int(parts[1])
                    e = Edge(src, dst)
                    time_interval.add_edge(e)

    def __get_new_time_interval(self, line):
        t = TimeInterval(line)
        self.time_intervals_start[t.start] = t
        return t


    def get_edge(self, node_src, node_dst):
        idn = str(node_src) + str(node_dst)
        return self.edges.get(idn, None)

    def get_time_interval(self, start):
        return self.time_intervals_start.get(start, None)

    def get_time_intervals(self):
        return self.time_intervals
