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
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresBuilder;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresOptimizer.Optimum;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LevenbergMarquardtOptimizer;
import org.hipparchus.util.FastMath;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/** Tool for assessing a prismatic rule geometry.
 * @author Luc Maisonobe
 */
public class Prisma {

    /** Observed measurements. */
    private final List<ObservedMeasurement> observed;

    /** Least squares problem solution. */
    private Optimum optimum;

    /** Simple constructor.
     * @param input path of mesurements
     * @exception IOException if measurements cannot be read
     */
    Prisma(final Path input) throws IOException {

        // parse measurements
        observed = new ArrayList<>();
        try (FileInputStream   fis = new FileInputStream(input.toFile());
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

    }

    /** Fit prismatic rule geometry.
     */
    void assessGeometry() {

        // prepare least squares problem
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
                maxEvaluations(1000).
                model(new Model(observed)).
                target(target).
                start(new double[] { sum / observed.size(), FastMath.PI / 3, FastMath.PI / 3 }).
                build();

        // solve problem
        optimum = new LevenbergMarquardtOptimizer().optimize(problem);
    }

    /** Get assessed geometry.
     * @return assessed geometry
     */
    Triangle getAssessedGeometry() {
        return new Triangle(optimum.getPoint().getEntry(0),
                            optimum.getPoint().getEntry(1),
                            optimum.getPoint().getEntry(2));
    }

    /**
     * Program entry point.
     * @param args program arguments (path to measurement file)
     */
    public static void main(final String[] args) {
        mainWithCustomizedErrorHandling(System.err, System::exit, args);
    }

    /**
     * Program entry point with customized failure handling.
     * @param errorOutput        error output
     * @param errorStatusHandler handler for error status
     * @param args program arguments (path to measurement file)
     */
    static void mainWithCustomizedErrorHandling(final PrintStream errorOutput, final IntConsumer errorStatusHandler,
                                                final String[] args) {
        try
        {

            // parse program arguments
            boolean displayResiduals = false;
            String  measurementsName = null;
            for (final String arg : args) {
                if (arg.equals("-residuals")) {
                    displayResiduals = true;
                } else {
                    measurementsName = arg;
                }
            }
            if (measurementsName == null) {
                errorOutput.append("usage: java org.spaceroots.prima.Prisma [--residuals] measurements.txt");
                errorStatusHandler.accept(1);
                return;
            }

            // read input file
            final FileSystem fs     = FileSystems.getDefault();
            final Path       cwd    = fs.getPath(System.getProperty("user.dir"));
            final Prisma     prisma = new Prisma(cwd.resolve(fs.getPath(measurementsName)).normalize());

            // assess geometrical properties
            prisma.assessGeometry();
            final Triangle triangle = prisma.getAssessedGeometry();
            final RealVector sigma = prisma.optimum.getSigma(1.0e-10);

            // display results
            System.out.format(Locale.US, "R = %.6f (±%.6f), α₁ = %.3f (±%.3f), α₂ = %.3f (±%.3f) ⇒ α₃ ≈ %.3f%nRMS = %.6f%n",
                              triangle.getR(),
                              sigma.getEntry(0),
                              FastMath.toDegrees(triangle.getAlpha1()),
                              FastMath.toDegrees(sigma.getEntry(1)),
                              FastMath.toDegrees(triangle.getAlpha2()),
                              FastMath.toDegrees(sigma.getEntry(2)),
                              FastMath.toDegrees(triangle.getAlpha3()),
                              prisma.optimum.getRMS());

            if (displayResiduals) {
                System.out.format(Locale.US, "%n# index top  d     h   observed  theoretical residual%n");
                for (int i = 0; i < prisma.observed.size(); i++) {
                    final ObservedMeasurement oi = prisma.observed.get(i);
                    final double t = triangle.theoreticalMeasurement(oi).getValue();
                    System.out.format(Locale.US, "   %2d    %s %4.1f %4.1f %10.6f %10.6f %9.6f%n",
                                      i, oi.getTop(), oi.getD(), oi.getH(), oi.getM(), t, oi.getM() - t);
                }
            }

        } catch (IOException e) {
            errorOutput.append(e.getLocalizedMessage());
            errorStatusHandler.accept(1);
        }
    }

}
