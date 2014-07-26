package crowdlanes.stages.embedding;

import crowdlanes.config.ConfigParamNames;
import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.PipelineStage;
import crowdlanes.util.GraphUtil;
import java.io.PrintWriter;
import java.util.HashMap;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;
import ml.subspace.MDS;
import ml.utils.Matlab;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.statistics.plugin.ConnectedComponents;

public class MovementStage extends PipelineStage {

    private boolean useGroundTruth;
    PipelineStage movementStage;
    private boolean once;

    private class DijkstraShortestPathAlgorithm2 extends DijkstraShortestPathAlgorithm {

        public DijkstraShortestPathAlgorithm2(Graph g, Node src) {
            super(g, src);
            timeInterval = null;
        }
    }

    private void computeMDS(Graph g) {
        ConnectedComponents cc = GraphUtil.getCC();
        if (cc.getConnectedComponentsCount() != 1) {
            return;
        }

        Node[] nodes = graphModel.getGraphVisible().getNodes().toArray();
        Matrix m = new DenseMatrix(nodes.length, nodes.length);

        for (Node src : nodes) {
            DijkstraShortestPathAlgorithm2 algo = new DijkstraShortestPathAlgorithm2(graphModel.getGraphVisible(), src);
            algo.compute();
            HashMap<NodeData, Double> distances = algo.getDistances();

            for (Node dst : nodes) {
                double d = distances.get(dst.getNodeData());
                m.setEntry(src.getId() - 1, dst.getId() - 1, d);
            }
        }
        //disp(m);
        //System.out.println("");
        //System.exit(0);
        Matrix o = Matlab.l2Distance(m, m);
        Matrix X = MDS.run(o, 2);
        //disp(X);
        for (int i = 0; i < nodes.length; i++) {
            double x = X.getEntry(i, 0);
            double y = X.getEntry(i, 1);
            nodes[i].getNodeData().setX((float) x);
            nodes[i].getNodeData().setX((float) y);
        }

    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        //computeMDS(graphModel.getGraphVisible());
        movementStage.run(from, to, hasChanged);
    }

    @Override
    public void setup(CurrentConfig cc) {
        useGroundTruth = cc.getBooleanValue(ConfigParamNames.CONFIG_PARAM_USE_GROUNDTRUTH);
        if (useGroundTruth) {
            movementStage = new SetNodesPosStage();
        } else {
            movementStage = new EmbeddingStage();
        }

        movementStage.setup(cc);
    }

    @Override
    public void tearDown() {
    }

    @Override
    public void printParams(PrintWriter pw) {
        pw.println(ConfigParamNames.CONFIG_PARAM_USE_GROUNDTRUTH + ": " + useGroundTruth);
        movementStage.printParams(pw);
    }

}
