package crowdlanes.embeddings;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 */
public class RandomLayout extends AbstractLayout implements Layout {

    private Random random;
    private Graph graph;
    private boolean converged;
    private double size;

    public RandomLayout(LayoutBuilder layoutBuilder,  Long seed, double size) {
        super(layoutBuilder);
        this.size = size;
        random = new Random();
        if (seed != null)
            random = new Random(seed);
        else
            random = new Random();
    }

    public void initAlgo() {
        converged = false;
        graph = graphModel.getGraphVisible();
    }

    public void goAlgo() {
        graph = graphModel.getGraphVisible();
        for (Node n : graph.getNodes()) {
            n.getNodeData().setX((float) (-size / 2 + size * random.nextDouble()));
            n.getNodeData().setY((float) (-size / 2 + size * random.nextDouble()));
        }
        converged = true;
    }

    @Override
    public boolean canAlgo() {
        return !converged;
    }

    public void endAlgo() {
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class, 
                    NbBundle.getMessage(getClass(), "Random.spaceSize.name"),
                    null,
                    "Random.spaceSize.name",
                    NbBundle.getMessage(getClass(), "Random.spaceSize.desc"),
                    "getSize", "setSize"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    public void resetPropertiesValues() {
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public Double getSize() {
        return size;
    }
}
