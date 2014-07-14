package crowdlanes.stages;

import crowdlanes.config.CurrentConfig;
import java.io.PrintWriter;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Lookup;

public abstract class PipelineStage {

    protected GraphModel graphModel;
    protected AttributeController attributeController;

    public PipelineStage() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        attributeController = Lookup.getDefault().lookup(AttributeController.class);
    }

    protected AttributeColumn addNodeColumn(String name, AttributeType type) {
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(name) == false) {
            nodesTable.addColumn(name, type, AttributeOrigin.COMPUTED);
        }
        
        return nodesTable.getColumn(name);
    }
    
     protected AttributeColumn addEdgeColumn(String name, AttributeType type) {
        AttributeTable edgesTable = attributeController.getModel().getEdgeTable();
        if (edgesTable.hasColumn(name) == false) {
            edgesTable.addColumn(name, type, AttributeOrigin.COMPUTED);
        }
        
        return edgesTable.getColumn(name);
    }

    public static boolean INFO = true;

    public abstract void run(double from, double to, boolean hasChanged);

    public abstract void setup(CurrentConfig cc);

    public abstract void tearDown();

    public void printParams(PrintWriter pw) {
    }

    protected void info(String msg) {
        if (INFO) {
            System.err.print(msg);
        }
    }
}
