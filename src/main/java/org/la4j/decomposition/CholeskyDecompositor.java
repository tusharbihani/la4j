/*
 * Copyright 2011-2013, by Vladimir Kostyukov and Contributors.
 * 
 * This file is part of la4j project (http://la4j.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributor(s): -
 * 
 */

package org.la4j.decomposition;

import org.la4j.factory.Factory;
import org.la4j.matrix.Matrices;
import org.la4j.matrix.Matrix;
import org.la4j.vector.Vector;

public class CholeskyDecompositor implements MatrixDecompositor {

    @Override
    public Matrix[] decompose(Matrix matrix, Factory factory) {

        if (matrix.rows() != matrix.columns()) {
            throw new IllegalArgumentException("Wrong matrix size:");
        }

        if (!matrix.is(Matrices.SYMMETRIC_MATRIX)) {
            throw new IllegalArgumentException();
        }

        if (!isPositiveDefinite(matrix)) {
            throw new IllegalArgumentException();
        }

        Matrix u = factory.createMatrix(matrix.rows(), matrix.rows());

        for (int j = 0; j < u.rows(); j++) {

            double d = 0.0;

            for (int k = 0; k < j; k++) {

                double s = 0.0;

                for (int i = 0; i < k; i++) {
                    s += u.unsafe_get(k, i) * u.unsafe_get(j, i);
                }

                s = (matrix.unsafe_get(j, k) - s) / u.unsafe_get(k, k);

                u.unsafe_set(j, k, s);

                d = d + s * s;
            }

            d = matrix.unsafe_get(j, j) - d;

            u.unsafe_set(j, j, Math.sqrt(Math.max(d, 0.0)));

            for (int k = j + 1; k < u.rows(); k++) {
                u.unsafe_set(j, k, 0.0);
            }
        }

        return new Matrix[] { u };
    }

    private boolean isPositiveDefinite(Matrix matrix) {

        //
        // TODO: create a MatrixPredicate for it
        //

        int n = matrix.rows();
        boolean result = true;

        Matrix l = matrix.blank();

        for (int j = 0; j < n; j++) {

            Vector rowj = l.getRow(j);

            double d = 0.0;
            for (int k = 0; k < j; k++) {

                Vector rowk = l.getRow(k);
                double s = 0.0;

                for (int i = 0; i < k; i++) {
                    s += rowk.unsafe_get(i) * rowj.unsafe_get(i);
                }

                s = (matrix.unsafe_get(j, k) - s) / l.unsafe_get(k, k);

                rowj.unsafe_set(k, s);
                l.setRow(j, rowj);

                d = d + s * s;
            }

            d = matrix.unsafe_get(j, j) - d;

            result = result && (d > 0.0);

            l.unsafe_set(j, j, Math.sqrt(Math.max(d, 0.0)));

            for (int k = j + 1; k < n; k++) {
                l.unsafe_set(j, k, 0.0);
            }
        }

        return result;
    }
}
