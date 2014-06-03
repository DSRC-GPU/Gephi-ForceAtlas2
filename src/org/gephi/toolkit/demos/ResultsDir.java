package org.gephi.toolkit.demos;

import java.io.File;
import java.io.IOException;

public class ResultsDir {

    public static int dirCounter;
   
    public static File getCurrentResultPath() throws IOException {
        String fileName = GraphReader.getInstance().getFile().getName().replaceFirst("[.][^.]+$", "");
        File currentPath = new File(new File(".").getAbsolutePath());
        File resultsPath = new File(currentPath, fileName + "_" + dirCounter);
        if (!resultsPath.exists()) {
            resultsPath.mkdirs();
        }
        
        return resultsPath;
    }
    
    public static File getNewResultPath() throws IOException {
        dirCounter++;
        return getCurrentResultPath();
    }
}
