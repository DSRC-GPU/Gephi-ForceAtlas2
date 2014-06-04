package org.gephi.toolkit.demos;

import java.util.Iterator;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;

public class Simulation {

    public void run(String Embedding_type, int ForceAtlas_iters, boolean ForceAtlas_useEdgeWeights,
            int VelocityVector_timeWindow, int Smoothening_noRounds, float Smoothening_phi,
            String Smoothening_averageMethod, double GraphIterator_step, double GraphIterator_duration) 
    {
        EmbeddingStage embedingStage = new EmbeddingStage(Embedding_type, ForceAtlas_iters, ForceAtlas_useEdgeWeights);
        VelocityProcessorStage velocityProcessor = new VelocityProcessorStage(VelocityVector_timeWindow);
        SmootheningStage smootheningStage = new SmootheningStage(Smoothening_noRounds, Smoothening_phi, Smoothening_averageMethod);
        GraphPrinter graphPrinter = new GraphPrinter();

        embedingStage.setup();
        velocityProcessor.setup();
        smootheningStage.setup();
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
                //continue;
            } else {
                count++;
            }
            embedingStage.run(from, to, hasChanged);
            velocityProcessor.run(from, to, hasChanged);
            smootheningStage.run(from, to, hasChanged);
            graphPrinter.run(from, to, hasChanged);
            System.err.println("");
        }

        System.err.println("hasChanged: " + count);

        graphPrinter.tearDown();
        smootheningStage.tearDown();
        velocityProcessor.tearDown();
        embedingStage.tearDown();
    }
}
