package crowdlanes.metrics;

import crowdlanes.GraphUtil;
import static crowdlanes.GraphUtil.getVector;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Node;

public class CosineGroupSimilarity {

    private final String vectorColumnName;
    private final ArrayList<Double> cosineSimResults;

    public CosineGroupSimilarity(String columnName) {
        cosineSimResults = new ArrayList<>();
        this.vectorColumnName = columnName;
    }


    public void printGroupSimilarity(PrintWriter writer, int group) {
        double gs;
        writer.print(group);
        gs = getInnerGroupSimilarity(group);
        writer.print(" " + gs);
        writer.print(" " + getStdDev(gs));
        gs = getInterGroupSimilarity(group);
        writer.print(" " + gs);
        writer.print(" " + getStdDev(gs));
        writer.println();
    }

    public double getStdDev(double groupSimilarity) {
        double sum = 0;
        for (Double d : cosineSimResults) {
            sum += Math.pow(d - groupSimilarity, 2);
        }
        return Math.sqrt(sum / cosineSimResults.size());
    }

    private void updateGroupSimilarity(Node n1, Node n2, FloatList vals1, FloatList vals2) {
        double cs = CosineSimilarity.similarity(vals1, vals2);
        if (!Double.isNaN(cs)) {
            cosineSimResults.add(cs);
        } else {
            warn(false, "cosineSimilarity is Nan: " + n1.getId() + "[ " + vals1 + " ] " + n2.getId() + "[ " + vals2 + " ]");
        }
    }

    private double getGroupSimilarity() {
        double groupSimilarity = 0;
        for (Double d : cosineSimResults) {
            groupSimilarity += d;
        }
        return groupSimilarity / cosineSimResults.size();
    }

    public double getInnerGroupSimilarity(int group) {
        cosineSimResults.clear();
        List<Node> sameGroup = GraphUtil.getNodesInGroup(group);

        for (int i = 0; i < sameGroup.size(); i++) {
            Node n1 = sameGroup.get(i);
            FloatList vals1 = getVector(n1, vectorColumnName);
            for (int j = i + 1; j < sameGroup.size(); j++) {
                Node n2 = sameGroup.get(j);
                FloatList vals2 = getVector(n2, vectorColumnName);
                updateGroupSimilarity(n1, n2, vals1, vals2);
            }
        }
        return getGroupSimilarity();
    }

    public double getInterGroupSimilarity(int group) {
        cosineSimResults.clear();
        List<Node> sameGroup = GraphUtil.getNodesInGroup(group);
        List<Node> fromOtherGroups = GraphUtil.getNodesNotInGroup(group);

        for (Node n1 : sameGroup) {
            FloatList vals1 = getVector(n1, vectorColumnName);
            for (Node n2 : fromOtherGroups) {
                FloatList vals2 = getVector(n2, vectorColumnName);
                updateGroupSimilarity(n1, n2, vals1, vals2);
            }
        }
        return getGroupSimilarity();
    }

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: GroupSimilarity: " + msg);
        }
    }
}
