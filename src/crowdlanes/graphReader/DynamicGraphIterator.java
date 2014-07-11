package crowdlanes.graphReader;

import crowdlanes.config.CurrentConfig;
import static crowdlanes.config.ConfigParamNames.*;
import crowdlanes.graphReader.GraphReader;
import java.util.Iterator;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class DynamicGraphIterator implements Iterable<Graph> {

    private GraphView crrGraph;
    private boolean hasChanged;
    private final GraphModel graphModel;
    private final double step;
    private final double duration;
    private final double min;
    private final double max;

    private double lastFrom;
    private double lastTo;

    public DynamicGraphIterator(CurrentConfig cc) {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        this.step = cc.getDoubleValue(CONFIG_PARAM_GRAPH_ITERATOR_STEP);
        this.duration = cc.getDoubleValue(CONFIG_PARAM_GRAPH_ITERATOR_WINDOW_SIZE);
        DynamicController dc = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel model = dc.getModel();
        crrGraph = graphModel.copyView(graphModel.getVisibleView());
        this.max = model.getMax();
        this.min = model.getMin();
    }

    public Range getCurrentRange() {
        return new Range(lastFrom, lastTo);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    private boolean hasChanged(Graph newGraph, Graph crrGraph) {

        if (newGraph.getEdgeCount() != crrGraph.getEdgeCount()) {
            return true;
        }

        if (newGraph.getNodeCount() != crrGraph.getNodeCount()) {
            return true;
        }

        for (Edge e : newGraph.getEdges()) {
            String id = e.getEdgeData().getId();
            if (crrGraph.getEdge(id) == null) {
                return true;
            }
        }

        for (Edge e : crrGraph.getEdges()) {
            String id = e.getEdgeData().getId();
            if (newGraph.getEdge(id) == null) {
                return true;
            }
        }

        for (Node n : newGraph.getNodes()) {
            String id = n.getNodeData().getId();
            if (crrGraph.getNode(id) == null) {
                return true;
            }
        }

        for (Node n : crrGraph.getNodes()) {
            String id = n.getNodeData().getId();
            if (newGraph.getNode(id) == null) {
                return true;
            }
        }

        return false;
    }

    public Iterator<Graph> iterator() {
        return new GraphIterator();
    }

    private class GraphIterator implements Iterator<Graph> {

        private GraphReader graphReader;
        private boolean someAction;
        private double from;
        private double to;

        public GraphIterator() {
            from = 0;
            to = duration;
            someAction = true;
            graphReader = Lookup.getDefault().lookup(GraphReader.class);
        }

        @Override
        public boolean hasNext() {
            return someAction;
        }

        @Override
        public Graph next() {
            Graph g = graphReader.getGraph(from, to);
            hasChanged = hasChanged(g, graphModel.getGraph(crrGraph));
            lastFrom = from;
            lastTo = to;
            if (to < max) {
                from = Math.min(from + step, max);
                to = Math.min(to + step, max);
                someAction = true;
            } else {
                someAction = false;
            }

            crrGraph = graphModel.copyView(g.getView());
            return g;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
