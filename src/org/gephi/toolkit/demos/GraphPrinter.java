package org.gephi.toolkit.demos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class GraphPrinter extends PipelineStage {

    private int step;
    private PrintWriter writer_nodes;
    private PrintWriter writer_edges;
    private final GraphModel graphModel;

    public GraphPrinter() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
    }

    public void printNodes() {
        writer_nodes.println("NumNodes: " + graphModel.getGraphVisible().getNodeCount());
        for (Node n : graphModel.getGraphVisible().getNodes()) {
            Integer g = (Integer) n.getAttributes().getValue("Group");
            String id = n.getNodeData().getId();
            writer_nodes.print(id + " " + g);

            FloatList velocity = (FloatList) n.getAttributes().getValue(VelocityProcessorStage.VELOCITY_VECTOR);
            FloatList velocitySmoothened = (FloatList) n.getAttributes().getValue(SmootheningStage.RESULT_VECTOR);

            writer_nodes.print(" (" + n.getNodeData().x() + " " + n.getNodeData().y() + ")");
            writer_nodes.print(" (" + velocity.getItem(0) + " " + velocity.getItem(1) + ")");
            writer_nodes.print(" (" + velocitySmoothened.getItem(0) + " " + velocitySmoothened.getItem(1) + ")");

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

        writer_nodes.println(from + "-" + to);
        writer_edges.println(from + "-" + to);
        printNodes();
        printEdges();
    }

    @Override
    public void setup() {
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
