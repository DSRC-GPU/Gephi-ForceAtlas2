package crowdlanes.stages.embedding;

import crowdlanes.config.ConfigParamNames;
import crowdlanes.config.CurrentConfig;
import crowdlanes.stages.PipelineStage;
import java.io.PrintWriter;

public class MovementStage extends PipelineStage {

    private boolean useGroundTruth;
    PipelineStage movementStage;

    @Override
    public void run(double from, double to, boolean hasChanged) {
        movementStage.run(from, to, hasChanged);
    }

    @Override
    public void setup(CurrentConfig cc) {
        useGroundTruth = cc.getBooleanValue(ConfigParamNames.CONFIG_PARAM_USE_GROUNDTRUTH);
        if (useGroundTruth) {
            movementStage = new SetNodesPosStage();
        } else {
            movementStage = new EmbeddingStage();
        }

        movementStage.setup(cc);
    }

    @Override
    public void tearDown() {
        if (movementStage != null) {
            movementStage.tearDown();
        }
    }

    @Override
    public void printParams(PrintWriter pw) {
        pw.println(ConfigParamNames.CONFIG_PARAM_USE_GROUNDTRUTH + ": " + useGroundTruth);
        movementStage.printParams(pw);
    }

}
