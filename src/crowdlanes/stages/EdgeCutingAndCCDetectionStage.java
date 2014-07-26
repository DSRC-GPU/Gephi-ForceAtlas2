package crowdlanes.stages;

import crowdlanes.config.CurrentConfig;
import crowdlanes.config.ResultsDir;
import static crowdlanes.stages.dop.Dop.EDGE_CUT;
import crowdlanes.util.CompareCommunities;
import crowdlanes.util.GraphUtil;
import static crowdlanes.util.GraphUtil.GROUP_COLUMN_NAME;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.Exceptions;

public class EdgeCutingAndCCDetectionStage extends PipelineStage {

    DescriptiveStatistics successRate;
    private PrintWriter results_writer;
    public static final String COMMUNITY_DETECTION = "COMMUNITY_DETECTION";

    public EdgeCutingAndCCDetectionStage() {
        super();
        successRate = new DescriptiveStatistics();
    }

    public double getCurrentSuccessRate() {
        long noVals = successRate.getN();
        if (noVals == 0) {
            return Double.NaN;
        }
        return successRate.getElement((int) (noVals - 1));
    }

    public double getAverageSuccessRate() {
        return successRate.getMean();
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
        double s = getSuccessRate(g) * 100;
        g.getAttributes().setValue(COMMUNITY_DETECTION, s);
        successRate.addValue(s);
        System.err.println("Success Rate: " + s);
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
        successRate.clear();
        addGraphColumn(COMMUNITY_DETECTION, AttributeType.DOUBLE);
        graphModel.getGraphVisible().getAttributes().setValue(COMMUNITY_DETECTION, Double.NaN);

        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            results_writer = new PrintWriter(new File(resultsDir, "results.txt"), "UTF-8");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void tearDown() {
        System.err.println("Avg Success Rate: " + successRate.getMean());
        results_writer.println("Avg Success Rate: " + successRate.getMean());
        results_writer.close();
    }
}
