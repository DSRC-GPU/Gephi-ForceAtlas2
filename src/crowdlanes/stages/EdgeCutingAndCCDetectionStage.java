package crowdlanes.stages;

import crowdlanes.GraphUtil;
import crowdlanes.Simulation;
import crowdlanes.config.CurrentConfig;
import static crowdlanes.stages.DoPStageCoords.EDGE_CUT;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.Lookup;

public class EdgeCutingAndCCDetectionStage extends PipelineStage {

    private final GraphModel graphModel;

    public EdgeCutingAndCCDetectionStage() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isColumnNull(PCADoPStage.EDGE_CUT)) {
            return; 
        }

        Graph g = graphModel.getGraphVisible();
        cutEdges(g);
        ConnectedComponents cc = GraphUtil.getCC();
        System.out.println("Connected Componets: " + cc.getComponentsSize());

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
    }

    @Override
    public void tearDown() {
    }

}
