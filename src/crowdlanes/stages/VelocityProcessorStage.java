package crowdlanes.stages;

import com.google.common.collect.EvictingQueue;
import static crowdlanes.config.ConfigParamNames.*;
import crowdlanes.config.CurrentConfig;
import crowdlanes.util.GraphUtil;
import java.io.PrintWriter;
import java.util.HashMap;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

public class VelocityProcessorStage extends PipelineStage {

    public final static String VELOCITY_VECTOR = "VelocityVector";

    private final HashMap<String, EvictingQueue<Vector2D>> velocityVectors;
    private final SpeedSimilarityStage sss;
    private final CosineSimilarityStage csc;
    private int crrWindowSize;
    private Integer windowSize;

    public VelocityProcessorStage() {
        super();
        velocityVectors = new HashMap<>();
        sss = new SpeedSimilarityStage("speed_sim", VELOCITY_VECTOR);
        csc = new CosineSimilarityStage("cosine_sim", VELOCITY_VECTOR);
        addNodeColumn(VELOCITY_VECTOR, AttributeType.LIST_DOUBLE);
    }

    private void setAverageVelocity(Node n) {
        String id = n.getNodeData().getId();
        EvictingQueue<Vector2D> vec = velocityVectors.get(id);
        if (vec.size() < 2) {
            return;
        }

        DescriptiveStatistics dsX = new DescriptiveStatistics();
        DescriptiveStatistics dsY = new DescriptiveStatistics();

        /*
         SimpleRegression regression = new SimpleRegression();
         for (Vector2D v : vec) {
         regression.addData(v.getX(), v.getY());
         } 
         System.err.println("angle: " +  Math.toDegrees(Math.atan(regression.getSlope())));
         */
        Vector2D prev = null;
        for (Vector2D crr : vec) {
            if (prev == null) {
                prev = crr;
                continue;
            }

            Vector2D diff = crr.subtract(prev);
            dsX.addValue(diff.getX());
            dsY.addValue(diff.getY());
            prev = crr;
        }

        n.getAttributes().setValue(VELOCITY_VECTOR, new DoubleList(new Double[]{dsX.getMean(), dsY.getMean()}));

    }

    private void updateVelocityVector(Node n) {
        String id = n.getNodeData().getId();
        EvictingQueue<Vector2D> vec = velocityVectors.get(id);
        Vector2D c = new Vector2D(n.getNodeData().x(), n.getNodeData().y());
        vec.offer(c);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        info("VelocityProcessor: ");

        Graph g = graphModel.getGraphVisible();
        for (Node n : g.getNodes()) {
            updateVelocityVector(n);
            setAverageVelocity(n);
        }
        info("\n");

        if (GraphUtil.isNodeColumnNull(VelocityProcessorStage.VELOCITY_VECTOR)) {
            return;
        }

        sss.run(from, to, hasChanged);
        csc.run(from, to, hasChanged);
    }

    public static Vector2D getVelocityVector(Node n) {
        return GraphUtil.getVector(n, VELOCITY_VECTOR);
    }

    @Override
    public void setup(CurrentConfig cc) {
        sss.setup(cc);
        csc.setup(cc);

        this.windowSize = cc.getIntegerValue(CONFIG_PARAM_VELOCITY_VEC_WINDOW_SIZE);
        if (windowSize < 0) {
            throw new IllegalArgumentException("Window size cannot be negative");
        }

        for (Node n : graphModel.getGraphVisible().getNodes()) {
            String id = n.getNodeData().getId();
            EvictingQueue<Vector2D> q = EvictingQueue.create(windowSize);
            velocityVectors.put(id, q);
        }
    }

    @Override
    public void tearDown() {
        velocityVectors.clear();
        sss.tearDown();
        csc.tearDown();
    }

    @Override
    public void printParams(PrintWriter pw) {
        pw.println(CONFIG_PARAM_VELOCITY_VEC_WINDOW_SIZE + ": " + windowSize);
    }
}
