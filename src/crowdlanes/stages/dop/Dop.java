package crowdlanes.stages.dop;

import crowdlanes.config.ConfigParamNames;
import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.PipelineStage;
import crowdlanes.util.GraphUtil;
import java.io.PrintWriter;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class Dop extends PipelineStage {

    public final static String EDGE_CUT = "Edge_Cut";
    public static final String PCA_PHI_FINE = "PCA_PHI_FINE";
    public static final String PCA_PHI_COARSE = "PCA_PHI_COARSE";

    private int total_incorrect_cuts;
    private int total_correct_cuts;
    private int total_missed_cuts;
    private int incorrect_cuts;
    private int correct_cuts;
    private int missed_cuts;

    private PipelineStage dop;

    public Dop() {
        super();
        addEdgeColumn(EDGE_CUT, AttributeType.BOOLEAN);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        dop.run(from, to, hasChanged);

        if (GraphUtil.isNodeColumnNull(PCA_PHI_FINE) || GraphUtil.isNodeColumnNull(PCA_PHI_COARSE)) {
            return;
        }

        info("DOP_PCA_Stage:");
        Graph g = graphModel.getGraphVisible();
        setEdgesStatus(g);
        getEdgesStat(g);
        info("\n");

        System.err.println("\tIncorrect cuts: " + incorrect_cuts);
        System.err.println("\tCorrect cuts: " + correct_cuts);
        System.err.println("\tMissed cuts: " + missed_cuts);
    }

    @Override
    public void setup(CurrentConfig cc) {
        if (cc.getBooleanValue(ConfigParamNames.CONFIG_PARAM_USE_PCA_BEFORE_SMOOTHENING)) {
            dop = new Dop_PCA();
        } else {
            dop = new Dop_VelocityVectors();
        }

        dop.setup(cc);
        total_incorrect_cuts = 0;
        total_correct_cuts = 0;
        total_missed_cuts = 0;
    }

    @Override
    public void tearDown() {
        System.err.println("PCADop:");
        System.err.println("\tTotal incorrect cuts: " + getIncorrectCuts());
        System.err.println("\tTotal correct cuts: " + getCorrectCuts());
        System.err.println("\tTotal missed cuts: " + getMissedCuts());
    }

    private double getDop(Node n) {
        Double fine = (Double) n.getAttributes().getValue(PCA_PHI_FINE);
        Double coarse = (Double) n.getAttributes().getValue(PCA_PHI_COARSE);
        return coarse - fine;
    }

    private boolean isEdgeCut(Node n1, Node n2) {
        return getDop(n1) * getDop(n2) < 0;
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

    private void setEdgesStatus(Graph g) {
        for (Edge e : g.getEdges()) {
            e.getAttributes().setValue(EDGE_CUT, isEdgeCut(e.getSource(), e.getTarget()));
        }
    }

    public static double getFinePCA(Node n) {
        return (double) n.getAttributes().getValue(PCA_PHI_FINE);
    }

    public static double getCoarsePCA(Node n) {
        return (double) n.getAttributes().getValue(PCA_PHI_COARSE);
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
    public void printParams(PrintWriter pw) {
        dop.printParams(pw);
    }
}
