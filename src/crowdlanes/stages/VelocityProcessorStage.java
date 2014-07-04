package crowdlanes.stages;

import com.google.common.collect.EvictingQueue;
import crowdlanes.*;
import crowdlanes.Simulation.CurrentConfig;
import static crowdlanes.config.ParamNames.*;
import java.util.HashMap;
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

public class VelocityProcessorStage extends PipelineStage {

    public final static String SECTION = "VelocityVector";
    public final static String VELOCITY_VECTOR = "VelocityVector";

    private final HashMap<String, EvictingQueue<Coords2D>> velocityVectors;
    private final SpeedSimilarityStage sss;
    private final CosineSimilarityStage csc;
    private int crrWindowSize;
    private final GraphModel graphModel;

    public VelocityProcessorStage() {

        velocityVectors = new HashMap<>();
        sss = new SpeedSimilarityStage("speed_sim", VELOCITY_VECTOR);
        csc = new CosineSimilarityStage("cosine_sim", VELOCITY_VECTOR);
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(VELOCITY_VECTOR) == false) {
            nodesTable.addColumn(VELOCITY_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }
    }

    private void setAverageVelocity(Node n) {
        String id = n.getNodeData().getId();
        EvictingQueue<Coords2D> vec = velocityVectors.get(id);
        if (vec.size() < 2) {
            return;
        }

        int count = 0;
        double displacementX = 0;
        double displacementY = 0;
        Coords2D prev = null;

        for (Coords2D crr : vec) {
            if (prev == null) {
                prev = crr;
                continue;
            }

            displacementX += (crr.x - prev.x);
            displacementY += (crr.y - prev.y);
            prev = crr;
            count++;
        }

        float x = (float) (displacementX / count);
        float y = (float) (displacementY / count);

        n.getAttributes().setValue(VELOCITY_VECTOR, new FloatList(new Float[]{x, y}));
    }

    private void updateVelocityVector(Node n) {
        String id = n.getNodeData().getId();
        EvictingQueue<Coords2D> vec = velocityVectors.get(id);
        Coords2D c = new Coords2D(n.getNodeData().x(), n.getNodeData().y());
        vec.offer(c);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        info("VelocityProcessor: ");

        Graph g = graphModel.getGraphVisible();
        for (Node n : g.getNodes()) {
            updateVelocityVector(n);
            setAverageVelocity(n);
            //info("*");
        }
        info("\n");

        if (GraphUtil.isColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        sss.run(from, to, hasChanged);
        csc.run(from, to, hasChanged);
    }

    public static FloatList getVelocityVector(Node n) {
        FloatList vals = (FloatList) n.getAttributes().getValue(VELOCITY_VECTOR);
        return vals;
    }

    @Override
    public void setup(CurrentConfig cc) {
        sss.setup(cc);
        csc.setup(cc);

        int windowSize = (int) cc.getValue(CONFIG_PARAM_VELOCITY_VEC_WINDOW_SIZE);
        if (windowSize < 0) {
            throw new IllegalArgumentException("Window size cannot be negative");
        }

        for (Node n : graphModel.getGraphVisible().getNodes()) {
            String id = n.getNodeData().getId();
            EvictingQueue<Coords2D> q = EvictingQueue.create(windowSize);
            velocityVectors.put(id, q);
        }
    }

    @Override
    public void tearDown() {
        velocityVectors.clear();
        sss.tearDown();
        csc.tearDown();
    }
}
