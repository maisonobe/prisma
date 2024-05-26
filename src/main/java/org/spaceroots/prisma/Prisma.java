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

import org.hipparchus.linear.RealVector;
import org.hipparchus.optim.SimplePointChecker;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresBuilder;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresOptimizer.Optimum;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LevenbergMarquardtOptimizer;
import org.hipparchus.util.FastMath;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Tool for assessing a prismatic rule geometry.
 * @author Luc Maisonobe
 */
public class Prisma {

    /** Private constructor for utility class.
     */
    private Prisma() {
        // nothing to do
    }

    /** Program entry point.
     * @param args program arguments
     */
    public static void main(final String[] args) throws IOException {
        try {

            if (args.length != 1) {
                System.err.println("usage: java org.spaceroots.prima.Prisma measurements.txt");
                System.exit(1);
            }

            final FileSystem fs    = FileSystems.getDefault();
            final Path       cwd   = fs.getPath(System.getProperty("user.dir"));

            // read input file
            final List<ObservedMeasurement> observed = new ArrayList<>();
            try (FileInputStream   fis = new FileInputStream(cwd.resolve(fs.getPath(args[0])).normalize().toFile());
                 InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader    br  = new BufferedReader(isr)) {
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    final String[] fields = line.split("\\s+");
                    if (fields.length != 4) {
                        throw new RuntimeException("invalid measurement: " + line);
                    }
                    observed.add(new ObservedMeasurement(Vertex.valueOf(fields[0]),
                                                         Double.parseDouble(fields[1]),
                                                         Double.parseDouble(fields[2]),
                                                         Double.parseDouble(fields[3])));
                }
            }
            if (observed.size() < 3) {
                throw new RuntimeException("not enough measurements");
            }

            double sum = 0;
            final double[] target = new double[observed.size()];
            for (int i = 0; i < observed.size(); i++) {
                final double mi = observed.get(i).getM();
                target[i] = mi;
                sum      += mi;
            }

            // build least squares problem
            final LeastSquaresProblem problem =
                new LeastSquaresBuilder().
                    maxIterations(1000).
                    model(new Model(observed)).
                    target(target).
                    start(new double[] { sum / observed.size(), FastMath.PI / 3, FastMath.PI / 3 }).
                    build();

            // solve problem
            final Optimum optimum = new LevenbergMarquardtOptimizer().optimize(problem);
            final Triangle triangle = new Triangle(optimum.getPoint().getEntry(0),
                                                   optimum.getPoint().getEntry(1),
                                                   optimum.getPoint().getEntry(2));
            final RealVector sigma = optimum.getSigma(1.0e-10);

            // display results
            System.out.format(Locale.US, "R = %.6f (±%.6f), α₁ = %.3f (±%.3f), α₂ = %.3f (±%.3f) ⇒ α₃ ≈ %.3f%nRMS = %.6f%n",
                              triangle.getR(),
                              sigma.getEntry(0),
                              FastMath.toDegrees(triangle.getAlpha1()),
                              FastMath.toDegrees(sigma.getEntry(1)),
                              FastMath.toDegrees(triangle.getAlpha2()),
                              FastMath.toDegrees(sigma.getEntry(2)),
                              FastMath.toDegrees(triangle.getAlpha3()),
                              optimum.getRMS());

        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

}
