package crowdlanes.metrics;

import crowdlanes.util.GraphUtil;
import static crowdlanes.util.GraphUtil.getVector;
import java.io.PrintWriter;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.graph.api.Node;

public class CosineGroupSimilarity {

    private final String vectorColumnName;
    DescriptiveStatistics cosineSimStats;

    public CosineGroupSimilarity(String columnName) {
        cosineSimStats = new DescriptiveStatistics();
        this.vectorColumnName = columnName;
    }

    public void printGroupSimilarity(PrintWriter writer, int group) {
        double gs;
        writer.print(group);
        writer.print(" " + getInnerGroupSimilarity(group));
        writer.print(" " + getStdDev());
        writer.print(" " + getInterGroupSimilarity(group));
        writer.print(" " + getStdDev());
        writer.println();
    }

    public double getStdDev() {
        return cosineSimStats.getStandardDeviation();
    }

    private void updateGroupSimilarity(Node n1, Node n2, Vector2D vals1, Vector2D vals2) {
        double cs = CosineSimilarity.similarity(vals1, vals2);
        if (!Double.isNaN(cs)) {
            cosineSimStats.addValue(cs);
        } else {
            warn(false, "cosineSimilarity is Nan: " + n1.getId() + "[ " + vals1 + " ] " + n2.getId() + "[ " + vals2 + " ]");
        }
    }

    public double getInnerGroupSimilarity(int group) {
        cosineSimStats.clear();
        List<Node> sameGroup = GraphUtil.getNodesInGroup(group);

        for (int i = 0; i < sameGroup.size(); i++) {
            Node n1 = sameGroup.get(i);
            Vector2D vals1 = getVector(n1, vectorColumnName);
            for (int j = i + 1; j < sameGroup.size(); j++) {
                Node n2 = sameGroup.get(j);
                Vector2D vals2 = getVector(n2, vectorColumnName);
                updateGroupSimilarity(n1, n2, vals1, vals2);
            }
        }
        return cosineSimStats.getMean();
    }

    public double getInterGroupSimilarity(int group) {
        cosineSimStats.clear();
        List<Node> sameGroup = GraphUtil.getNodesInGroup(group);
        List<Node> fromOtherGroups = GraphUtil.getNodesNotInGroup(group);

        for (Node n1 : sameGroup) {
            Vector2D vals1 = getVector(n1, vectorColumnName);
            for (Node n2 : fromOtherGroups) {
                Vector2D vals2 = getVector(n2, vectorColumnName);
                updateGroupSimilarity(n1, n2, vals1, vals2);
            }
        }
        return cosineSimStats.getMean();
    }

    private void warn(boolean exp, String msg) {
        if (!exp) {
            System.err.println("WARNING: GroupSimilarity: " + msg);
        }
    }
}
