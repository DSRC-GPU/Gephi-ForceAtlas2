package org.gephi.toolkit.demos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.gephi.graph.api.*;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DynamicProcessor;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class ForceAtlas2Printer {

    private final int noOfIterations;

    public ForceAtlas2Printer(String gexfFile, int noOfIterations) throws FileNotFoundException {
        PrintStream originalStream = System.out;
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
                //NO-OP
            }
        });

        System.setOut(dummyStream);
        importGraph(gexfFile);
        System.setOut(originalStream);
        this.noOfIterations = noOfIterations;
    }

    public void exportToPdf() {
        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("out.pdf"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return;
        }
    }

    public void runLayout() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        ForceAtlas2 layout = new ForceAtlas2(null);
        layout.setGraphModel(graphModel);
        layout.initAlgo();
        layout.resetPropertiesValues();

        Graph g = graphModel.getGraph();
        for (Node n : g.getNodes()) {
            n.getNodeData().setX(100);
            n.getNodeData().setY(100);
        }

        for (int i = 0; i < noOfIterations && layout.canAlgo(); i++) {
            layout.goAlgo();
            printNodes();
        }
        layout.endAlgo();
    }

    public final void importGraph(String file_path) throws FileNotFoundException {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Import first file
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        File file = new File(file_path);
        if (file.isFile() == false) {
            throw new FileNotFoundException();
        }

        container = importController.importFile(file);

        //Initialize the DynamicProcessor - which will append the container to the workspace
        DynamicProcessor dynamicProcessor = new DynamicProcessor();
        dynamicProcessor.setDateMode(false);       //Set 'true' if you set real dates (ex: yyyy-mm-dd), it's double otherwise
        dynamicProcessor.setLabelmatching(true);   //Set 'true' if node matching is done on labels instead of ids

        //Set date for this file
        dynamicProcessor.setDate("2007");

        //Process the container using the DynamicProcessor
        importController.process(container, dynamicProcessor, workspace);
    }

    private void printNodes() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph g = graphModel.getGraph();

        List list = new ArrayList();
        NodeIterator it = g.getNodes().iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }

        Collections.sort(list, new Comparator<Node>() {
            public int compare(Node n1, Node n2) {
                return n1.getId() - n2.getId();
            }
        });

        for (Node n : g.getNodes()) {
            System.out.println(n.getId() + " " + n.getNodeData().x() + " " + n.getNodeData().y());
        }

        System.out.println("");
    }
}
