package crowdlanes.metrics;

import crowdlanes.util.GraphUtil;
import static crowdlanes.util.GraphUtil.getVector;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.graph.api.Node;

public class SpeedGroupSimilarity {

    private final String vectorColumnName;
    private final DescriptiveStatistics speedSimStats;

    public SpeedGroupSimilarity(String columnName) {
        speedSimStats = new DescriptiveStatistics();
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
        return speedSimStats.getStandardDeviation();
    }

    private void updateGroupSimilarity(Vector2D l1, Vector2D l2) {
        double speedDif = Math.abs(l1.getNorm() - l2.getNorm());
        speedSimStats.addValue(speedDif);
    }


    public double getInnerGroupSimilarity(int group) {
        speedSimStats.clear();
        List<Node> sameGroup = GraphUtil.getNodesInGroup(group);

        for (int i = 0; i < sameGroup.size(); i++) {
            Node n1 = sameGroup.get(i);
            Vector2D vals1 = getVector(n1, vectorColumnName);
            for (int j = i + 1; j < sameGroup.size(); j++) {
                Node n2 = sameGroup.get(j);
                Vector2D vals2 = getVector(n2, vectorColumnName);
                updateGroupSimilarity(vals1, vals2);
            }
        }
        return speedSimStats.getMean();
    }

    public double getInterGroupSimilarity(int group) {
        speedSimStats.clear();
        List<Node> sameGroup = GraphUtil.getNodesInGroup(group);
        List<Node> fromOtherGroups = GraphUtil.getNodesNotInGroup(group);

        for (Node n1 : sameGroup) {
            Vector2D vals1 = getVector(n1, vectorColumnName);
            for (Node n2 : fromOtherGroups) {
                Vector2D vals2 = getVector(n2, vectorColumnName);
                updateGroupSimilarity(vals1, vals2);
            }
        }
        return speedSimStats.getMean();
    }

}
