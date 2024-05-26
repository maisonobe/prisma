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

/** Container for one measurement.
 * @author Luc Maisonobe
 */
public class ObservedMeasurement {

    /** Top vertex. */
    private final Vertex top;

    /** Cylindrical pin diameter. */
    private final double d;

    /** Spacer block height. */
    private final double h;

    /** Measured value. */
    private final double m;

    /** Simple constructor.
     * @param top top vertex
     * @param d   cylindrical pin diameter
     * @param h   spacer block height
     * @param m   measured value
     */
    public ObservedMeasurement(final Vertex top, final double d, final double h, final double m) {
        this.top = top;
        this.d   = d;
        this.h   = h;
        this.m   = m;
    }

    /** Get top vertex.
     * @return top vertex
     */
    public Vertex getTop() {
        return top;
    }

    /** Get cylindrical pin diameter.
     * @return cylindrical pin diameter
     */
    public double getD() {
        return d;
    }

    /** Get spacer block height.
     * @return spacer block height
     */
    public double getH() {
        return h;
    }

    /** Get measured value.
     * @return measured value
     */
    public double getM() {
        return m;
    }

}
