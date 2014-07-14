package crowdlanes.stages.embedding;

import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.PipelineStage;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicFloat;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class SetNodesPosStage extends PipelineStage {

    private final static String XPOS_COLUMN = "xpos";
    private final static String YPOS_COLUMN = "ypos";
    private AttributeColumn xpos;
    private AttributeColumn ypos;

    public SetNodesPosStage() {
        super();
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        Graph g = graphModel.getGraphVisible();
        for (Node n : g.getNodes()) {
            DynamicFloat xval = (DynamicFloat) n.getAttributes().getValue(xpos.getIndex());
            DynamicFloat yval = (DynamicFloat) n.getAttributes().getValue(ypos.getIndex());
            float x = xval.getValue(from, to, Estimator.LAST);
            float y = yval.getValue(from, to, Estimator.LAST);
            n.getNodeData().setX(x);
            n.getNodeData().setY(y);
        }
    }

    @Override
    public void setup(CurrentConfig cc) {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel model = ac.getModel();
        xpos = model.getNodeTable().getColumn(XPOS_COLUMN);
        ypos = model.getNodeTable().getColumn(YPOS_COLUMN);

        if (xpos == null || ypos == null) {
            throw new IllegalStateException("Gexf file does not have nodes positions");
        }
    }

    @Override
    public void tearDown() {
    }

}
