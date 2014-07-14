package crowdlanes.metrics;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class CosineSimilarity {

    public static double similarity(Vector2D v1, Vector2D v2) {

        double mag1 = v1.getNorm();
        double mag2 = v2.getNorm();

        if (mag1 == 0 || mag2 == 0) {
            return Double.NaN;
        }

        return Vector2D.angle(v1, v2);
    }

    public static double angularSimilarity(Vector2D v1, Vector2D v2) {
        double cs = CosineSimilarity.similarity(v1, v2);
        if (Double.isNaN(cs)) {
            return Double.NaN;
        }

        return 1 - (Math.acos(cs) / Math.PI);
    }
}
