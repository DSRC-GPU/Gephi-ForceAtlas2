package crowdlanes;

import crowdlanes.config.ConfigParam;
import crowdlanes.config.CurrentConfig;
import static crowdlanes.config.ParamNames.*;
import crowdlanes.stages.*;
import crowdlanes.stages.GraphPrinterStage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;

public class Simulation {

    private final List<PipelineStage> pipeline;

    public Simulation() throws IllegalAccessException {
        pipeline = new ArrayList<>();
        pipeline.add(new EmbeddingStage());
        pipeline.add(new VelocityProcessorStage());
        pipeline.add(new SmootheningStage(SmootheningStage.FINE_SMOOTHENING, CONFIG_PARAM_SMOOTHENING_PHI_FINE));
        pipeline.add(new SmootheningStage(SmootheningStage.COARSE_SMOOTHENING, CONFIG_PARAM_SMOOTHENING_PHI_COARSE));
        //pipeline.add(new DoPStageCoords());
        pipeline.add(new PCADoPStage());
        pipeline.add(new EdgeCutingAndCCDetectionStage());
        pipeline.add(new GraphPrinterStage());
    }

    public void run(List<ConfigParam.Value> paramVals) {
        int count = 0;
        CurrentConfig cc = new CurrentConfig(paramVals);
        for (PipelineStage ps : pipeline) {
            ps.setup(cc);
        }

        DynamicGraphIterator dynamicGraphIterator = new DynamicGraphIterator(cc);
        System.err.println("min: " + dynamicGraphIterator.getMin() + " max: " + dynamicGraphIterator.getMax());

        for (Graph g : dynamicGraphIterator) {
            Range r = dynamicGraphIterator.getCurrentRange();
            boolean hasChanged = dynamicGraphIterator.hasChanged();
            System.err.println(r);
            System.err.println("nodeCount: " + g.getNodeCount());
            System.err.println("edgeCount: " + g.getEdgeCount());
            System.err.println("hasChanged: " + hasChanged);
            if (!hasChanged) {
                System.err.println("");
                continue;
            }
            
            
            for (PipelineStage ps : pipeline) {
                ps.run(r.getLowerDouble(), r.getUpperDouble(), hasChanged);
            }
            
            System.err.println("");
            count++;
        }

        System.err.println("hasChanged: " + count);

        for (PipelineStage ps : pipeline) {
            ps.tearDown();
        }

    }
}
