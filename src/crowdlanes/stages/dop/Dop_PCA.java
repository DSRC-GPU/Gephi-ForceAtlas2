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
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class Dop_PCA extends PipelineStage {

    private static final String PCA_VELOCITY_VECTOR = "PCA_VELOCITY_VECTOR";

    private final PipelineStage smootheningFine;
    private final PipelineStage smootheningCoarse;
    private final GraphModel graphModel;
    private final SmootheningDataProvider sdp;

    public Dop_PCA() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(PCA_VELOCITY_VECTOR) == false) {
            nodesTable.addColumn(PCA_VELOCITY_VECTOR, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        }

        sdp = new SmootheningDataProviderImpl();
        smootheningFine = new SmootheningScalarStage(PCA_PHI_FINE, CONFIG_PARAM_SMOOTHENING_PHI_FINE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_FINE, sdp);
        smootheningCoarse = new SmootheningScalarStage(PCA_PHI_COARSE, CONFIG_PARAM_SMOOTHENING_PHI_COARSE, CONFIG_PARAM_SMOOTHENING_NO_ROUNDS_COARSE, sdp);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (GraphUtil.isNodeColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        runPCA(g);
        smootheningFine.run(from, to, hasChanged);
        smootheningCoarse.run(from, to, hasChanged);
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
        double[][] data = new double[nodeCount][2];

        Node[] nodes = g.getNodes().toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            FloatList vel = VelocityProcessorStage.getVelocityVector(n);
            data[i][0] = vel.getItem(0);
            data[i][1] = vel.getItem(1);
        }

        Matrix X = new DenseMatrix(data);
        Matrix R = PCA.run(X, 1);

        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            n.getAttributes().setValue(PCA_VELOCITY_VECTOR, R.getEntry(i, 0));
        }
    }

    private class SmootheningDataProviderImpl implements SmootheningDataProvider {

        @Override
        public double getValue(Node n) {
            return (double) n.getAttributes().getValue(PCA_VELOCITY_VECTOR);
        }
    }

    @Override
    public void printParams(PrintWriter pw) {
        smootheningCoarse.printParams(pw);
        smootheningFine.printParams(pw);
    }
}
