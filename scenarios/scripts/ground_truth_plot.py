#!/usr/bin/env python2.7

import sys

import networkx as nx
import matplotlib.pyplot as plt
import matplotlib.animation as animation

def get_graph_at_time(G, time_stamp):
    filter_func = lambda attrdict: True if attrdict["spells"][0][0] <= time_stamp <= attrdict["spells"][0][1] else False
    nodes = [n for n, attrdict in G.node.items() if filter_func(attrdict)]
    if not nodes:
        return None

    newG = nx.Graph(G.subgraph(nodes))
    for n, attrdict in newG.nodes(data = True):
        xpos = attrdict["xpos"]
        ypos = attrdict["ypos"]
        new_xpos = [pos for pos, start, end in xpos if start <= time_stamp <= end]
        new_ypos = [pos for pos, start, end in ypos if start <= time_stamp <= end]
        newG.node[n]["pos"] = new_xpos + new_ypos
        del newG.node[n]["xpos"]
        del newG.node[n]["ypos"]
        del newG.node[n]["spells"]

    for src, dst, d in newG.edges(data = True):
        spells = [True for start, end in d["spells"] if start <= time_stamp <= end]
        if not spells:
            newG.remove_edge(src, dst)
        else:
            del newG.edge[src][dst]["spells"]

    return newG

def get_graph_snapshot(G, time_step = 1):
    crr_time = 0
    while True:
        newG = get_graph_at_time(G, crr_time)
        if not newG:
            break
        pos = nx.get_node_attributes(newG, "pos")
        yield newG, pos
        crr_time += time_step


def plot(G):
    it = iter(get_graph_snapshot(G))
    nodesPlot = None
    edgesPlot = None

    def init():
        global nodesPlot, edgesPlot
        newG, pos = next(it)
        nodesPlot = nx.draw_networkx_nodes(newG, pos, node_size = 20)
        edgesPlot = nx.draw_networkx_edges(newG, pos)
        return nodesPlot, edgesPlot

    def animate(i):
        global nodesPlot, edgesPlot
        newG, pos = next(it)
        edges =[[newG.node[src]["pos"], newG.node[dst]["pos"]] for src, dst in newG.edges()]
        nodesPlot.set_offsets(pos.values())
        edgesPlot.set_segments(edges)
        return nodesPlot, edgesPlot

    fig = plt.figure()
    plt.xlim(0, 800)
    plt.ylim(0, 800)
    anim = animation.FuncAnimation(fig, animate, init_func = init, frames=50, interval=10, blit=False)
    plt.show()

def main(argv=None):
    if argv is None:
        argv = sys.argv

    if len(argv) == 1:
        print "Usage %s gexf_file"
        sys.exit(1)

    G = nx.read_gexf(argv[1])
    plot(G)

if __name__ == "__main__":
    main()

