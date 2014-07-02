package crowdlanes.stages;

import crowdlanes.EdgeWeight;
import crowdlanes.embeddings.MaximalMatchingCoarsening;
import crowdlanes.embeddings.MultiLevelLayout;
import crowdlanes.embeddings.RandomLayout;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.spi.Layout;
import org.openide.util.Lookup;

public class EmbeddingStage extends PipelineStage {

    private final static Long randomSeed = 42L;
    public final static String SECTION = "Embedding";
    public final static String YIFANHU_LAYOUT = "YifanHu";
    public final static String FORCE_ATLAS2_LAYOUT = "ForceAtlas2";

    private boolean initAlgo;
    private boolean endAlgo;
    private GraphModel graphModel;

    private int noIters;
    private final Layout layout;
    private final String embeddingType;

    private final boolean useEdgeWeights;

    public EmbeddingStage(String embeddingType, int noIters, boolean useEdgeWeights) {
        this.noIters = noIters;
        this.embeddingType = embeddingType;
        this.useEdgeWeights = useEdgeWeights;
        ForceAtlas2 forceAltasLayout = new ForceAtlas2(null);
        YifanHuLayout yifanHuLayout = new YifanHuLayout(null, new StepDisplacement(1f));

        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

        switch (embeddingType) {
            case YIFANHU_LAYOUT:
                initYifanHu(yifanHuLayout);
                layout = yifanHuLayout;
                break;
            case FORCE_ATLAS2_LAYOUT:
                initForceAtlas2(forceAltasLayout);
                layout = forceAltasLayout;
                break;
            default:
                throw new IllegalArgumentException("Unkown embedding type: " + embeddingType);
        }

        initializeEmbedding();
    }

    private void initializeEmbedding() {

        Layout random = new RandomLayout(null, randomSeed, 1000);
        random.setGraphModel(graphModel);
        random.initAlgo();
        random.goAlgo();

        MultiLevelLayout multiLevelLayout = new MultiLevelLayout(null, new MaximalMatchingCoarsening());
        multiLevelLayout.resetPropertiesValues();
        multiLevelLayout.setGraphModel(graphModel);
        multiLevelLayout.initAlgo();

        while (!multiLevelLayout.isConverged()) {
            multiLevelLayout.goAlgo();
        }
        multiLevelLayout.endAlgo();
    }


    public void run(double from, double to, boolean hasChanged) {
        info(embeddingType + " Stage: ");

        if (useEdgeWeights) {
            EdgeWeight.getInstance().setEdgeWeights(from, to);
        }

        if (initAlgo) {
            layout.initAlgo();
        }

        for (int i = 0; i < noIters && layout.canAlgo(); i++) {
            layout.goAlgo();
            info("*");
        }

        if (endAlgo) {
            layout.endAlgo();
        }

        info("\n");
    }

    private void initYifanHu(YifanHuLayout yifanHuLayout) {
        yifanHuLayout.setGraphModel(graphModel);
        yifanHuLayout.resetPropertiesValues();
        noIters = Integer.MAX_VALUE;
        initAlgo = true;
        endAlgo = true;
    }

    private void initForceAtlas2(ForceAtlas2 forceAltasLayout) {
        forceAltasLayout.setGraphModel(graphModel);
        forceAltasLayout.resetPropertiesValues();
        forceAltasLayout.setAdjustSizes(false);
        forceAltasLayout.setThreadsCount(1);
        if (!useEdgeWeights) {
            forceAltasLayout.setEdgeWeightInfluence(0.0);
        } else {
            forceAltasLayout.setEdgeWeightInfluence(1.0);
        }
        forceAltasLayout.initAlgo();
        graphModel.getGraphVisible().readUnlockAll();
        initAlgo = false;
        endAlgo = false;
    }

    public void setup() {
    }

    public void tearDown() {
        if (layout instanceof ForceAtlas2) {
            ((ForceAtlas2) layout).endAlgo();
        }
    }
}
