package crowdlanes.stages.dop;

import static crowdlanes.config.ConfigParamNames.*;
import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.PipelineStage;
import crowdlanes.stages.VelocityProcessorStage;
import static crowdlanes.stages.dop.Dop.PCA_PHI_COARSE;
import static crowdlanes.stages.dop.Dop.PCA_PHI_FINE;
import crowdlanes.stages.smoothening.SmootheningVelocityVectorStage;
import crowdlanes.util.GraphUtil;
import crowdlanes.util.PCA;
import java.io.PrintWriter;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class Dop_VelocityVectors extends PipelineStage {

    private final static String FINE_SMOOTHENING = "smoothened_fine";
    private final static String COARSE_SMOOTHENING = "smoothened_coarse";
    private final PipelineStage smootheningFine;
    private final PipelineStage smootheningCoarse;
    private final GraphModel graphModel;

    public Dop_VelocityVectors() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(PCA_PHI_FINE) == false) {
            nodesTable.addColumn(PCA_PHI_FINE, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        }

        if (nodesTable.hasColumn(PCA_PHI_COARSE) == false) {
            nodesTable.addColumn(PCA_PHI_COARSE, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        }

        smootheningFine = new SmootheningVelocityVectorStage(FINE_SMOOTHENING, CONFIG_PARAM_SMOOTHENING_PHI_FINE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE);
        smootheningCoarse = new SmootheningVelocityVectorStage(COARSE_SMOOTHENING, CONFIG_PARAM_SMOOTHENING_PHI_COARSE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isNodeColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        smootheningFine.run(from, to, hasChanged);
        smootheningCoarse.run(from, to, hasChanged);
        runPCA(g);
    }

    @Override
    public void setup(CurrentConfig cc) {
        smootheningFine.setup(cc);
        smootheningCoarse.setup(cc);
    }

    @Override
    public void tearDown() {
        smootheningCoarse.tearDown();
        smootheningFine.tearDown();
    }

    private void runPCA(Graph g) {
        int nodeCount = g.getNodeCount();
        double[][] data = new double[2 * nodeCount][2];

        Node[] nodes = g.getNodes().toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            DoubleList fine = (DoubleList) n.getAttributes().getValue(FINE_SMOOTHENING);
            DoubleList coarse = (DoubleList) n.getAttributes().getValue(COARSE_SMOOTHENING);
            data[2 * i][0] = fine.getItem(0);
            data[2 * i][1] = fine.getItem(1);
            data[2 * i + 1][0] = coarse.getItem(0);
            data[2 * i + 1][1] = coarse.getItem(1);
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
        smootheningCoarse.printParams(pw);
        smootheningFine.printParams(pw);
    }
}
