package crowdlanes.stages;

import crowdlanes.metrics.CosineSimilarity;
import crowdlanes.*;
import static crowdlanes.config.ParamNames.*;
import crowdlanes.config.CurrentConfig;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

public class SmootheningStage extends PipelineStage {

    public final static String FINE_SMOOTHENING = "smoothened_fine";
    public final static String COARSE_SMOOTHENING = "smoothened_coarse";
    public final static String SECTION = "Smoothening";

    private final static String AVG_NORMAL = "normal";
    private final static String AVG_COSINE_SIM = "cosineSimilarity";
    private final static String AVG_EDGE_WEIGHTS = "edgeWeights";

    private final GraphModel graphModel;
    private boolean normalAverage;
    private boolean cosineSimWeights;
    private boolean edgeWeights;
    private int noRounds;
    private float phi;
    private final HashMap<Integer, Coords2D> state;
    private final SpeedSimilarityStage sss;
    private final CosineSimilarityStage csc;
    private final String resultVector;
    private final String phiParamName;
    private final String noRoundsParamName;
    private String averageMethod;
    //private final int threadCount;
    //private final ExecutorService pool;

    public SmootheningStage(String resultVector, String phiName, String noRoundsParamName) throws IllegalAccessException {
        this.state = new HashMap<>();
        this.phiParamName = phiName;
        this.noRoundsParamName = noRoundsParamName;
        this.resultVector = resultVector;

        sss = new SpeedSimilarityStage("speed_sim_" + resultVector, resultVector);
        csc = new CosineSimilarityStage("cosine_sim_" + resultVector, resultVector);

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(resultVector) == false) {
            nodesTable.addColumn(resultVector, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }
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
        double edgeWeight = EdgeWeight.getEdgeWeightRatio(from, to, e);
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
            //neighSum.x = 0;
            //neighSum.y = 0;
        }
    }

    private void runRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            Coords2D neighSum = state.get(n.getId());

            for (Node neighbour : graph.getNeighbors(n)) {
                Edge e = graph.getEdge(n, neighbour);
                FloatList neighbourDir = GraphUtil.getVector(neighbour, resultVector);
                neighSum.x += (neighbourDir.getItem(0) * e.getWeight());
                neighSum.y += (neighbourDir.getItem(1) * e.getWeight());
            }
        }
    }

    private void endRound(Graph graph) {
        for (Node n : graph.getNodes()) {

            //double totalWeight = 0;
            double totalWeight = 1;
            for (Node neigh : graph.getNeighbors(n)) {
                totalWeight += graph.getEdge(n, neigh).getWeight();
            }

            Coords2D nodeVals = state.get(n.getId());
            nodeVals.x /= totalWeight;
            nodeVals.y /= totalWeight;

            FloatList dir = VelocityProcessorStage.getVelocityVector(n);
            float x = (1f - phi) * nodeVals.x + phi * dir.getItem(0);
            float y = (1f - phi) * nodeVals.y + phi * dir.getItem(1);
            n.getAttributes().setValue(resultVector, new FloatList(new Float[]{x, y}));
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {

        if (GraphUtil.isColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

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
        csc.run(from, to, hasChanged);
        sss.run(from, to, hasChanged);
    }

    @Override
    public void setup(CurrentConfig cc) {
        csc.setup(cc);
        sss.setup(cc);

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

        for (Node n : graphModel.getGraph().getNodes()) {
            state.put(n.getId(), new Coords2D());
            n.getAttributes().setValue(resultVector, null);
        }
    }

    public void printParams(PrintWriter pw) {
        pw.println(CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS + ": " + this.averageMethod);
        pw.println(noRoundsParamName + ": " + this.noRounds);
        pw.println(phiParamName + ": " + this.phi);
    }

    @Override
    public void tearDown() {
        state.clear();
        csc.tearDown();
        sss.tearDown();
    }
}
