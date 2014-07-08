package crowdlanes.stages;

import crowdlanes.GraphUtil;
import crowdlanes.config.ResultsDir;
import crowdlanes.metrics.SpeedGroupSimilarity;
import crowdlanes.config.CurrentConfig;
import java.io.File;
import java.io.PrintWriter;
import java.util.Set;
import org.openide.util.Exceptions;

public class SpeedSimilarityStage extends PipelineStage {

    private PrintWriter speedSimWriter;
    private final SpeedGroupSimilarity speedGroupSimilarity;
    String fname;

    public SpeedSimilarityStage(String fname, String column) {
        this.fname = fname;
        speedGroupSimilarity = new SpeedGroupSimilarity(column);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        Set<Integer> groups = GraphUtil.getGroups();
        speedSimWriter.println("from " + from + " to " + to);
        speedSimWriter.println("entries: " + groups.size());
        for (Integer g : groups) {
            speedGroupSimilarity.printGroupSimilarity(speedSimWriter, g);
        }
    }

    @Override
    public void setup(CurrentConfig cc) {
        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            speedSimWriter = new PrintWriter(new File(resultsDir, fname), "UTF-8");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void tearDown() {
        speedSimWriter.close();
    }
}
