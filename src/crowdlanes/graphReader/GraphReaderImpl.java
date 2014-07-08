package crowdlanes.graphReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.ImportUtils;
import org.gephi.io.importer.api.NodeDraft;
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

    public GraphReaderImpl() {

    }

    @Override
    public void importFile(String filePath) throws FileNotFoundException, IOException {
        fileObject = FileUtil.toFileObject(new File(filePath));
        File file = FileUtil.toFile(fileObject);

        if (!file.isFile() || !file.exists()) {
            throw new FileNotFoundException("Problem loading file: " + filePath);
        }

        if (fileObject.hasExt("gexf")) {
            importGraphGexf();
        } else {
            importGraphTxt();
        }
    }

    public FileObject getFile() {
        return fileObject;
    }

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
        pc.newProject();

        //Import first file
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Container container;

        container = importController.importFile(FileUtil.toFile(fileObject));
        importController.process(container);
        setup();
    }

    private enum ReadState {

        READ_TICK, READ_NODES, READ_EDGES
    };

    private void importGraphTxt() throws FileNotFoundException, IOException {

        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        ContainerLoader loader = container.getLoader();
        ReadState state = ReadState.READ_TICK;

        LineNumberReader lineReader = ImportUtils.getTextReader(fileObject);
        List<String> lines = new ArrayList<>();
        for (; lineReader.ready();) {
            String line = lineReader.readLine();
            if (line != null && !line.isEmpty()) {
                lines.add(line.trim());
            }
        }

        int i = 0;
        int tick;
        while (true) {
            switch (state) {

                case READ_TICK:
                    tick = Integer.parseInt(lines.get(i++));
                    state = ReadState.READ_NODES;
                    break;
                case READ_NODES:
                    String[] parts = lines.get(i++).split(":");
                    if (!parts[0].equals("NumNodes") || parts.length != 2) {
                        throw new IllegalArgumentException("Mallformed input file");
                    }

                    int numNodes = Integer.parseInt(parts[1]);
                    for (int j = 0; j < numNodes; j++) {
                        String[] nodeParts = lines.get(i + j).split("\\s+");
                        if (nodeParts.length != 4) {
                            throw new IllegalArgumentException("Mallformed input file");
                        }

                        int id = Integer.parseInt(nodeParts[0]);
                        int group = Integer.parseInt(nodeParts[1]);
                        float x = Float.parseFloat(nodeParts[2]);
                        float y = Float.parseFloat(nodeParts[3]);

                    }
            }
        }
    }

    private void addNode(ContainerLoader loader, String id) {
        if (!loader.nodeExists(id)) {
            NodeDraft node = loader.factory().newNodeDraft();
            node.setId(id);
            node.setLabel(id);
            loader.addNode(node);
        }

    }
}
