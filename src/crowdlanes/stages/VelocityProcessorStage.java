package crowdlanes.stages;

import crowdlanes.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class VelocityProcessorStage extends PipelineStage {

    public final static String SECTION = "VelocityVector";
    public final static String VELOCITY_VECTOR = "VelocityVector";

    private final HashMap<String, LinkedList<Coords2D>> velocityVectors;
    private final SpeedGroupSimilarity speedGroupSimilarity;
    private final CosineGroupSimilarity cosineGroupSimilarity;
    private PrintWriter cosineSimWriter;
    private PrintWriter speedSimWriter;
    private int crrWindowSize;
    private final int windowSize;
    private final GraphModel graphModel;

    public VelocityProcessorStage(int windowSize) {
        if (windowSize < 0) {
            throw new IllegalArgumentException("Window size cannot be negative");
        }

        this.windowSize = windowSize;
        velocityVectors = new HashMap<>();
        cosineGroupSimilarity = new CosineGroupSimilarity(VELOCITY_VECTOR);
        speedGroupSimilarity = new SpeedGroupSimilarity((VELOCITY_VECTOR));
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        AttributeTable nodesTable = attributeController.getModel().getNodeTable();
        if (nodesTable.hasColumn(VELOCITY_VECTOR) == false) {
            nodesTable.addColumn(VELOCITY_VECTOR, AttributeType.LIST_FLOAT, AttributeOrigin.COMPUTED);
        }

    }

    private void setAverageVelocity(Node n) {
        String id = n.getNodeData().getId();
        LinkedList<Coords2D> vec = velocityVectors.get(id);
        if (vec.size() < 2) {
            return;
        }

        int count = 0;
        double displacementX = 0;
        double displacementY = 0;
        for (int i = 0; i < vec.size() - 1; i++) {
            Coords2D crr = vec.get(i);
            Coords2D next = vec.get(i + 1);
            displacementX += (next.x - crr.x);
            displacementY += (next.y - crr.y);
            count++;
        }

        float x = (float) (displacementX / count);
        float y = (float) (displacementY / count);

        n.getAttributes().setValue(VELOCITY_VECTOR, new FloatList(new Float[]{x, y}));
    }

    private void updateVelocityVector(Node n) {
        String id = n.getNodeData().getId();
        LinkedList<Coords2D> vec = velocityVectors.get(id);

        if (vec.size() == windowSize) {
            vec.removeFirst();
        }

        Coords2D c = new Coords2D(n.getNodeData().x(), n.getNodeData().y());
        vec.addLast(c);
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
        
        crrWindowSize = Math.min(crrWindowSize + 1, windowSize);
        if (crrWindowSize < 2) {
            return;
        }

        cosineSimWriter.println("from " + from + " to " + to);
        cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, 1);
        cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, 2);

        speedSimWriter.println("from " + from + " to " + to);
        speedGroupSimilarity.printGroupSimilarity(speedSimWriter, 1);
        speedGroupSimilarity.printGroupSimilarity(speedSimWriter, 2);

    }

    public static FloatList getVelocityVector(Node n) {
        FloatList vals = (FloatList) n.getAttributes().getValue(VELOCITY_VECTOR);
        return vals;
    }

    @Override
    public void setup() {
        crrWindowSize = 0;
        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            cosineSimWriter = new PrintWriter(new File(resultsDir, "cosine_sim"), "UTF-8");
            speedSimWriter = new PrintWriter(new File(resultsDir, "speed_sim"), "UTF-8");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        for (Node n : graphModel.getGraphVisible().getNodes()) {
            String id = n.getNodeData().getId();
            velocityVectors.put(id, new LinkedList<Coords2D>());
        }
    }

    @Override
    public void tearDown() {
        velocityVectors.clear();
        cosineSimWriter.close();
        speedSimWriter.close();
    }

}
