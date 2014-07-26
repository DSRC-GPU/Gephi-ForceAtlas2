package crowdlanes.stages.smoothening;

import crowdlanes.util.EdgeWeight;
import crowdlanes.metrics.CosineSimilarity;
import crowdlanes.stages.VelocityProcessorStage;
import java.util.HashMap;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.xalan.xsltc.compiler.util.Type;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class SmootheningScalarStage {

    private final static String AVG_NORMAL = "normal";
    private final static String AVG_COSINE_SIM = "cosineSimilarity";
    private final static String AVG_EDGE_WEIGHTS = "edgeWeights";

    private boolean normalAverage;
    private boolean cosineSimWeights;
    private boolean edgeWeights;

    private final HashMap<Node, Double> smoothedValues;
    private final HashMap<Node, Double> state;
    private final int noRounds;
    private final float phi;
    private final String averageMethod;
    private final SmootheningDataProvider dp;

    public SmootheningScalarStage(float phi, int noRounds, String averageMethod, SmootheningDataProvider dp) {
        this.state = new HashMap<>();
        this.smoothedValues = new HashMap<>();
        this.dp = dp;
        this.averageMethod = averageMethod;
        this.phi = phi;
        this.noRounds = noRounds;

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

    private void setCosineSimWeight(Edge e) {
        Vector2D srcDir = VelocityProcessorStage.getVelocityVector(e.getSource());
        Vector2D dstDir = VelocityProcessorStage.getVelocityVector(e.getTarget());
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
            smoothedValues.put(n, v);
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
            state.put(n, smoothedValues.get(n));
        }
    }

    private void runRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            double sum = 0;
            double totalWeight = 1;

            for (Node neighbour : graph.getNeighbors(n)) {
                double weight = graph.getEdge(n, neighbour).getWeight();
                Double v = smoothedValues.get(neighbour);

                sum += (v * weight);
                totalWeight += weight;
            }

            sum /= totalWeight;
            state.put(n, sum);
        }
    }

    private void endRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            double neighSum = state.get(n);
            double res = ((1 - phi) * neighSum) + (phi * dp.getValue(n));
            smoothedValues.put(n, res);
        }
    }

    public void run(Graph g, double from, double to, boolean hasChanged) {
        System.out.print("SmootheningStage: ");
        initialize(from, to, g);
        for (int i = 0; i < noRounds; i++) {
            startRound(g);
            runRound(g);
            endRound(g);
        }
        System.out.println("");
    }

    public double getValue(Node n) {
        return smoothedValues.get(n);
    }
}
