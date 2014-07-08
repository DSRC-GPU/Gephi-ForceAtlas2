package crowdlanes.metrics;

import crowdlanes.Coords2D;
import org.gephi.data.attributes.type.FloatList;

public class CosineSimilarity {

    public static double similarity(Coords2D v1, Coords2D v2) {

        double mag1 = v1.getLen();
        double mag2 = v2.getLen();

        if (mag1 == 0 || mag2 == 0) {
            return Double.NaN;
        }

        double cs = dotProduct(v1, v2) / mag1 / mag2;
        return Math.max(-1.0, Math.min(1.0, cs));
    }

    public static double similarity(FloatList l1, FloatList l2) {
        float x1, x2, y1, y2;

        x1 = l1.getItem(0);
        x2 = l2.getItem(0);

        y1 = l1.getItem(1);
        y2 = l2.getItem(1);

        double mag1 = Math.sqrt(x1 * x1 + y1 * y1);
        double mag2 = Math.sqrt(x2 * x2 + y2 * y2);

        if (mag1 == 0 || mag2 == 0) {
            return Double.NaN;
        }

        double cs = dotProduct(l1, l2) / mag1 / mag2;
        return Math.max(-1.0, Math.min(1.0, cs));
    }

    public static double dotProduct(FloatList l1, FloatList l2) {
        float x1, x2, y1, y2;

        x1 = l1.getItem(0);
        x2 = l2.getItem(0);
        y1 = l1.getItem(1);
        y2 = l2.getItem(1);

        return (x1 * x2 + y1 * y2);
    }

    public static double dotProduct(Coords2D v1, Coords2D v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public static double angularSimilarity(Coords2D v1, Coords2D v2) {
        double cs = CosineSimilarity.similarity(v1, v2);
        if (Double.isNaN(cs)) {
            return Double.NaN;
        }

        return 1 - (Math.acos(cs) / Math.PI);
    }

    public static double angularSimilarity(FloatList v1, FloatList v2) {
        double cs = CosineSimilarity.similarity(v1, v2);
        if (Double.isNaN(cs)) {
            return Double.NaN;
        }

        return 1 - (Math.acos(cs) / Math.PI);
    }
}
