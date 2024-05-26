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
import org.hipparchus.util.FastMath;
import org.hipparchus.util.FieldSinCos;

/** Container for one triangle model.
 * @author Luc Maisonobe
 */
public class Triangle {

    /** Index of R free variable. */
    private static final int R = 0;

    /** Index of α₁ free variable. */
    private static final int ALPHA_1 = R + 1;

    /** Index of α₂ free variable. */
    private static final int ALPHA_2 = ALPHA_1 + 1;

    /** Total number of free variables. */
    private static final int TOTAL = ALPHA_2 + 1;

    /** Circumscribed circle radius. */
    private final Gradient gR;

    /** First angle. */
    private final Gradient g1;

    /** Second angle. */
    private final Gradient g2;

    /** Third angle. */
    private final Gradient g3;

    /** Simple constructor.
     * @param r      circumscribed circle radius
     * @param alpha1 first angle
     * @param alpha2 second angle
     */
    public Triangle(final double r, final double alpha1, final double alpha2) {
        this.gR = Gradient.variable(TOTAL, R,       r);
        this.g1 = Gradient.variable(TOTAL, ALPHA_1, alpha1);
        this.g2 = Gradient.variable(TOTAL, ALPHA_2, alpha2);
        this.g3 = g1.add(g2).subtract(FastMath.PI).negate();
    }

    /** Get circumscribed circle radius.
     * @return circumscribed circle radius
     */
    public double getR() {
        return gR.getValue();
    }

    /** Get first angle.
     * @return first angle
     */
    public double getAlpha1() {
        return g1.getValue();
    }

    /** Get second angle.
     * @return second angle
     */
    public double getAlpha2() {
        return g2.getValue();
    }

    /** Get third angle.
     * @return third angle
     */
    public double getAlpha3() {
        return g3.getValue();
    }

    /** Evaluate theoretical measurement.
     * @param observed observed measurement
     * @return theoretical measurement
     */
    public Gradient theoreticalMeasurement(final ObservedMeasurement observed) {

        // get the bottom angles
        final Gradient alphaA;
        final Gradient alphaB;
        switch (observed.getTop()) {
            case A1:
                alphaA = g2;
                alphaB = g3;
                break;
            case A2:
                alphaA = g1;
                alphaB = g3;
                break;
            case A3:
                alphaA = g1;
                alphaB = g2;
                break;
            default:
                // this should never happen
                throw new RuntimeException("impossible case, please contact program author");
        }

        // compute theoretical measurement
        return bottomLength(alphaA, alphaB).
               add(pinOffset(alphaA, observed.getD(), observed.getH())).
               add(pinOffset(alphaB, observed.getD(), observed.getH()));

    }

    /** Compute length of bottom side.
     * @param alphaA first angle at bottom
     * @param alphaB second angle at bottom
     * @return length of bottom side
     */
    private Gradient bottomLength(final Gradient alphaA, final Gradient alphaB) {
        return gR.multiply(2).multiply(alphaA.add(alphaB).sin());
    }

    /** Compute cylindrical pin offset with respect to bottom side.
     * @param alpha angle at bottom on pin side
     * @param d cylindrical pin diameter
     * @param h space block height
     * @return cylindrical pin offset with respect to bottom side
     */
    private Gradient pinOffset(final Gradient alpha, final double d, final double h) {
        final FieldSinCos<Gradient> sc = alpha.sinCos();
        return sc.sin().add(1).multiply(d).add(sc.cos().multiply(d + 2 * h)).
               divide(sc.sin().multiply(2));
    }

}
