/* Copyright 2024 Luc Maisonobe
 * Licensed to Luc Maisonobe under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spaceroots.prisma;

import org.hipparchus.analysis.differentiation.Gradient;
import org.hipparchus.linear.Array2DRowRealMatrix;
import org.hipparchus.linear.ArrayRealVector;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.linear.RealVector;
import org.hipparchus.optim.nonlinear.vector.leastsquares.MultivariateJacobianFunction;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.Pair;

import java.util.List;
import java.util.Locale;

/** Model least squares problem model.
 * @author Luc Maisonobe
 */
public class Model implements MultivariateJacobianFunction {

    /** Observed measurements. */
    private final List<ObservedMeasurement> observed;

    /** Flag for showing evaluations. */
    private final boolean showEvaluations;

    /** Evaluations counter. */
    private int evaluationsCounter;

    /** Simple constructor.
     * @param observed observed measurements
     * @param showEvaluations if true, Levenberg-Marquardt evaluations are shown
     */
    public Model(final List<ObservedMeasurement> observed,
                 final boolean showEvaluations) {
        this.observed           = observed;
        this.showEvaluations    = showEvaluations;
        this.evaluationsCounter = 0;
        if (showEvaluations) {
            System.out.format(Locale.US, "evaluation     R        α₁       α₂       α₃%n");
        }
     }

    /** {@inheritDoc} */
    @Override
    public Pair<RealVector, RealMatrix> value(final RealVector point) {

        ++evaluationsCounter;
        if (showEvaluations) {
            System.out.format(Locale.US, "    %2d      %7.3f  %6.4f  %6.4f  %6.4f%n",
                              evaluationsCounter,
                              point.getEntry(0),
                              FastMath.toDegrees(point.getEntry(1)),
                              FastMath.toDegrees(point.getEntry(2)),
                              FastMath.toDegrees(FastMath.PI - point.getEntry(1) - point.getEntry(2)));
        }

        // create current estimate of the triangle
        final Triangle triangle = new Triangle(point.getEntry(0), point.getEntry(1), point.getEntry(2));

        // allocate arrays
        final RealVector value    = new ArrayRealVector(observed.size());
        final RealMatrix jacobian = new Array2DRowRealMatrix(observed.size(), 3);

        // fill up results
        for (int i = 0; i < observed.size(); ++i) {

            // compute the theoretical measurement corresponding to the observed measurement
            final Gradient theoretical = triangle.theoreticalMeasurement(observed.get(i));

            // distribute value and derivatives for the least squares problem solver
            value.setEntry(i, theoretical.getValue());
            jacobian.setRow(i, theoretical.getGradient());

        }

        return new Pair<>(value, jacobian);

    }

}
