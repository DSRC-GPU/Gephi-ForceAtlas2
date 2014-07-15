package crowdlanes.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import la.matrix.DenseMatrix;
import la.matrix.Matrix;

public class CompareCommunities {
    
    public static void reindex_membership(List<Integer> v) {
        List<Integer> oldSorted = new ArrayList<>(v);
        List<Integer> newToOldReal = new ArrayList<>();
        Collections.sort(oldSorted);
        
        int lastId = v.get(0) - 1;
        for (Integer thisId : oldSorted) {
            if (lastId != thisId) {
                newToOldReal.add(thisId);
                lastId = thisId;
            }
        }
        
        for (int i = 0; i < v.size(); i++) {
            int thisId = v.get(i);
            int pos = newToOldReal.indexOf(thisId);
            v.set(i, pos);         
        }
    }

    public static double nmi(List<Integer> v1, List<Integer> v2) {
        
        reindex_membership(v1);
        reindex_membership(v2);

        int k1 = Collections.max(v1) + 1;
        int k2 = Collections.max(v2) + 1;
        Matrix m = new DenseMatrix(k1, k2);

        double[] p1 = new double[k1];
        double[] p2 = new double[k2];
        for (int i = 0; i < p1.length; i++) {
            p1[i] = 0;
        }

        for (int i = 0; i < p2.length; i++) {
            p2[i] = 0;
        }

        for (Integer i : v1) {
            p1[i]++;
        }
        double h1 = 0;
        for (int i = 0; i < p1.length; i++) {
            p1[i] /= v1.size();
            h1 -= p1[i] * Math.log(p1[i]);
        }

        for (Integer i : v2) {
            p2[i]++;
        }
        double h2 = 0;
        for (int i = 0; i < p2.length; i++) {
            p2[i] /= v2.size();
            h2 -= p2[i] * Math.log(p2[i]);
        }

        for (int i = 0; i < p1.length; i++) {
            p1[i] = Math.log(p1[i]);
        }

        for (int i = 0; i < p2.length; i++) {
            p2[i] = Math.log(p2[i]);
        }

        for (int i = 0; i < k1; i++) {
            for (int j = 0; j < k2; j++) {
                m.setEntry(i, j, 0);
            }
        }

        for (int i = 0; i < v1.size(); i++) {
            double val = m.getEntry(v1.get(i), v2.get(i));
            m.setEntry(v1.get(i), v2.get(i), val + 1);
        }

        double mut_inf = 0;
        for (int i = 0; i < k1; i++) {
            for (int j = 0; j < k2; j++) {
                double val = m.getEntry(i, j);
                if (val == 0) {
                    continue;
                }

                double p = val / v1.size();
                mut_inf += p * (Math.log(p) - p1[i] - p2[j]);
            }
        }

        if (h1 == 0 && h2 == 0) {
            return 1;
        } else {
            return 2 * mut_inf / (h1 + h2);
        }
    }
}
