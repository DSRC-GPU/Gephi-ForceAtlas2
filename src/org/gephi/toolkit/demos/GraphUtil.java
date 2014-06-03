package org.gephi.toolkit.demos;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public final class GraphUtil {

    private static final String GROUP_COLUMN_NAME = "Group";

    public static void printNodes(PrintStream ps) {

        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph g = graphModel.getGraphVisible();

        List list = new ArrayList();
        for (Node n : g.getNodes()) {
            list.add(n);
        }

        Collections.sort(list, new Comparator<Node>() {
            public int compare(Node n1, Node n2) {
                return n1.getId() - n2.getId();
            }
        });

        for (Node n : g.getNodes()) {
            ps.println(n.getId() + " " + n.getNodeData().x() + " " + n.getNodeData().y());
        }

        ps.println("");
    }

    public static Set<Integer> getGroups() {
        Set<Integer> groups = new HashSet<Integer>();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = graphModel.getGraphVisible();

        for (Node n : graph.getNodes()) {
            Integer group = (Integer) n.getAttributes().getValue(GROUP_COLUMN_NAME);
            groups.add(group);
        }

        return groups;
    }

    public static List<Node> getNodesInGroup(int group) {
        List<Node> nodes = new ArrayList<Node>();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = graphModel.getGraphVisible();

        for (Node n : graph.getNodes()) {
            Integer nodeGroup = (Integer) n.getAttributes().getValue(GROUP_COLUMN_NAME);
            if (nodeGroup.compareTo(group) == 0) {
                nodes.add(n);
            }
        }

        return nodes;
    }

    public static List<Node> getNodesNotInGroup(int group) {
        List<Node> nodes = new ArrayList<Node>();
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph graph = graphModel.getGraphVisible();

        for (Node n : graph.getNodes()) {
            Integer nodeGroup = (Integer) n.getAttributes().getValue(GROUP_COLUMN_NAME);
            if (nodeGroup.compareTo(group) != 0) {
                nodes.add(n);
            }
        }

        return nodes;
    }

    public static FloatList getVector(Node n, String columnName) {
        FloatList vals = (FloatList) n.getAttributes().getValue(columnName);
        assert vals != null : "vals is null node: " + n;
        assert vals.size() == 2 : "vals size < 2: got: " + vals.size() + " node: " + n;
        return vals;
    }

}
