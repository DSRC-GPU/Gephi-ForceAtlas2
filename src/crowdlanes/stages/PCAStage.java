package crowdlanes.stages;

import crowdlanes.GraphUtil;
import crowdlanes.Simulation;
import crowdlanes.config.CurrentConfig;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import ml.subspace.KernelPCA;
import ml.utils.ArrayOperator;
import ml.utils.Matlab;
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

public class PCAStage extends PipelineStage {

    public static final String PCA_PHI_FINE = "PCA_PHI_FINE";
    public static final String PCA_PHI_COARSE = "PCA_PHI_COARSE";
    private GraphModel graphModel;

    public PCAStage() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(PCA_PHI_FINE) == false) {
            nodesTable.addColumn(PCA_PHI_FINE, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
            nodesTable.addColumn(PCA_PHI_COARSE, AttributeType.DOUBLE, AttributeOrigin.COMPUTED);
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
            
        if (GraphUtil.isColumnNull(SmootheningStage.FINE_SMOOTHENING) || GraphUtil.isColumnNull(SmootheningStage.COARSE_SMOOTHENING)) {
            return;
        }

        Graph g = graphModel.getGraphVisible();

        int nodeCount = g.getNodeCount();
        double[][] data = new double[2 * nodeCount][2];

        Node[] nodes = g.getNodes().toArray();
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            FloatList fine = GraphUtil.getVector(n, SmootheningStage.FINE_SMOOTHENING);
            FloatList coarse = GraphUtil.getVector(n, SmootheningStage.COARSE_SMOOTHENING);
            data[2 * i][0] = fine.getItem(0);
            data[2 * i][1] = fine.getItem(1);
            data[2 * i + 1][0] = coarse.getItem(0);
            data[2 * i + 1][1] = coarse.getItem(1);
        }

        Matrix X = new DenseMatrix(data);
        //Matrix R = pca(X, 1);
        Matrix R = KernelPCA.run(X, 1);
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            n.getAttributes().setValue(PCA_PHI_FINE, R.getEntry(2 * i, 0));
            n.getAttributes().setValue(PCA_PHI_COARSE, R.getEntry(2 * i + 1, 0));
        }
    }

    private Matrix pca(Matrix X, int r) {
        int N = Matlab.size(X, 1);
        double[] S = Matlab.sum(X).getPr();
        ArrayOperator.divideAssign(S, N);
        X = X.copy();
        int M = X.getColumnDimension();
        double s = 0.0D;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                s = S[j];
                if (s != 0.0D) {
                    X.setEntry(i, j, X.getEntry(i, j) - s);
                }
            }
        }
        Matrix XT = X.transpose();
        Matrix Psi = XT.mtimes(X);

        return X.mtimes(Matlab.eigs(Psi, r, "lm")[0]);
    }

    @Override
    public void setup(CurrentConfig cc) {

    }

    @Override
    public void tearDown() {
    }
}
