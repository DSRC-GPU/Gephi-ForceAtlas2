package org.gephi.toolkit.demos;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.openide.util.Lookup;

public class EmbeddingStage extends PipelineStage {

    public final static String SECTION = "Embedding";

    private GraphModel graphModel;
    private static final String FORCE_ATLAS2_LAYOUT = "ForceAtlas2";
    private static final String YIFANHU_LAYOUT = "YifanHu";
    private final ForceAtlas2 forceAltasLayout;
    private final YifanHuLayout yifanHuLayout;
    private final int noIters;
    private final String embeddingType;

    private final boolean useEdgeWeights;

    public EmbeddingStage(String embeddingType, int noIters, boolean useEdgeWeights) {
        if (!embeddingType.equals(FORCE_ATLAS2_LAYOUT) && !embeddingType.equals(YIFANHU_LAYOUT))
            throw new IllegalArgumentException("unexpected embedding type");
        
        this.noIters = noIters;
        this.embeddingType = embeddingType;
        this.useEdgeWeights = useEdgeWeights;
        forceAltasLayout = new ForceAtlas2(null);
        yifanHuLayout = new YifanHuLayout(null, new StepDisplacement(1f));
    }

    public void run(double from, double to, boolean hasChanged) {
        info("ForceAtlas Stage: ");

        if (useEdgeWeights) {
            EdgeWeight.getInstance().setEdgeWeights(from, to);
        }

        if (embeddingType.equals(YIFANHU_LAYOUT)) {
            yifanHuLayout.initAlgo();
            while (!yifanHuLayout.isConverged()) {
                yifanHuLayout.goAlgo();
                info("*");
            }
            yifanHuLayout.endAlgo();

        } else if (embeddingType.equals(FORCE_ATLAS2_LAYOUT)) {
            for (int i = 0; i < noIters && forceAltasLayout.canAlgo(); i++) {
                info("*");
                forceAltasLayout.goAlgo();
            }
        }

        info("\n");
    }

    public void setup() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        if (embeddingType.equals(YIFANHU_LAYOUT)) {
            yifanHuLayout.setGraphModel(graphModel);
            yifanHuLayout.resetPropertiesValues();
        } else if (embeddingType.equals(FORCE_ATLAS2_LAYOUT)) {
            forceAltasLayout.setGraphModel(graphModel);
            forceAltasLayout.resetPropertiesValues();
            forceAltasLayout.setAdjustSizes(true);
            if (!useEdgeWeights) {
                forceAltasLayout.setEdgeWeightInfluence(0.0);
            } else {
                forceAltasLayout.setEdgeWeightInfluence(1.0);
            }
            forceAltasLayout.initAlgo();
            graphModel.getGraphVisible().readUnlockAll();
        }
    }

    public void tearDown() {
        if (embeddingType.equals(FORCE_ATLAS2_LAYOUT)) {
            forceAltasLayout.endAlgo();
        }
    }
}
