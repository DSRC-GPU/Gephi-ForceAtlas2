package crowdlanes;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public final class GraphReader {

    private DynamicModelImpl model;
    private DynamicRangeFilter dynamicRangeFilter;
    private FilterProcessor processor;
    private FilterController filterController;
    private GraphModel graphModel;
    private Query dynamicQuery;
    private boolean init;
    private File file;

    private static final GraphReader INSTANCE = new GraphReader();

    private GraphReader() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public void setup(String gexfFile) throws FileNotFoundException {
        importGraph(gexfFile);
        setupDynamicProcessor();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        init = true;
    }

    public File getFile() {
        return file;
    }

    public Graph getGraph(double from, double to) {
        if (!init) {
            throw new IllegalStateException("GraphReader has not been initialized");
        }

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

    private void setupDynamicProcessor() {
        processor = new FilterProcessor();
        filterController = Lookup.getDefault().lookup(FilterController.class);
        FilterBuilder[] builders = Lookup.getDefault().lookup(DynamicRangeBuilder.class).getBuilders();
        dynamicRangeFilter = (DynamicRangeFilter) builders[0].getFilter();     //There is only one TIME_INTERVAL column, so it's always the [0] builder
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        model = (DynamicModelImpl) dynamicController.getModel();
    }

    public static GraphReader getInstance() {
        return INSTANCE;
    }

    private void importGraph(String file_path) throws FileNotFoundException {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Import first file
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        file = new File(file_path);
        if (file.isFile() == false) {
            throw new FileNotFoundException("Cannot find file: " + file);
        }

        container = importController.importFile(file);
        importController.process(container);
    }
}
