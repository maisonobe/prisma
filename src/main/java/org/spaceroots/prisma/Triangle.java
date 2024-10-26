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
import org.hipparchus.util.SinCos;

import java.util.SortedSet;

/** Container for one triangle model for the cross-section of a prismatic rule.
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
            default:
                alphaA = g1;
                alphaB = g2;
        }

        // compute theoretical measurement
        return sideLength(alphaA, alphaB).
               add(pinOffset(alphaA, observed.getD(), observed.getH())).
               add(pinOffset(alphaB, observed.getD(), observed.getH()));

    }

    /** Distribute residuals along faces.
     * @param observed observed measurement
     * @param faceA1A2 holder where to put residuals along the A₁-A₂ face
     * @param faceA2A3 holder where to put residuals along the A₂-A₃ face
     * @param faceA3A1 holder where to put residuals along the A₃-A₁ face
     */
    public void distributeResiduals(final ObservedMeasurement observed,
                                    final SortedSet<Residual> faceA1A2,
                                    final SortedSet<Residual> faceA2A3,
                                    final SortedSet<Residual> faceA3A1) {

        // extract observed values
        final double d = observed.getD();
        final double h = observed.getH();
        final double m = observed.getM();

        // compute residual
        final double residual = m - theoreticalMeasurement(observed).getValue();

        // distribute residual along the two slanted faces
        switch (observed.getTop()) {
            case A1:
                faceA1A2.add(new Residual(sideLength(g1, g2).getValue() - contactLocation(g2, d, h), residual));
                faceA3A1.add(new Residual(contactLocation(g3, d, h), residual));
                break;
            case A2:
                faceA1A2.add(new Residual(contactLocation(g1, d, h), residual));
                faceA2A3.add(new Residual(sideLength(g2, g3).getValue() - contactLocation(g3, d, h), residual));
                break;
            default:
                faceA2A3.add(new Residual(contactLocation(g2, d, h), residual));
                faceA3A1.add(new Residual(sideLength(g3, g1).getValue() - contactLocation(g1, d, h), residual));
        }

    }

    /** Compute length of one side.
     * @param alphaA angle at first vertex
     * @param alphaB angle at second vertex
     * @return length of side between the two vertices
     */
    private Gradient sideLength(final Gradient alphaA, final Gradient alphaB) {
        return gR.multiply(2).multiply(alphaA.add(alphaB).sin());
    }

    /** Compute cylindrical pin offset with respect to bottom side.
     * @param alpha angle at bottom on cylindrical pin side
     * @param d cylindrical pin diameter
     * @param h spacer block height
     * @return cylindrical pin offset with respect to bottom side
     */
    private Gradient pinOffset(final Gradient alpha, final double d, final double h) {
        final FieldSinCos<Gradient> sc = alpha.sinCos();
        return sc.sin().add(1).multiply(d).subtract(sc.cos().multiply(d + 2 * h)).
               divide(sc.sin().multiply(2));
    }

    /** Compute location of contact point with respect to bottom vertex.
     * @param alpha angle at bottom on cylindrical pin side
     * @param d cylindrical pin diameter
     * @param h spacer block height
     * @return location of contact point with respect to bottom vertex
     */
    private double contactLocation(final Gradient alpha, final double d, final double h) {
        final SinCos sc = FastMath.sinCos(alpha.getValue());
        return (h + 0.5 * d * (1 - sc.cos())) / sc.sin();
    }

}
