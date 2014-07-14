package crowdlanes.stages.embedding;

import static crowdlanes.config.ConfigParamNames.*;
import crowdlanes.util.EdgeWeight;
import crowdlanes.config.CurrentConfig;
import crowdlanes.embeddings.MaximalMatchingCoarsening;
import crowdlanes.embeddings.MultiLevelLayout;
import crowdlanes.stages.PipelineStage;
import java.io.PrintWriter;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.spi.Layout;

public class EmbeddingStage extends PipelineStage {

    public final static String YIFANHU_LAYOUT = "YifanHu";
    public final static String FORCE_ATLAS2_LAYOUT = "ForceAtlas2";

    private boolean initAlgo;
    private boolean endAlgo;
    private Layout layout;


    private Integer seed;
    private int noIters;
    private String embeddingType;
    private boolean useEdgeWeights;

    public EmbeddingStage() {
        super();
    }

    private void initializeEmbedding(Integer seed) {

        MultiLevelLayout multiLevelLayout = new MultiLevelLayout(null, new MaximalMatchingCoarsening(seed), seed);
        multiLevelLayout.resetPropertiesValues();
        multiLevelLayout.setGraphModel(graphModel);
        multiLevelLayout.initAlgo();

        while (!multiLevelLayout.canAlgo()) {
            multiLevelLayout.goAlgo();
        }
        multiLevelLayout.endAlgo();
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        info(embeddingType + " Stage: ");

        if (useEdgeWeights) {
            EdgeWeight.setEdgeWeights(from, to);
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
        yifanHuLayout.setOptimalDistance(100f);
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

    public void setup(CurrentConfig cc) {
        this.seed = cc.getIntegerValue(CONFIG_PARAM_INITIAL_EMBEDDING_SEED);
        this.noIters = cc.getIntegerValue(CONFIG_PARAM_FORCE_ATLAS_NO_ITER);
        this.embeddingType = cc.getStringValue(CONFIG_PARAM_EMBEDDING_TYPE);
        this.useEdgeWeights = cc.getBooleanValue(CONFIG_PARAM_FORCE_ATLAS_USE_EDGE_WEIGHTS);

        switch (embeddingType) {
            case YIFANHU_LAYOUT:
                YifanHuLayout yifanHuLayout = new YifanHuLayout(null, new StepDisplacement(1f));
                initYifanHu(yifanHuLayout);
                layout = yifanHuLayout;
                break;
            case FORCE_ATLAS2_LAYOUT:
                ForceAtlas2 forceAltasLayout = new ForceAtlas2(null);
                initForceAtlas2(forceAltasLayout);
                layout = forceAltasLayout;
                break;
            default:
                throw new IllegalArgumentException("Unkown embedding type: " + embeddingType);
        }

        initializeEmbedding(seed);
    }

    public void tearDown() {
        if (!endAlgo) {
            layout.endAlgo();
        }
    }

    @Override
    public void printParams(PrintWriter pw) {
        pw.println(CONFIG_PARAM_INITIAL_EMBEDDING_SEED + ": " + this.seed);
        pw.println(CONFIG_PARAM_FORCE_ATLAS_NO_ITER + ": " + this.noIters);
        pw.println(CONFIG_PARAM_EMBEDDING_TYPE + ": " + this.embeddingType);
        pw.println(CONFIG_PARAM_FORCE_ATLAS_USE_EDGE_WEIGHTS + ": " + this.useEdgeWeights);
    }
}
