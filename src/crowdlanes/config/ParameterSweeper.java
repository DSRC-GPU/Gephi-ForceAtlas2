package crowdlanes.config;

import static com.google.common.collect.Sets.cartesianProduct;
import crowdlanes.Simulation;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ParameterSweeper {

    private List<ConfigParam> params;
    private final Simulation sim;

    private PrintWriter paramWriter;

    public ParameterSweeper() throws IOException, IllegalAccessException {
        params = new ArrayList<>();
        this.sim = new Simulation();
    }

    public void run() {
        for (List<ConfigParam.Value> l : cartesianProduct(params)) {
            sim.run(l);
        }
    }

    public void registerParam(ConfigParam pm) {
        params.add(pm);
        pm.read();
    }

    /*

     private void writeParamFile(Integer Initial_embeding_seed, String Embedding_type, int ForceAtlas_iters, boolean ForceAtlas_useEdgeWeights,
     int VelocityVector_timeWindow, int Smoothening_noRounds, float Smoothening_phi1, float Smoothening_phi2,
     String Smoothening_averageMethod, double GraphIterator_step, double GraphIterator_duration) {
     try {
     File resultsDir = ResultsDir.getNewResultPath();
     paramWriter = new PrintWriter(new File(resultsDir, "params"), "UTF-8");
     } catch (FileNotFoundException ex) {
     Exceptions.printStackTrace(ex);
     } catch (UnsupportedEncodingException ex) {
     Exceptions.printStackTrace(ex);
     } catch (IOException ex) {
     Exceptions.printStackTrace(ex);
     }

     paramWriter.println("Initial_embeding_seed: " + Initial_embeding_seed);
     paramWriter.println("Embedding_type: " + Embedding_type);
     paramWriter.println("ForceAtlas_iters: " + ForceAtlas_iters);
     paramWriter.println("ForceAtlas_useEdgeWeights: " + ForceAtlas_useEdgeWeights);
     paramWriter.println("VelocityVector_timeWindow: " + VelocityVector_timeWindow);
     paramWriter.println("Smoothening_noRounds: " + Smoothening_noRounds);
     paramWriter.println("Smoothening_phi1: " + Smoothening_phi1);
     paramWriter.println("Smoothening_phi2: " + Smoothening_phi2);
     paramWriter.println("Smoothening_averageMethod: " + Smoothening_averageMethod);
     paramWriter.println("GraphIterator_step: " + GraphIterator_step);
     paramWriter.println("GraphIterator_duration: " + GraphIterator_duration);
     paramWriter.close();

     }

   
     public void run() throws IOException, IllegalAccessException {
     for (Integer seed : Initial_embeding_seed) {
     for (String type : Embedding_type) {
     for (int iter : ForceAtlas_iters) {
     for (Boolean useEmbeddingEdgeWeights : ForceAtlas_useEdgeWeights) {
     for (Integer timeWindowSize : VelocityVector_timeWindow) {
     for (double step : GraphIterator_step) {
     for (double duration : GraphIterator_duration) {
     for (float phi1 : Smoothening_phi1) {
     for (float phi2 : Smoothening_phi2) {
     for (int rounds : Smoothening_noRounds) {
     for (String avgMethod : Smoothening_averageMethod) {

     System.err.println(seed + " " + type + " " + iter + " " + useEmbeddingEdgeWeights
     + " " + timeWindowSize + " " + step + " " + duration + " "
     + phi1 + " " + phi2 + " " + rounds + " " + avgMethod);

     writeParamFile(seed, type, iter, useEmbeddingEdgeWeights,
     timeWindowSize, rounds, phi1, phi2, avgMethod, step, duration);

     sim.run(seed, type, iter, useEmbeddingEdgeWeights,
     timeWindowSize, rounds, phi1, phi2, avgMethod, step, duration);

     }
     }
     }
     }
     }
     }
     }
     }
     }
     }
     }
     }
     */
}
