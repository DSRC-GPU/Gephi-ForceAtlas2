package crowdlanes;

import crowdlanes.stages.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.ini4j.Wini;
import org.openide.util.Exceptions;

public class ParameterSweeper {

    private final Simulation sim;
    private final ConfigParam<String> Embedding_type;
    private final ConfigParam<Integer> ForceAtlas_iters;
    private final ConfigParam<Boolean> ForceAtlas_useEdgeWeights;
    private final ConfigParam<Integer> VelocityVector_timeWindow;
    private final ConfigParam<Double> GraphIterator_step;
    private final ConfigParam<Double> GraphIterator_duration;
    private final ConfigParam<Float> Smoothening_phi1;
    private final ConfigParam<Float> Smoothening_phi2;
    private final ConfigParam<Integer> Smoothening_noRounds;
    private final ConfigParam<String> Smoothening_averageMethod;
    private PrintWriter paramWriter;

    public ParameterSweeper(Wini config, Simulation sim) throws IOException {
        this.sim = sim;

        Embedding_type = new ConfigParam(config, EmbeddingStage.SECTION, String.class);
        ForceAtlas_iters = new ConfigParam(config, EmbeddingStage.SECTION, Integer.class);
        ForceAtlas_useEdgeWeights = new ConfigParam(config, EmbeddingStage.SECTION, Boolean.class);
        VelocityVector_timeWindow = new ConfigParam(config, VelocityProcessorStage.SECTION, Integer.class);
        GraphIterator_step = new ConfigParam(config, DynamicGraphIterator.SECTION, Double.class);
        GraphIterator_duration = new ConfigParam(config, DynamicGraphIterator.SECTION, double.class);
        Smoothening_phi1 = new ConfigParam(config, SmootheningStage.SECTION, Float.class);
        Smoothening_phi2 = new ConfigParam(config, SmootheningStage.SECTION, Float.class);
        Smoothening_noRounds = new ConfigParam(config, SmootheningStage.SECTION, Integer.class);
        Smoothening_averageMethod = new ConfigParam(config, SmootheningStage.SECTION, String.class);

        Embedding_type.read("type");
        ForceAtlas_iters.read("iters");
        ForceAtlas_useEdgeWeights.read("edgeWeights");
        VelocityVector_timeWindow.read("timeWindowSize");
        GraphIterator_step.read("step");
        GraphIterator_duration.read("duration");
        Smoothening_phi1.read("phi1");
        Smoothening_phi2.read("phi2");
        Smoothening_noRounds.read("rounds");
        Smoothening_averageMethod.read("average");
    }

    private void writeParamFile(String Embedding_type, int ForceAtlas_iters, boolean ForceAtlas_useEdgeWeights,
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

                                                System.err.println(type + " " + iter + " " + useEmbeddingEdgeWeights
                                                        + " " + timeWindowSize + " " + step + " " + duration + " "
                                                        + phi1 + " " + phi2 + " " + rounds + " " + avgMethod);

                                                writeParamFile(type, iter, useEmbeddingEdgeWeights,
                                                        timeWindowSize, rounds, phi1, phi2, avgMethod, step, duration);

                                                sim.run(type, iter, useEmbeddingEdgeWeights,
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
