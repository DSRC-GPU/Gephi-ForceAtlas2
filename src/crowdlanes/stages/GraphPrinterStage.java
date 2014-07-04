package crowdlanes.stages;

import crowdlanes.GraphUtil;
import static crowdlanes.GraphUtil.getVector;
import crowdlanes.ResultsDir;
import crowdlanes.config.CurrentConfig;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class GraphPrinterStage extends PipelineStage {

    private int step;
    private PrintWriter writer_nodes;
    private PrintWriter writer_edges;
    private final GraphModel graphModel;

    public GraphPrinterStage() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
    }

    public void printNodes() {
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        AttributeColumn groupColumn = attributeModel.getNodeTable().getColumn(ConnectedComponents.WEAKLY);

        Node[] nodes = graphModel.getGraphVisible().getNodes().toArray();
        Arrays.sort(nodes, new Comparator<Node>() {
            public int compare(Node n1, Node n2) {
                return n1.getId() - n2.getId();
            }
        });
        

        writer_nodes.println("NumNodes: " + graphModel.getGraphVisible().getNodeCount());
        for (int i = 0; i < nodes.length; i++) {
            Node n = nodes[i];
            Integer g = (Integer) n.getAttributes().getValue(GraphUtil.GROUP_COLUMN_NAME);
            Integer cc = groupColumn != null ? (Integer) n.getAttributes().getValue(groupColumn.getIndex()) : 1;
            String id = n.getNodeData().getId();

            FloatList velocity = getVector(n, VelocityProcessorStage.VELOCITY_VECTOR);
            FloatList velocitySmoothened1 = getVector(n, SmootheningStage.FINE_SMOOTHENING);
            FloatList velocitySmoothened2 = getVector(n, SmootheningStage.COARSE_SMOOTHENING);
            double phi_fine = (Double) n.getAttributes().getValue(PCAStage.PCA_PHI_FINE);
            double phi_coarse = (Double) n.getAttributes().getValue(PCAStage.PCA_PHI_COARSE);

            writer_nodes.print(id + " " + g + " " + cc);
            writer_nodes.print(" (" + n.getNodeData().x() + "," + n.getNodeData().y() + ")");
            writer_nodes.print(" (" + velocity.getItem(0) + "," + velocity.getItem(1) + ")");
            writer_nodes.print(" (" + velocitySmoothened1.getItem(0) + "," + velocitySmoothened1.getItem(1) + ")");
            writer_nodes.print(" (" + velocitySmoothened2.getItem(0) + "," + velocitySmoothened2.getItem(1) + ")");
            writer_nodes.print(" (" + phi_fine + "," + phi_coarse + ")");
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

        writer_nodes.println("from " + from + " to " + to);
        writer_edges.println("from " + from + " to " + to);
        printNodes();
        printEdges();
    }

    @Override
    public void setup(CurrentConfig cc) {
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
    }

    @Override
    public void tearDown() {
        writer_edges.close();
        writer_nodes.close();
    }
}
