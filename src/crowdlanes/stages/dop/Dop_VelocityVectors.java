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

    public final static String SMOOTHENING_COORDS_FINE_X = "SMOOTHENING_COORDS_FINE_X";
    public final static String SMOOTHENING_COORDS_FINE_Y = "SMOOTHENING_COORDS_FINE_Y";
    public final static String SMOOTHENING_COORDS_COARSE_X = "SMOOTHENING_COORDS_COARSE_X";
    public final static String SMOOTHENING_COORDS_COARSE_Y = "SMOOTHENING_COORDS_COARSE_Y";

    private final SmootheningScalarStage xFine;
    private final SmootheningScalarStage yFine;
    private final SmootheningScalarStage xCoarse;
    private final SmootheningScalarStage yCoarse;

    public Dop_VelocityVectors() {
        super();
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

        addNodeColumn(PCA_PHI_FINE, AttributeType.DOUBLE);
        addNodeColumn(PCA_PHI_COARSE, AttributeType.DOUBLE);

        xFine = new SmootheningScalarStage(SMOOTHENING_COORDS_FINE_X, CONFIG_PARAM_SMOOTHENING_PHI_FINE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE, sdpX);
        yFine = new SmootheningScalarStage(SMOOTHENING_COORDS_FINE_Y, CONFIG_PARAM_SMOOTHENING_PHI_FINE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE, sdpY);

        xCoarse = new SmootheningScalarStage(SMOOTHENING_COORDS_COARSE_X, CONFIG_PARAM_SMOOTHENING_PHI_COARSE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE, sdpX);
        yCoarse = new SmootheningScalarStage(SMOOTHENING_COORDS_COARSE_Y, CONFIG_PARAM_SMOOTHENING_PHI_COARSE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE, sdpY);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isNodeColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        xFine.run(from, to, hasChanged);
        yFine.run(from, to, hasChanged);
        xCoarse.run(from, to, hasChanged);
        yCoarse.run(from, to, hasChanged);
        runPCA(g);
    }

    @Override
    public void setup(CurrentConfig cc) {
        xFine.setup(cc);
        yFine.setup(cc);
        xCoarse.setup(cc);
        yCoarse.setup(cc);
    }

    @Override
    public void tearDown() {
        xFine.tearDown();
        yFine.tearDown();
        xCoarse.tearDown();
        yCoarse.tearDown();
    }

    private void runPCA(Graph g) {
        int nodeCount = g.getNodeCount();
        double[][] data = new double[2 * nodeCount][2];

        Node[] nodes = g.getNodes().toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            data[2 * i][0] = (double) n.getAttributes().getValue(SMOOTHENING_COORDS_FINE_X);
            data[2 * i][1] = (double) n.getAttributes().getValue(SMOOTHENING_COORDS_FINE_Y);
            data[2 * i + 1][0] = (double) n.getAttributes().getValue(SMOOTHENING_COORDS_COARSE_X);
            data[2 * i + 1][1] = (double) n.getAttributes().getValue(SMOOTHENING_COORDS_COARSE_Y);
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
        xFine.printParams(pw);
        xCoarse.printParams(pw);
    }
}
