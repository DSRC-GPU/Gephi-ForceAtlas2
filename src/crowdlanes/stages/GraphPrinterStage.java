package crowdlanes.stages;

import crowdlanes.config.CurrentConfig;
import crowdlanes.config.ResultsDir;
import crowdlanes.stages.dop.Dop;
import crowdlanes.util.GraphUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.Exceptions;

public class GraphPrinterStage extends PipelineStage {

    private int step;
    private PrintWriter writer_nodes;
    private PrintWriter writer_edges;

    public GraphPrinterStage() {
        super();
    }

    public void printNodes() {
        AttributeModel attributeModel = attributeController.getModel();
        AttributeColumn groupColumn = attributeModel.getNodeTable().getColumn(ConnectedComponents.WEAKLY);

        Node[] nodes = graphModel.getGraphVisible().getNodes().toArray();
        Arrays.sort(nodes, new Comparator<Node>() {
            @Override
            public int compare(Node n1, Node n2) {
                return n1.getId() - n2.getId();
            }
        });

        writer_nodes.println("NumNodes: " + graphModel.getGraphVisible().getNodeCount());
        for (Node n : nodes) {
            Integer g = (Integer) n.getAttributes().getValue(GraphUtil.GROUP_COLUMN_NAME);
            Integer cc = groupColumn != null ? (Integer) n.getAttributes().getValue(groupColumn.getIndex()) : 1;
            String id = n.getNodeData().getId();

            writer_nodes.print(id + "," + g + "," + cc);
            writer_nodes.print("," + n.getNodeData().x() + " " + n.getNodeData().y());

            Vector2D velocity = VelocityProcessorStage.getVelocityVector(n);
            writer_nodes.print("," + velocity.getX() + " " + velocity.getY());

            /*
             Vector2D smoothFineVel = Dop.getFineVector(n);
             writer_nodes.print("," + smoothFineVel.getX() + " " + smoothFineVel.getY());

             Vector2D smoothCoarseVel = Dop.getCoarseVector(n);
             writer_nodes.print("," + smoothCoarseVel.getX() + " " + smoothCoarseVel.getY());
             */
            
            double pcaFine = Dop.getFinePCA(n);
            double pcaCoarse = Dop.getCoarsePCA(n);
            writer_nodes.print("," + pcaFine + "," + pcaCoarse);
            writer_nodes.println();
        }
    }

    public void printEdges() {
        writer_edges.println("NumEdges: " + graphModel.getGraphVisible().getEdgeCount());
        for (Edge e : graphModel.getGraphVisible().getEdges()) {
            writer_edges.println(e.getSource().getNodeData().getId() + " " + e.getTarget().getNodeData().getId());
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        if (step++ == 0) {
            return;
        }

        Graph g = graphModel.getGraphVisible();
        Double successRate = (Double) g.getAttributes().getValue(EdgeCutingAndCCDetectionStage.COMMUNITY_DETECTION);
        if (successRate == null)
            successRate = Double.NaN;
        
        writer_nodes.println("from " + from + " to " + to + " " + successRate);
        printNodes();

        writer_edges.println("from " + from + " to " + to);
        printEdges();
    }

    @Override
    public void setup(CurrentConfig cc) {
        step = 0;
        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            writer_nodes = new PrintWriter(new File(resultsDir, "nodes.txt"), "UTF-8");
            writer_edges = new PrintWriter(new File(resultsDir, "edges.txt"), "UTF-8");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        writer_nodes.println("ID,GROUP,CC,EMBEDDING_POS,VELOCITY_VECTOR,FINE_PCA,COARSE_PCA");
        //writer_nodes.println("ID,GROUP,CC,EMBEDDING_POS,VELOCITY_VECTOR,FINE_SMOOTH_VEL_VECTOR,COARSE_SMOOTH_VEL_VECTOR,FINE_PCA,COARSE_PCA");
        
    }

    @Override
    public void tearDown() {
        writer_edges.close();
        writer_nodes.close();
    }
}
