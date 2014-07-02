package crowdlanes.stages;

import crowdlanes.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class SmootheningStage extends PipelineStage {

    public final static String FINE_SMOOTHENING = "smoothened_fine";
    public final static String COARSE_SMOOTHENING = "smoothened_coarse";
    public final static String SECTION = "Smoothening";

    private final static String AVG_NORMAL = "normal";
    private final static String AVG_COSINE_SIM = "cosineSimilarity";
    private final static String AVG_EDGE_WEIGHTS = "edgeWeights";

    private PrintWriter cosineSimWriter;
    private PrintWriter speedSimWriter;
    private GraphModel graphModel;
    private boolean normalAverage;
    private boolean cosineSimWeights;
    private boolean edgeWeights;
    private final int noRounds;
    private final float inhibitFactor;
    private final HashMap<Integer, Coords2D> state;
    private final SpeedGroupSimilarity speedGroupSimilarity;
    private final CosineGroupSimilarity cosineGroupSimilarity;
    private final String resultVector;

    public SmootheningStage(int noRounds, float inhibitFactor, String averageMethod, String resultVector) throws IllegalAccessException {
        this.noRounds = noRounds;
        this.resultVector = resultVector;
        this.inhibitFactor = inhibitFactor;
        this.state = new HashMap<>();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(resultVector) == false) {
            nodesTable.addColumn(resultVector, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

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

        cosineGroupSimilarity = new CosineGroupSimilarity(resultVector);
        speedGroupSimilarity = new SpeedGroupSimilarity(resultVector);
    }

    private void setCosineSimWeight(Edge e) {
        FloatList srcDir = GraphUtil.getVector(e.getSource(), resultVector);
        FloatList dstDir = GraphUtil.getVector(e.getTarget(), resultVector);
        double cs = CosineSimilarity.angularSimilarity(srcDir, dstDir);
        if (!Double.isNaN(cs)) {
            e.setWeight((float) cs);
        } else {
            e.setWeight(.0f);
        }
    }

    private void setEdgeVisibilityWeight(double from, double to, Edge e) {
        double edgeWeight = EdgeWeight.getInstance().getEdgeWeightRatio(from, to, e);
        e.setWeight((float) edgeWeight);
    }

    private void initialize(double from, double to, Graph graph) {
        for (Node n : graph.getNodes()) {
            if (n.getAttributes().getValue(resultVector) == null) {
                FloatList v = VelocityProcessorStage.getVelocityVector(n);
                n.getAttributes().setValue(resultVector, new FloatList(new Float[]{v.getItem(0), v.getItem(1)}));
            }
        }

        // Set edge Weights
        for (Edge e : graph.getEdges()) {
            if (cosineSimWeights) {
                setCosineSimWeight(e);
            } else if (edgeWeights) {
                setEdgeVisibilityWeight(from, to, e);
            } else {
                e.setWeight(1f);
            }
        }
    }

    private void startRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            FloatList nDir = GraphUtil.getVector(n, resultVector);
            Coords2D neighSum = state.get(n.getId());
            neighSum.x = nDir.getItem(0);
            neighSum.y = nDir.getItem(1);
        }
    }

    private void runRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            Coords2D neighSum = state.get(n.getId());

            for (Node neighbour : graph.getNeighbors(n)) {
                Edge e = graph.getEdge(n, neighbour);
                FloatList neighbourDir = GraphUtil.getVector(n, resultVector);
                neighSum.x += neighbourDir.getItem(0) * e.getWeight();
                neighSum.y += neighbourDir.getItem(1) * e.getWeight();
            }
        }
    }

    private void endRound(Graph graph) {
        for (Node n : graph.getNodes()) {

            double totalWeight = 1;
            for (Node neigh : graph.getNeighbors(n)) {
                totalWeight += graph.getEdge(n, neigh).getWeight();
            }

            Coords2D nodeVals = state.get(n.getId());
            nodeVals.x /= totalWeight;
            nodeVals.y /= totalWeight;

            FloatList dir = VelocityProcessorStage.getVelocityVector(n);
            float x = (1 - inhibitFactor) * nodeVals.x + inhibitFactor * dir.getItem(0);
            float y = (1 - inhibitFactor) * nodeVals.y + inhibitFactor * dir.getItem(1);
            n.getAttributes().setValue(resultVector, new FloatList(new Float[]{x, y}));
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged
    ) {
        info("SmootheningStage: ");

        Graph g = graphModel.getGraphVisible();
        initialize(from, to, g);
        for (int i = 0; i < noRounds; i++) {
            startRound(g);
            runRound(g);
            endRound(g);
            //info("*");
        }

        info("\n");

        cosineSimWriter.println("from " + from + " to " + to);
        cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, 1);
        cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, 2);

        speedSimWriter.println("from " + from + " to " + to);
        speedGroupSimilarity.printGroupSimilarity(speedSimWriter, 1);
        speedGroupSimilarity.printGroupSimilarity(speedSimWriter, 2);

    }

    @Override
    public void setup() {
        state.clear();
        for (Node n : graphModel.getGraph().getNodes()) {
            state.put(n.getId(), new Coords2D());
            n.getAttributes().setValue(resultVector, null);
        }

        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            cosineSimWriter = new PrintWriter(new File(resultsDir, "cosine_sim_" + resultVector), "UTF-8");
            speedSimWriter = new PrintWriter(new File(resultsDir, "speed_sim_" + resultVector), "UTF-8");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void tearDown() {
        cosineSimWriter.close();
        speedSimWriter.close();
    }
}