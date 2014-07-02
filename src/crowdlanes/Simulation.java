package crowdlanes;

import crowdlanes.stages.*;
import java.util.Iterator;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;

public class Simulation {

    public void run(String Embedding_type, int ForceAtlas_iters, boolean ForceAtlas_useEdgeWeights,
            int VelocityVector_timeWindow, int Smoothening_noRounds, float Smoothening_phi1, float Smoothening_phi2,
            String Smoothening_averageMethod, double GraphIterator_step, double GraphIterator_duration) throws IllegalAccessException {

        EmbeddingStage embedingStage = new EmbeddingStage(Embedding_type, ForceAtlas_iters, ForceAtlas_useEdgeWeights);
        VelocityProcessorStage velocityProcessor = new VelocityProcessorStage(VelocityVector_timeWindow);
        SmootheningStage smootheningStage1 = new SmootheningStage(Smoothening_noRounds, Smoothening_phi1, Smoothening_averageMethod, SmootheningStage.FINE_SMOOTHENING);
        SmootheningStage smootheningStage2 = new SmootheningStage(Smoothening_noRounds, Smoothening_phi2, Smoothening_averageMethod, SmootheningStage.COARSE_SMOOTHENING);
        PCAStage pcaStage = new PCAStage();
        DoPStage dopStage = new DoPStage(Smoothening_phi1, Smoothening_phi2);
        PCADoPStage pcaDopStage = new PCADoPStage(Smoothening_phi1, Smoothening_phi2);
        GraphPrinter graphPrinter = new GraphPrinter();

        embedingStage.setup();
        velocityProcessor.setup();
        smootheningStage1.setup();
        smootheningStage2.setup();
        dopStage.setup();
        //pcaStage.setup();
        //pcaDopStage.setup();
        graphPrinter.setup();

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
                continue;
            } else {
                count++;
            }

            embedingStage.run(from, to, hasChanged);
            System.err.println("CC size: "  + GraphUtil.getCC().getConnectedComponentsCount());
            velocityProcessor.run(from, to, hasChanged);
            if (count > 1) {
                smootheningStage1.run(from, to, hasChanged);
                smootheningStage2.run(from, to, hasChanged);

                //pcaStage.run(from, to, hasChanged);
                //pcaDopStage.run(from, to, hasChanged);
                graphPrinter.run(from, to, hasChanged);
                dopStage.run(from, to, hasChanged);
            }

            System.err.println("");
        }

        System.err.println("hasChanged: " + count);
        System.err.println("total incorrect cuts: " + pcaDopStage.getIncorrectCuts());
        System.err.println("total correct cuts: " + pcaDopStage.getCorrectCuts());
        System.err.println("total missed cuts: " + pcaDopStage.getMissedCuts());

        graphPrinter.tearDown();
        pcaDopStage.tearDown();
        pcaStage.tearDown();
        //dopStage.tearDown();
        smootheningStage1.tearDown();
        smootheningStage2.tearDown();
        velocityProcessor.tearDown();
        embedingStage.tearDown();
    }
}
