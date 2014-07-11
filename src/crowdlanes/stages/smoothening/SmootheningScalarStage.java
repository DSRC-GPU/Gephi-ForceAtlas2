package crowdlanes.stages.smoothening;

import crowdlanes.util.EdgeWeight;
import crowdlanes.config.CurrentConfig;
import static crowdlanes.config.ConfigParamNames.CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS;
import crowdlanes.metrics.CosineSimilarity;
import crowdlanes.stages.PipelineStage;
import crowdlanes.stages.VelocityProcessorStage;
import java.io.PrintWriter;
import java.util.HashMap;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class SmootheningScalarStage extends PipelineStage {

    private final static String AVG_NORMAL = "normal";
    private final static String AVG_COSINE_SIM = "cosineSimilarity";
    private final static String AVG_EDGE_WEIGHTS = "edgeWeights";

    private final GraphModel graphModel;
    private boolean normalAverage;
    private boolean cosineSimWeights;
    private boolean edgeWeights;
    private int noRounds;
    private float phi;
    private final HashMap<Integer, Double> state;
    private final String phiParamName;
    private final String noRoundsParamName;
    private String averageMethod;
    private final SmootheningDataProvider dp;
    private final AttributeColumn outputColumn;

    public SmootheningScalarStage(String outputColumn, String phiName, String noRoundsParamName, SmootheningDataProvider dp) {
        this.dp = dp;
        this.state = new HashMap<>();
        this.phiParamName = phiName;
        this.noRoundsParamName = noRoundsParamName;

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(outputColumn) == false) {
            this.outputColumn = nodesTable.addColumn(outputColumn, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        } else {
            this.outputColumn = nodesTable.getColumn(outputColumn);
        }
    }

    private void setCosineSimWeight(Edge e) {
        FloatList srcDir = VelocityProcessorStage.getVelocityVector(e.getSource());
        FloatList dstDir = VelocityProcessorStage.getVelocityVector(e.getTarget());
        double cs = CosineSimilarity.angularSimilarity(srcDir, dstDir);
        if (!Double.isNaN(cs)) {
            e.setWeight((float) cs);
        } else {
            e.setWeight(.0f);
        }
    }

    private void setEdgeVisibilityWeight(double from, double to, Edge e) {
        double edgeWeight = EdgeWeight.getEdgeWeightRatio(from, to, e);
        e.setWeight((float) edgeWeight);
    }

    private void initialize(double from, double to, Graph graph) {
        for (Node n : graph.getNodes()) {
            //if (n.getAttributes().getValue(resultVector) == null) {
            Double v = dp.getValue(n);
            n.getAttributes().setValue(outputColumn.getIndex(), v);
            //}
        }

        // Set edge Weights
        for (Edge e : graph.getEdges()) {
            if (cosineSimWeights) {
                setCosineSimWeight(e);
            } else if (edgeWeights) {
                setEdgeVisibilityWeight(from, to, e);
            } else if (normalAverage) {
                e.setWeight(1f);
            } else {
                throw new IllegalStateException("Unknown average method");
            }
        }
    }

    private void startRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            Double v = (Double) n.getAttributes().getValue(outputColumn.getIndex());
            state.put(n.getId(), v);
        }
    }

    private void runRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            double sum = 0;
            double totalWeight = 1;

            for (Node neighbour : graph.getNeighbors(n)) {
                Edge e = graph.getEdge(n, neighbour);
                double weight = e.getWeight();
                Double v = (Double) neighbour.getAttributes().getValue(outputColumn.getIndex());
                
                sum += (v * weight);
                totalWeight += weight;
            }

            sum /= totalWeight;
            state.put(n.getId(), sum);
        }
    }

    private void endRound(Graph graph) {
        for (Node n : graph.getNodes()) {

            Double v = dp.getValue(n);
            double neighSum = state.get(n.getId());

            double smoothenedVal = ((1 - phi) * neighSum) + (phi * v);
            n.getAttributes().setValue(outputColumn.getIndex(), smoothenedVal);
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        info("SmootheningStage: ");

        Graph g = graphModel.getGraphVisible();
        initialize(from, to, g);
        for (int i = 0; i < noRounds; i++) {
            startRound(g);
            runRound(g);
            endRound(g);
        }

        info("\n");
    }

    @Override
    public void setup(CurrentConfig cc) {

        this.averageMethod = cc.getStringValue(CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS);
        this.noRounds = cc.getIntegerValue(noRoundsParamName);
        this.phi = cc.getFloatValue(phiParamName);

        normalAverage = cosineSimWeights = edgeWeights = false;
        switch (averageMethod) {
            case AVG_NORMAL:
                normalAverage = true;
                break;
            case AVG_COSINE_SIM:
                cosineSimWeights = true;
                break;
            case AVG_EDGE_WEIGHTS:
                edgeWeights = true;
                break;
            default:
                throw new IllegalArgumentException("Unknown average method: " + averageMethod);
        }
    }

    @Override
    public void printParams(PrintWriter pw) {
        pw.println(CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS + ": " + this.averageMethod);
        pw.println(noRoundsParamName + ": " + this.noRounds);
        pw.println(phiParamName + ": " + this.phi);
    }

    @Override
    public void tearDown() {
        state.clear();
    }
    
    public AttributeColumn getOutputColumn() {
        return outputColumn;
    }

}
