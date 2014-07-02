package crowdlanes;

import java.io.FileNotFoundException;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Lookup;

public class EdgeWeight {

    private static final EdgeWeight INSTANCE = new EdgeWeight();
    private AttributeColumn edgeColumn;

    private EdgeWeight() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static EdgeWeight getInstance() {
        return INSTANCE;
    }
    
    public void setup()  {        
        AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
        edgeColumn = am.getEdgeTable().getColumn(DynamicModel.TIMEINTERVAL_COLUMN);
    }

    public int getEdgeWeighForInterval(double from, double to, Edge edge) {
        int weight = 0;
        if (edgeColumn != null) {
            Object obj = edge.getEdgeData().getAttributes().getValue(edgeColumn.getIndex());
            if (obj != null) {
                TimeInterval timeInterval = (TimeInterval) obj;
                weight = timeInterval.getValues(from, to).size();
                /*
                for (int i = (int) from; i < (int) to; i++) {
                    if (timeInterval.isInRange(i, i)) {
                        weight += 1;
                    }
                }
                */
            }
        }
        return weight;
    }
    
    public double getEdgeWeightRatio(double from, double to, Edge edge) {
        double duration = to - from;
        return getEdgeWeighForInterval(from, to, edge) / duration;
    }

    public void setEdgeWeights(double from, double to) {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        Graph g = graphModel.getGraphVisible();

        float duration = (int) to - (int) from;
        float ratio;
        for (Edge e : g.getEdges()) {
            ratio = getEdgeWeighForInterval(from, to, e) / duration;
            e.setWeight(ratio);
        }
    }
}
