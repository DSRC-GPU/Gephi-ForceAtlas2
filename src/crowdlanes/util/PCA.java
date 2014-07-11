package crowdlanes.util;

import la.matrix.Matrix;
import ml.utils.ArrayOperator;
import ml.utils.Matlab;

public class PCA {

    public static Matrix run(Matrix X, int r) {
        int N = Matlab.size(X, 1);
        double[] S = Matlab.sum(X).getPr();
        ArrayOperator.divideAssign(S, N);
        X = X.copy();
        int M = X.getColumnDimension();
        double s = 0.0D;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < M; j++) {
                s = S[j];
                if (s != 0.0D) {
                    X.setEntry(i, j, X.getEntry(i, j) - s);
                }
            }
        }
        Matrix XT = X.transpose();
        Matrix Psi = XT.mtimes(X);

        return X.mtimes(Matlab.eigs(Psi, r, "lm")[0]);
    }
}
