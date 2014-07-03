package crowdlanes;

import crowdlanes.stages.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;

public class Simulation {

    public void run(Integer Embedding_seed, String Embedding_type, int ForceAtlas_iters, boolean ForceAtlas_useEdgeWeights,
            int VelocityVector_timeWindow, int Smoothening_noRounds, float Smoothening_phi1, float Smoothening_phi2,
            String Smoothening_averageMethod, double GraphIterator_step, double GraphIterator_duration) throws IllegalAccessException {

        List<PipelineStage> pipeline = new ArrayList<>();
        pipeline.add(new EmbeddingStage(Embedding_seed, Embedding_type, ForceAtlas_iters, ForceAtlas_useEdgeWeights));
        pipeline.add(new VelocityProcessorStage(VelocityVector_timeWindow));
        pipeline.add(new SmootheningStage(Smoothening_noRounds, Smoothening_phi1, Smoothening_averageMethod, SmootheningStage.FINE_SMOOTHENING));
        pipeline.add(new SmootheningStage(Smoothening_noRounds, Smoothening_phi2, Smoothening_averageMethod, SmootheningStage.COARSE_SMOOTHENING));
        pipeline.add(new DoPStageCoords(Smoothening_phi1, Smoothening_phi2));
        pipeline.add(new PCADoPStage(Smoothening_phi1, Smoothening_phi2));
        pipeline.add(new GraphPrinterStage());

        for (PipelineStage ps : pipeline) {
            ps.setup();
        }

        DynamicGraphIterator dynamicGraphIterator = new DynamicGraphIterator(GraphIterator_step, GraphIterator_duration);

        Iterator<Graph> it = dynamicGraphIterator.iterator();
        System.err.println("min: " + dynamicGraphIterator.getMin() + " max: " + dynamicGraphIterator.getMax());

        int count = 0;
        while (it.hasNext()) {
            Range r = dynamicGraphIterator.getCurrentRange();
            double from = r.getLowerDouble();
            double to = r.getUpperDouble();
            Graph g = it.next();
            boolean hasChanged = dynamicGraphIterator.hasChanged();
            System.err.println(r);
            System.err.println("nodeCount: " + g.getNodeCount());
            System.err.println("edgeCount: " + g.getEdgeCount());
            System.err.println("hasChanged: " + hasChanged);
            if (!hasChanged) {
                System.err.println("");
                continue;
            } else {
                count++;
            }

            for (PipelineStage ps : pipeline) {
                ps.run(from, to, hasChanged);
            }
            
            System.err.println("");
        }

        System.err.println("hasChanged: " + count);

        for (int i = pipeline.size(); i > 0; i--) {
            pipeline.get(i).tearDown();
        }

    }
}
