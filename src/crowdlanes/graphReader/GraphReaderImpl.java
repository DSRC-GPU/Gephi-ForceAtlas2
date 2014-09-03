package crowdlanes.graphReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.DynamicModelImpl;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.filters.FilterProcessor;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder;
import org.gephi.filters.plugin.dynamic.DynamicRangeBuilder.DynamicRangeFilter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.project.api.ProjectController;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = GraphReader.class)
public final class GraphReaderImpl implements GraphReader {

    private DynamicModelImpl model;
    private DynamicRangeFilter dynamicRangeFilter;
    private FilterProcessor processor;
    private FilterController filterController;
    private GraphModel graphModel;
    private Query dynamicQuery;
    private FileObject fileObject;

    @Override
    public void importFile(String filePath) throws FileNotFoundException, IOException {
        File file = new File(filePath);
        if (!file.isFile() || !file.exists()) {
            throw new FileNotFoundException("Problem loading file: " + filePath);
        }

        fileObject = FileUtil.toFileObject(file);
        importGraphGexf();

    }

    @Override
    public FileObject getFile() {
        return fileObject;
    }

    @Override
    public Graph getGraph(double from, double to) {

        try {
            Field f = model.getClass().getDeclaredField("visibleTimeInterval");
            f.setAccessible(true);
            f.set(model, new TimeInterval(from, to));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        dynamicQuery = filterController.createQuery(dynamicRangeFilter);
        GraphView gv = filterController.filter(dynamicQuery);
        graphModel.setVisibleView(gv);

        return graphModel.getGraphVisible();
    }

    private void setup() {
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        processor = new FilterProcessor();
        filterController = Lookup.getDefault().lookup(FilterController.class);
        FilterBuilder[] builders = Lookup.getDefault().lookup(DynamicRangeBuilder.class).getBuilders();
        dynamicRangeFilter = (DynamicRangeFilter) builders[0].getFilter();     //There is only one TIME_INTERVAL column, so it's always the [0] builder
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        model = (DynamicModelImpl) dynamicController.getModel();
    }

    private void importGraphGexf() throws FileNotFoundException {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.closeCurrentWorkspace();
        pc.newProject();

        //Import first file
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        container = importController.importFile(FileUtil.toFile(fileObject));
        importController.process(container);
        setup();
    }
}
