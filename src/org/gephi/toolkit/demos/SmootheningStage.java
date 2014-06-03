package org.gephi.toolkit.demos;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public final static String RESULT_VECTOR = "SmoothenedVector";
    public final static String SECTION = "Smoothening";

    private int step;
    private PrintWriter cosineSimWriter;
    private PrintWriter speedSimWriter;
    private GraphModel graphModel;
    private final int noRounds;
    private final float inhibitFactor;
    private boolean normalAverage;
    private boolean cosineAverage;
    private boolean edgeWeightsAverage;
    private final HashMap<Integer, List<Coords2D>> state;
    private final HashMap<Integer, List<Double>> cosineSim;
    private final HashMap<Integer, List<Double>> edgeWeights;
    private final SpeedGroupSimilarity speedGroupSimilarity;
    private final CosineGroupSimilarity cosineGroupSimilarity;

    public SmootheningStage(int noRounds, float inhibitFactor, String averageMethod) {
        this.noRounds = noRounds;
        this.inhibitFactor = inhibitFactor;
        state = new HashMap<Integer, List<Coords2D>>();
        cosineSim = new HashMap<Integer, List<Double>>();
        edgeWeights = new HashMap<Integer, List<Double>>();

        cosineGroupSimilarity = new CosineGroupSimilarity(RESULT_VECTOR);
        speedGroupSimilarity = new SpeedGroupSimilarity((RESULT_VECTOR));

        if (averageMethod.equals("normal")) {
            normalAverage = true;
            info("normal Average\n");
        } else if (averageMethod.equals("cosineSimilarity")) {
            cosineAverage = true;
            info("cosineAverage Average\n");
        } else if (averageMethod.equals("edgeWeights\n")) {
            info("edgeWeights Average");
            edgeWeightsAverage = true;
        }
    }

    private void initialize(Graph graph) {
        for (Node n : graph.getNodes()) {
            if (n.getAttributes().getValue(RESULT_VECTOR) == null) {
                FloatList v = VelocityProcessorStage.getVelocityVector(n);
                n.getAttributes().setValue(RESULT_VECTOR, new FloatList(new Float[]{v.getItem(0), v.getItem(1)}));
            }
        }
    }

    private void startRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            List<Coords2D> nodeVals = state.get(n.getId());
            List<Double> nodeCosineSim = cosineSim.get(n.getId());
            List<Double> edgeWeightsVals = edgeWeights.get(n.getId());
            nodeVals.clear();
            nodeCosineSim.clear();

            FloatList vals = (FloatList) n.getAttributes().getValue(RESULT_VECTOR);
            assert vals.size() == 2 : "vals size < 2: got: " + vals.size() + " node: " + n;

            nodeCosineSim.add(1.0);
            edgeWeightsVals.add(1.0);
            nodeVals.add(new Coords2D(vals.getItem(0), vals.getItem(1)));
        }
    }

    private void runRound(double from, double to, Graph graph) {
        for (Node n : graph.getNodes()) {
            List<Coords2D> nodeVals = state.get(n.getId());
            List<Double> nodeCosineSim = cosineSim.get(n.getId());
            List<Double> edgeWeightsVals = edgeWeights.get(n.getId());
            FloatList nDir = (FloatList) n.getAttributes().getValue(RESULT_VECTOR);

            for (Node neighbour : graph.getNeighbors(n)) {
                FloatList neighbourDir = (FloatList) neighbour.getAttributes().getValue(RESULT_VECTOR);
                Edge e = graph.getEdge(n, neighbour);
                double cs = CosineSimilarity.angularSimilarity(nDir, neighbourDir);
                if (!Double.isNaN(cs)) {
                    nodeCosineSim.add(cs);
                } else {
                    nodeCosineSim.add(.0);
                }
                double edgeWeightRatio = EdgeWeight.getInstance().getEdgeWeightRatio(from, to, e);
                Coords2D c = new Coords2D(neighbourDir.getItem(0), neighbourDir.getItem(1));

                edgeWeightsVals.add(edgeWeightRatio);
                nodeVals.add(c);
            }
        }
    }

    private void endRound(Graph graph) {
        for (Node n : graph.getNodes()) {
            List<Coords2D> nodeVals = state.get(n.getId());
            List<Double> nodeCosineSim = cosineSim.get(n.getId());
            List<Double> edgeWeightsVals = edgeWeights.get(n.getId());

            FloatList dir = VelocityProcessorStage.getVelocityVector(n);
            assert nodeVals.size() == nodeCosineSim.size() : "List size differ";

            float sumX = 0;
            float sumY = 0;
            float sumWeight = 0;
            assert nodeVals.size() == graph.getDegree(n) + 1;

            for (int i = 0; i < nodeVals.size(); i++) {
                Coords2D c = nodeVals.get(i);
                double weight;

                if (cosineAverage) {
                    weight = nodeCosineSim.get(i);
                } else if (edgeWeightsAverage) {
                    weight = edgeWeightsVals.get(i);
                } else {
                    weight = 1;
                }

                sumX += c.x * weight;
                sumY += c.y * weight;
                sumWeight += weight;
            }

            sumX /= sumWeight;
            sumY /= sumWeight;

            float x = (1 - inhibitFactor) * sumX + inhibitFactor * dir.getItem(0);
            float y = (1 - inhibitFactor) * sumY + inhibitFactor * dir.getItem(1);
            n.getAttributes().setValue(RESULT_VECTOR, new FloatList(new Float[]{x, y}));
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged
    ) {
        info("SmootheningStage: \n");
        if (step++ == 0) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        initialize(g);
        for (int i = 0; i < noRounds; i++) {
            startRound(g);
            runRound(from, to, g);
            endRound(g);
        }

        cosineSimWriter.println("from " + from + " to " + to);
        cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, 1);
        cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, 2);

        speedSimWriter.println("from " + from + " to " + to);
        speedGroupSimilarity.printGroupSimilarity(speedSimWriter, 1);
        speedGroupSimilarity.printGroupSimilarity(speedSimWriter, 2);
    }

    @Override
    public void setup() {
        step = 0;
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(RESULT_VECTOR) == false) {
            nodesTable.addColumn(RESULT_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

        state.clear();
        cosineSim.clear();
        for (Node n : graphModel.getGraph().getNodes()) {
            state.put(n.getId(), new ArrayList<Coords2D>());
            cosineSim.put(n.getId(), new ArrayList<Double>());
            edgeWeights.put(n.getId(), new ArrayList<Double>());
            n.getAttributes().setValue(RESULT_VECTOR, null);
        }

        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            cosineSimWriter = new PrintWriter(new File(resultsDir, "cosine_sim_smoothened"), "UTF-8");
            speedSimWriter = new PrintWriter(new File(resultsDir, "speed_sim_smoothened"), "UTF-8");
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
