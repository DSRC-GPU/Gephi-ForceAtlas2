/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crowdlanes.stages;

import crowdlanes.CosineGroupSimilarity;
import crowdlanes.GraphUtil;
import crowdlanes.ResultsDir;
import crowdlanes.Simulation;
import java.io.File;
import java.io.PrintWriter;
import org.openide.util.Exceptions;

/**
 *
 * @author vlad
 */
public class CosineSimilarityStage extends PipelineStage {

    private PrintWriter cosineSimWriter;
    private final CosineGroupSimilarity cosineGroupSimilarity;
    String fname;

    public CosineSimilarityStage(String fname, String column) {
        this.fname = fname;
        cosineGroupSimilarity = new CosineGroupSimilarity(column);
    }

    @Override
    public void run(double from, double to, boolean hasChanged) {
        cosineSimWriter.println("from " + from + " to " + to);

        for (Integer g : GraphUtil.getGroups()) {
            cosineGroupSimilarity.printGroupSimilarity(cosineSimWriter, g);
        }
    }

    @Override
    public void setup(Simulation.CurrentConfig cc) {
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
