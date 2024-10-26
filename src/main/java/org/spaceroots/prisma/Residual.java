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

/** Container for one residual along a face.
 * @author Luc Maisonobe
 */
public class Residual {

    /** Location relative to first vertex. */
    private final double location;

    /** Residual value. */
    private final double residual;

    /** Simple constructor.
     * @param location location relative to first vertex
     * @param residual residual value
     */
    public Residual(final double location, final double residual) {
        this.location = location;
        this.residual = residual;
    }

    /** Get location relative to first vertex.
     * @return location relative to first vertex
     */
    public double getLocation() {
        return location;
    }

    /** Get residual value.
     * @return residual value
     */
    public double getResidual() {
        return residual;
    }

}
