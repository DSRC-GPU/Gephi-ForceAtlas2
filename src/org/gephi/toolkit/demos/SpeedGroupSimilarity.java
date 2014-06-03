package org.gephi.toolkit.demos;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.graph.api.Node;
import static org.gephi.toolkit.demos.GraphUtil.getVector;

public class SpeedGroupSimilarity {

    private final String vectorColumnName;
    private final ArrayList<Double> speedSimResults;

    public SpeedGroupSimilarity(String columnName) {
        speedSimResults = new ArrayList<Double>();
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
        for (Double d : speedSimResults) {
            sum += Math.pow(d - groupSimilarity, 2);
        }
        return Math.sqrt(sum / speedSimResults.size());
    }

    private void updateGroupSimilarity(Node n1, Node n2, FloatList l1, FloatList l2) {
        float x1, x2, y1, y2;
        x1 = l1.getItem(0);
        x2 = l2.getItem(0);
        y1 = l1.getItem(1);
        y2 = l2.getItem(1);

        double mag1 = Math.sqrt(x1 * x1 + y1 * y1);
        double mag2 = Math.sqrt(x2 * x2 + y2 * y2);
        double speedDif = Math.abs(mag1 - mag2);
        speedSimResults.add(speedDif);
    }

    private double getGroupSimilarity() {
        double groupSimilarity = 0;
        for (Double d : speedSimResults) {
            groupSimilarity += d;
        }
        return groupSimilarity / speedSimResults.size();
    }

    public double getInnerGroupSimilarity(int group) {
        speedSimResults.clear();
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
        speedSimResults.clear();
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

}
