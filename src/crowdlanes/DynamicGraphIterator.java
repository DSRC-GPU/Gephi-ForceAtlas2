package crowdlanes;

import java.util.Iterator;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.filters.api.Range;
import org.gephi.graph.api.Graph;
import org.openide.util.Lookup;

public class DynamicGraphIterator implements Iterable<Graph> {

    public final static String SECTION = "GraphIterator";
    private final double step;
    private final double duration;
    private final double min;
    private final double max;
    private double to;
    private double from;
    private boolean someAction;

    public DynamicGraphIterator(double step, double duration) {
        this.step = step;
        this.duration = duration;
        DynamicController dc = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel model = dc.getModel();
        this.max = model.getMax();
        this.min = model.getMin();
        from = 0;
        duration = 0;
    }

    public Range getCurrentRange() {
        return new Range(from, to);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }
    
    public boolean hasChanged() {
        return GraphReader.getInstance().hasChanged();
    }

    public Iterator<Graph> iterator() {
        from = 0;
        to = duration;
        someAction = true;
        return new Iterator<Graph>() {

            public boolean hasNext() {
                return someAction;
            }

            public Graph next() {
                GraphReader gr = GraphReader.getInstance();
                Graph g = gr.getGraph(from, to);
                if (to < max) {
                    from = Math.max(from + step, min);
                    to = Math.min(to + step, max);
                    someAction = true;
                } else {
                    someAction = false;
                }

                return g;
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}
