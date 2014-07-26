package crowdlanes.stages.dop;

import static crowdlanes.config.ConfigParamNames.*;
import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.PipelineStage;
import crowdlanes.stages.VelocityProcessorStage;
import static crowdlanes.stages.dop.Dop.PCA_PHI_COARSE;
import static crowdlanes.stages.dop.Dop.PCA_PHI_FINE;
import crowdlanes.stages.smoothening.SmootheningDataProvider;
import crowdlanes.stages.smoothening.SmootheningScalarStage;
import crowdlanes.util.GraphUtil;
import crowdlanes.util.PCA;
import java.io.PrintWriter;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class Dop_PCA extends PipelineStage {

    private static final String PCA_VELOCITY_VECTOR = "PCA_VELOCITY_VECTOR";

    private SmootheningScalarStage smootheningFine;
    private SmootheningScalarStage smootheningCoarse;
    private Float phiFine;
    private Float phiCoarse;
    private Integer noRoundsFine;
    private Integer noRoundsCoarse;
    private String avgMethod;

    public Dop_PCA() {
        super();
        addNodeColumn(PCA_PHI_FINE, AttributeType.DOUBLE);
        addNodeColumn(PCA_PHI_COARSE, AttributeType.DOUBLE);
        addNodeColumn(PCA_VELOCITY_VECTOR, AttributeType.DOUBLE);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isNodeColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        runPCA(g);
        smootheningFine.run(g, from, to, hasChanged);
        smootheningCoarse.run(g, from, to, hasChanged);

        for (Node n : g.getNodes()) {
            n.getAttributes().setValue(PCA_PHI_FINE, smootheningFine.getValue(n));
            n.getAttributes().setValue(PCA_PHI_COARSE, smootheningCoarse.getValue(n));
        }
    }

    @Override
    public void setup(CurrentConfig cc) {
        SmootheningDataProvider sdp = new SmootheningDataProvider() {

            @Override
            public double getValue(Node n) {
                return (double) n.getAttributes().getValue(PCA_VELOCITY_VECTOR);
            }
        };

        phiFine = cc.getFloatValue(CONFIG_PARAM_SMOOTHENING_PHI_FINE);
        phiCoarse = cc.getFloatValue(CONFIG_PARAM_SMOOTHENING_PHI_COARSE);
        noRoundsFine = cc.getIntegerValue(CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE);
        noRoundsCoarse = cc.getIntegerValue(CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE);
        avgMethod = cc.getStringValue(CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS);

        smootheningFine = new SmootheningScalarStage(phiFine, noRoundsFine, avgMethod, sdp);
        smootheningCoarse = new SmootheningScalarStage(phiCoarse, noRoundsCoarse, avgMethod, sdp);
    }

    @Override
    public void tearDown() {
    }

    private void runPCA(Graph g) {
        int nodeCount = g.getNodeCount();
        double[][] data = new double[nodeCount][2];

        Node[] nodes = g.getNodes().toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            Vector2D v = VelocityProcessorStage.getVelocityVector(n);
            data[i][0] = v.getX();
            data[i][1] = v.getY();
        }

        Matrix X = new DenseMatrix(data);
        Matrix R = PCA.run(X, 1);

        for (int i = 0; i < nodes.length; i++) {
            nodes[i].getAttributes().setValue(PCA_VELOCITY_VECTOR, R.getEntry(i, 0));
        }
    }

    @Override
    public void printParams(PrintWriter pw) {
        pw.println(CONFIG_PARAM_SMOOTHENING_PHI_FINE + ": " + phiFine);
        pw.println(CONFIG_PARAM_SMOOTHENING_PHI_COARSE + ": " + phiCoarse);
        pw.println(CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE + ": " + noRoundsFine);
        pw.println(CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE + ": " + noRoundsCoarse);
        pw.println(CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS + ": " + avgMethod);
    }
}
