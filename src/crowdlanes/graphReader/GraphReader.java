package crowdlanes.graphReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.gephi.graph.api.Graph;
import org.openide.filesystems.FileObject;

public interface GraphReader {

    public void importFile(String filePath) throws FileNotFoundException, IOException;

    public FileObject getFile();
    
    public Graph getGraph(double from, double to);
    
}
