package crowdlanes.stages;

import crowdlanes.config.CurrentConfig;
import java.io.PrintWriter;

public abstract class PipelineStage {

    public static boolean INFO = true;
    
    public abstract void run(double from, double to, boolean hasChanged);

    public abstract void setup(CurrentConfig cc);

    public abstract void tearDown();
    
    public void printParams(PrintWriter pw) {}

    protected void info(String msg) {
        if (INFO) {
            System.err.print(msg);
        }
    }
}
