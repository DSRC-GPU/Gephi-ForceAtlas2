package crowdlanes.stages;

import crowdlanes.GraphUtil;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class PCADoPStage extends PipelineStage {

    public final static String DOP_VALUE = "DoP_PCA";
    public final static String EDGE_CUT = "Edge_Cut";
    private int total_incorrect_cuts;
    private int total_correct_cuts;
    private int total_missed_cuts;
    private int incorrect_cuts;
    private int correct_cuts;
    private int missed_cuts;
    private GraphModel graphModel;
    private PCAStage pcaStage;
    private final float phi1;
    private final float phi2;

    public PCADoPStage(float phi1, float phi2) {
        if (phi1 > phi2) {
            throw new IllegalArgumentException("phi1 value must be smaller then phi2");
        }

        pcaStage = new PCAStage();
        this.phi1 = phi1;
        this.phi2 = phi2;

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);

        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(DOP_VALUE) == false) {
            nodesTable.addColumn(DOP_VALUE, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        }

        AttributeTable edgesTable = attributeController.getModel().getEdgeTable();
        if (edgesTable.hasColumn(EDGE_CUT) == false) {
            edgesTable.addColumn(EDGE_CUT, AttributeType.BOOLEAN, AttributeOrigin.COMPUTED);
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

    @Override
    public void run(double from, double to, boolean hasChanged) {

        pcaStage.run(from, to, hasChanged);
        Graph g = graphModel.getGraphVisible();
        for (Node n : g.getNodes()) {
            Double fine = (Double) n.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
            Double coarse = (Double) n.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);
            computeDop(n, fine, coarse);
        }

        setEdgesStatus(g);
        getEdgesStat(g);
        cutEdges(g);

        System.err.println("Incorrect cuts: " + incorrect_cuts);
        System.err.println("Correct cuts: " + correct_cuts);
        System.err.println("Missed cuts: " + missed_cuts);
    }

    private void computeDop(Node n, Double fine, Double coarse) {
        n.getAttributes().setValue(DOP_VALUE, coarse - fine);
    }

    private boolean isEdgeCut(Node n1, Node n2) {
        double dop1 = (Double) n1.getAttributes().getValue(DOP_VALUE);
        double dop2 = (Double) n2.getAttributes().getValue(DOP_VALUE);

        /*
         if (!GraphUtil.sameGroup(n1, n2) && dop1 * dop2 >= 0) {
         Double p11 = (Double) n1.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
         Double p12 = (Double) n1.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);
         Double p21 = (Double) n2.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
         Double p22 = (Double) n2.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);

         System.err.println("Missed cut:" + dop1 + " " + dop2 + " ( " + p11 + " , " + p12 + " )( " + p21 + " , " + p22 + " )");

         }

         if (GraphUtil.sameGroup(n1, n2) && dop1 * dop2 < 0) {
         Double p11 = (Double) n1.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
         Double p12 = (Double) n1.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);
         Double p21 = (Double) n2.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
         Double p22 = (Double) n2.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);

         System.err.println("Incorrect cut:" + dop1 + " " + dop2 + " ( " + p11 + " , " + p12 + " )( " + p21 + " , " + p22 + " )");

         }

         if (!GraphUtil.sameGroup(n1, n2) && dop1 * dop2 < 0) {
         Double p11 = (Double) n1.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
         Double p12 = (Double) n1.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);
         Double p21 = (Double) n2.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
         Double p22 = (Double) n2.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);

         System.err.println("Correct cut:" + dop1 + " " + dop2 + " ( " + p11 + " , " + p12 + " )( " + p21 + " , " + p22 + " )");
         }
         */
        return dop1 * dop2 < 0;
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

    @Override
    public void setup() {
        total_incorrect_cuts = 0;
        total_correct_cuts = 0;
        total_missed_cuts = 0;
        pcaStage.setup();
    }

    @Override
    public void tearDown() {
        pcaStage.tearDown();
        System.err.println("total incorrect cuts: " + getIncorrectCuts());
        System.err.println("total correct cuts: " + getCorrectCuts());
        System.err.println("total missed cuts: " + getMissedCuts());
    }
}
