package crowdlanes.stages;

import crowdlanes.config.CurrentConfig;
import static crowdlanes.stages.dop.Dop.EDGE_CUT;
import crowdlanes.util.CompareCommunities;
import crowdlanes.util.GraphUtil;
import static crowdlanes.util.GraphUtil.GROUP_COLUMN_NAME;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.ConnectedComponents;

public class EdgeCutingAndCCDetectionStage extends PipelineStage {
    
    private int steps;
    private double avgSuccessRate;

    public EdgeCutingAndCCDetectionStage() {
        super();
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isEdgeColumnNull(EDGE_CUT)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        cutEdges(g);
        ConnectedComponents cc = GraphUtil.getCC();
        int[] componentsSize = cc.getComponentsSize();
        System.err.println("Connected Componets: " + cc.getConnectedComponentsCount());
        System.err.println("Connected comp sizes: " + Arrays.toString(componentsSize));
        double successRate = getSuccessRate(g) * 100;
        avgSuccessRate += successRate;
        steps++;
        System.err.println("Success Rate: " + successRate);
    }

    public double getSuccessRate(Graph g) {
        AttributeModel attributeModel = attributeController.getModel();
        AttributeColumn groupColumn = attributeModel.getNodeTable().getColumn(ConnectedComponents.WEAKLY);
        
        List<Integer> groundTruth = new ArrayList<>();
        List<Integer> results = new ArrayList<>();

        for (Node n : g.getNodes()) {
            int group = (int) n.getAttributes().getValue(GROUP_COLUMN_NAME);
            int cc = groupColumn != null ? (int) n.getAttributes().getValue(groupColumn.getIndex()) : 1;
            groundTruth.add(group);
            results.add(cc);
        }
        
        return CompareCommunities.nmi(results, groundTruth);
    }

    private void cutEdges(Graph g) {
        Edge[] edges = g.getEdges().toArray();
        for (Edge e : edges) {
            Boolean isCut = (Boolean) e.getAttributes().getValue(EDGE_CUT);
            if (isCut) {
                g.removeEdge(e);
            }
        }
    }

    @Override
    public void setup(CurrentConfig cc) {
        avgSuccessRate = 0;
        steps = 0;
    }

    @Override
    public void tearDown() {
         System.err.println("Avg Success Rate: " + avgSuccessRate / steps);
    }
}
