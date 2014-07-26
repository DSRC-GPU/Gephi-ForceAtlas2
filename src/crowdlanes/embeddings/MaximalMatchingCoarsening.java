package crowdlanes.embeddings;

import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;

public class MaximalMatchingCoarsening implements org.gephi.layout.plugin.multilevel.MultiLevelLayout.CoarseningStrategy {

    private final Random rand;

    public MaximalMatchingCoarsening(Integer seed) {
        rand = (seed == null) ? new Random() : new Random(seed);
    }

    @Override
    public void coarsen(HierarchicalGraph g) {
        HierarchicalGraph graph = g;
        int retract = 0;
        int count = 0;
        for (Edge e : graph.getEdgesAndMetaEdges().toArray()) {
            Node a = e.getSource();
            Node b = e.getTarget();
            count++;
            if (graph.getParent(a) == graph.getParent(b) && graph.getLevel(a) == 0) {
                float x = (a.getNodeData().x() + b.getNodeData().x()) / 2;
                float y = (a.getNodeData().y() + b.getNodeData().y()) / 2;

                Node parent = graph.groupNodes(new Node[]{a, b});
                parent.getNodeData().setX(x);
                parent.getNodeData().setY(y);
                graph.retract(parent);
                retract++;
            }
        }
    }

    @Override
    public void refine(HierarchicalGraph graph) {
        double r = 10;
        int count = 0;
        int refined = 0;
        for (Node node : graph.getTopNodes().toArray()) {
            count++;
            if (graph.getChildrenCount(node) == 2) {
                refined++;
                float x = node.getNodeData().x();
                float y = node.getNodeData().y();

                for (Node child : graph.getChildren(node)) {
                    //double t = Math.random();
                    double t = rand.nextDouble();
                    child.getNodeData().setX((float) (x + r * Math.cos(t)));
                    child.getNodeData().setY((float) (y + r * Math.sin(t)));
                }
                graph.ungroupNodes(node);
            }
        }
        //System.out.println("COUNT = " + count);
        //System.out.println("REFINED = " + refined);
    }
}
