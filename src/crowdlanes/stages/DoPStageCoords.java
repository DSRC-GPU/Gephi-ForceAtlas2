package crowdlanes.stages;

import static crowdlanes.config.ParamNames.*;
import crowdlanes.GraphUtil;
import static crowdlanes.GraphUtil.getVector;
import crowdlanes.Simulation.CurrentConfig;
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

public class DoPStageCoords extends PipelineStage {

    public final static String EDGE_CUT = "Edge_Cut";
    public final static String RESULT_VECTOR_COORDS = "DoPCoords";
    public final static String RESULT_VECTOR_ANGLE = "DoPAngle";

    private int total_incorrect_cuts;
    private int total_correct_cuts;
    private int total_missed_cuts;
    private int incorrect_cuts;
    private int correct_cuts;
    private int missed_cuts;
    private final GraphModel graphModel;

    private float phi_fine;
    private float phi_coarse;

    public DoPStageCoords() {

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(RESULT_VECTOR_COORDS) == false) {
            nodesTable.addColumn(RESULT_VECTOR_COORDS, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
            nodesTable.addColumn(RESULT_VECTOR_ANGLE, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        }
    }

    public int getIncorrectCuts() {
        return total_incorrect_cuts;
    }

    public int getCorrectCuts() {
        return total_correct_cuts;
    }

    public int getMissedCuts() {
        return total_missed_cuts;
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

    private void setEdgesStatus(Graph g) {
        for (Edge e : g.getEdges()) {
            e.getAttributes().setValue(EDGE_CUT, isEdgeCut(e.getSource(), e.getTarget()));
        }
    }

    private void getEdgesStat(Graph g) {
        missed_cuts = 0;
        correct_cuts = 0;
        incorrect_cuts = 0;
        for (Edge e : g.getEdges()) {
            Boolean isCut = (Boolean) e.getAttributes().getValue(EDGE_CUT);
            boolean sameGroup = GraphUtil.sameGroup(e.getSource(), e.getTarget());

            if (isCut) {
                if (sameGroup) {
                    incorrect_cuts++;
                } else {
                    correct_cuts++;
                }
            } else if (!sameGroup) {
                missed_cuts++;

            }
        }
        total_correct_cuts += correct_cuts;
        total_incorrect_cuts += incorrect_cuts;
        total_missed_cuts += missed_cuts;
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        
        if (GraphUtil.isColumnNull(SmootheningStage.FINE_SMOOTHENING) || GraphUtil.isColumnNull(SmootheningStage.COARSE_SMOOTHENING)) {
            return;
        }

        info("DOP_Coords_Stage:");

        Graph g = graphModel.getGraphVisible();
        for (Node n : g.getNodes()) {
            FloatList fine = getVector(n, SmootheningStage.FINE_SMOOTHENING);
            FloatList coarse = getVector(n, SmootheningStage.COARSE_SMOOTHENING);
            computeDop(n, fine, coarse);
        }

        setEdgesStatus(g);
        getEdgesStat(g);
        cutEdges(g);
        info("\n");

        System.err.println("Incorrect cuts: " + incorrect_cuts);
        System.err.println("Correct cuts: " + correct_cuts);
        System.err.println("Missed cuts: " + missed_cuts);
    }

    private void computeDop(Node n, FloatList fine, FloatList coarse) {
        float x = coarse.getItem(0) - fine.getItem(0);
        float y = coarse.getItem(1) - fine.getItem(1);
        n.getAttributes().setValue(RESULT_VECTOR_COORDS, new FloatList(new float[]{x, y}));
    }

    private boolean isEdgeCut(Node n, Node neigh) {
        FloatList dopN = (FloatList) n.getAttributes().getValue(RESULT_VECTOR_COORDS);
        FloatList dopNeigh = (FloatList) neigh.getAttributes().getValue(RESULT_VECTOR_COORDS);

        float x1 = dopN.getItem(0);
        float y1 = dopN.getItem(1);
        float x2 = dopNeigh.getItem(0);
        float y2 = dopNeigh.getItem(1);

        return (x1 * x2 < 0 || y1 * y2 < 0);
    }

    @Override
    public void setup(CurrentConfig cc) {
        this.phi_fine = (float) cc.getValue(CONFIG_PARAM_SMOOTHENING_PHI_FINE);
        this.phi_coarse = (float) cc.getValue(CONFIG_PARAM_SMOOTHENING_PHI_COARSE);

        if (phi_fine > phi_coarse) {
            throw new IllegalArgumentException("phi1 value must be smaller then phi2");
        }

        total_incorrect_cuts = 0;
        total_correct_cuts = 0;
        total_missed_cuts = 0;

    }

    @Override
    public void tearDown() {
        System.err.println("total incorrect cuts: " + getIncorrectCuts());
        System.err.println("total correct cuts: " + getCorrectCuts());
        System.err.println("total missed cuts: " + getMissedCuts());
    }

}
