package crowdlanes.stages.smoothening;

import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.CosineSimilarityStage;
import crowdlanes.stages.PipelineStage;
import crowdlanes.stages.SpeedSimilarityStage;
import java.io.PrintWriter;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

public class SmootheningVelocityVectorStage extends PipelineStage {

    public final static String SMOOTHENING_COORDS_X = "smoothened_X";
    public final static String SMOOTHENING_COORDS_Y = "smoothened_Y";

    private final GraphModel graphModel;
    private final AttributeColumn outputColumn;
    private final AttributeColumn xOutputColumn;
    private final AttributeColumn yOutputColumn;
    private final SmootheningScalarStage xsmooth;
    private final SmootheningScalarStage ysmooth;
    private final SpeedSimilarityStage sss;
    private final CosineSimilarityStage csc;

    public SmootheningVelocityVectorStage(String outputColumn, String phiName, String noRoundsParamName) {

        sss = new SpeedSimilarityStage("speed_sim_" + outputColumn, outputColumn);
        csc = new CosineSimilarityStage("cosine_sim_" + outputColumn, outputColumn);

        xsmooth = new SmootheningScalarStage(SMOOTHENING_COORDS_X, phiName, noRoundsParamName, new SmootheningDataProvider() {

            @Override
            public double getValue(Node n) {
                return n.getNodeData().x();
            }
        });

        ysmooth = new SmootheningScalarStage(SMOOTHENING_COORDS_Y, phiName, noRoundsParamName, new SmootheningDataProvider() {

            @Override
            public double getValue(Node n) {
                return n.getNodeData().y();
            }
        });
        
        xOutputColumn = xsmooth.getOutputColumn();
        yOutputColumn = ysmooth.getOutputColumn();

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(outputColumn) == false) {
            this.outputColumn = nodesTable.addColumn(outputColumn, AttributeType.LIST_DOUBLE, AttributeOrigin.COMPUTED);
        } else {
            this.outputColumn = nodesTable.getColumn(outputColumn);
        }
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {

        xsmooth.run(from, to, hasChanged);
        ysmooth.run(from, to, hasChanged);

        for (Node n : graphModel.getGraphVisible().getNodes()) {
            Double x = (Double) n.getAttributes().getValue(xOutputColumn.getIndex());
            Double y = (Double) n.getAttributes().getValue(yOutputColumn.getIndex());
            n.getAttributes().setValue(outputColumn.getIndex(), new DoubleList(new Double[]{x, y}));
        }

        //csc.run(from, to, hasChanged);
        //sss.run(from, to, hasChanged);
    }

    @Override
    public void setup(CurrentConfig cc) {
        csc.setup(cc);
        sss.setup(cc);

        xsmooth.setup(cc);
        ysmooth.setup(cc);

    }

    @Override
    public void printParams(PrintWriter pw) {
        xsmooth.printParams(pw);
    }

    @Override
    public void tearDown() {
        csc.tearDown();
        sss.tearDown();
        xsmooth.tearDown();
        ysmooth.tearDown();
    }
}
