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

public class Dop_VelocityVectors extends PipelineStage {

    private SmootheningScalarStage xFine;
    private SmootheningScalarStage yFine;
    private SmootheningScalarStage xCoarse;
    private SmootheningScalarStage yCoarse;
    private Float phiFine;
    private Float phiCoarse;
    private Integer noRoundsFine;
    private Integer noRoundsCoarse;
    private String avgMethod;

    public Dop_VelocityVectors() {
        super();
        addNodeColumn(PCA_PHI_FINE, AttributeType.DOUBLE);
        addNodeColumn(PCA_PHI_COARSE, AttributeType.DOUBLE);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isNodeColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        xFine.run(g, from, to, hasChanged);
        yFine.run(g, from, to, hasChanged);
        xCoarse.run(g, from, to, hasChanged);
        yCoarse.run(g, from, to, hasChanged);
        runPCA(g);
    }

    @Override
    public void setup(CurrentConfig cc) {
        SmootheningDataProvider sdpX = new SmootheningDataProvider() {

            @Override
            public double getValue(Node n) {
                Vector2D velocityVector = VelocityProcessorStage.getVelocityVector(n);
                return velocityVector.getX();
            }
        };

        SmootheningDataProvider sdpY = new SmootheningDataProvider() {

            @Override
            public double getValue(Node n) {
                Vector2D velocityVector = VelocityProcessorStage.getVelocityVector(n);
                return velocityVector.getY();
            }
        };

        phiFine = cc.getFloatValue(CONFIG_PARAM_SMOOTHENING_PHI_FINE);
        phiCoarse = cc.getFloatValue(CONFIG_PARAM_SMOOTHENING_PHI_COARSE);
        noRoundsFine = cc.getIntegerValue(CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE);
        noRoundsCoarse = cc.getIntegerValue(CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE);
        avgMethod = cc.getStringValue(CONFIG_PARAM_SMOOTHENING_AVG_WEIGHTS);

        xFine = new SmootheningScalarStage(phiFine, noRoundsFine, avgMethod, sdpX);
        yFine = new SmootheningScalarStage(phiFine, noRoundsFine, avgMethod, sdpY);

        xCoarse = new SmootheningScalarStage(phiCoarse, noRoundsCoarse, avgMethod, sdpX);
        yCoarse = new SmootheningScalarStage(phiCoarse, noRoundsCoarse, avgMethod, sdpY);

    }

    @Override
    public void tearDown() {
    }

    private void runPCA(Graph g) {
        int nodeCount = g.getNodeCount();
        double[][] data = new double[2 * nodeCount][2];

        Node[] nodes = g.getNodes().toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            data[2 * i][0] = xFine.getValue(n);
            data[2 * i][1] = yFine.getValue(n);
            data[2 * i + 1][0] = xCoarse.getValue(n);
            data[2 * i + 1][1] = yCoarse.getValue(n);
        }

        Matrix X = new DenseMatrix(data);
        Matrix R = PCA.run(X, 1);
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            n.getAttributes().setValue(PCA_PHI_FINE, R.getEntry(2 * i, 0));
            n.getAttributes().setValue(PCA_PHI_COARSE, R.getEntry(2 * i + 1, 0));
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
