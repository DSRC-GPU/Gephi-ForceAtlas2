package crowdlanes.config;

import crowdlanes.graphReader.GraphReader;
import java.io.File;
import java.io.IOException;
import org.openide.util.Lookup;

public class ResultsDir {

    public static int dirCounter;
   
    public static File getCurrentResultPath() throws IOException {
        String fileName = Lookup.getDefault().lookup(GraphReader.class).getFile().getName();
        File currentPath = new File(new File(".").getAbsolutePath());
        File resultsPath = new File(currentPath, fileName + "_" + dirCounter);
        if (!resultsPath.exists()) {
            resultsPath.mkdirs();
        }
        
        return resultsPath;
    }
    
    public static void updateResultPath() {
        dirCounter++;
    }
}
