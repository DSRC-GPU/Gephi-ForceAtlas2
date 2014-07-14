package crowdlanes.stages;

import crowdlanes.metrics.CosineGroupSimilarity;
import crowdlanes.util.GraphUtil;
import crowdlanes.config.ResultsDir;
import crowdlanes.config.CurrentConfig;
import java.io.File;
import java.io.PrintWriter;
import java.util.Set;
import org.openide.util.Exceptions;

/**
 *
 * @author vlad
 */
public class CosineSimilarityStage extends PipelineStage {

    private PrintWriter cosineSimWriter;
    private final CosineGroupSimilarity cosineGroupSimilarity;
    private final String fname;

    public CosineSimilarityStage(String fname, String column) {
        this.fname = fname;
        cosineGroupSimilarity = new CosineGroupSimilarity(column);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        Set<Integer> groups = GraphUtil.getGroups();
        cosineSimWriter.println("from " + from + " to " + to);
        cosineSimWriter.println("entries: " + groups.size());

        for (Integer g : groups) {
            cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, g);
        }
    }

    @Override
    public void setup(CurrentConfig cc) {
        try {
            File resultsDir = ResultsDir.getCurrentResultPath();
            cosineSimWriter = new PrintWriter(new File(resultsDir, fname), "UTF-8");
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void tearDown() {
        cosineSimWriter.close();
    }
}
