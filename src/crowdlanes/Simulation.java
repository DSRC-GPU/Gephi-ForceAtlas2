package crowdlanes;

import crowdlanes.config.ConfigParam;
import crowdlanes.config.CurrentConfig;
import crowdlanes.config.ResultsDir;
import crowdlanes.graphReader.DynamicGraphIterator;
import crowdlanes.graphReader.GraphReader;
import crowdlanes.stages.*;
import crowdlanes.stages.GraphPrinterStage;
import crowdlanes.stages.dop.Dop;
import crowdlanes.stages.embedding.MovementStage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class Simulation {

    private final String paramFileName = "params.txt";
    private List<PipelineStage> pipeline;
    private final String gexfFile;

    public Simulation(String gexfFile) throws IllegalAccessException, IOException {
        this.gexfFile = gexfFile;
        pipeline = new ArrayList<>();
    }

    public void setup(CurrentConfig cc) {

        for (PipelineStage ps : pipeline) {
            ps.setup(cc);
        }

        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            PrintWriter pw = new PrintWriter(new File(resultsDir, paramFileName), "UTF-8");
            for (PipelineStage ps : pipeline) {
                ps.printParams(pw);
            }
            pw.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void run(List<ConfigParam.Value> paramVals) {

        try {
            Lookup.getDefault().lookup(GraphReader.class).importFile(gexfFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        pipeline.clear();
        pipeline.add(new MovementStage());
        pipeline.add(new VelocityProcessorStage());
        pipeline.add(new Dop());
        pipeline.add(new EdgeCutingAndCCDetectionStage());
        pipeline.add(new GraphPrinterStage());

        CurrentConfig cc = new CurrentConfig(paramVals);
        setup(cc);
        int count = 0;

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
