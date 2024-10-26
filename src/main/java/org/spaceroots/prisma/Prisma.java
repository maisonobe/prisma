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
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
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
     * @exception IOException if input file cannot be read
     */
    Prisma(final Path input) throws IOException {

        // parse measurements
        observed = new ArrayList<>();
        try (FileInputStream   fis = new FileInputStream(input.toFile());
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader    br  = new BufferedReader(isr)) {
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                if (line.startsWith("#")) {
                    // skip commented-out lines
                    continue;
                }
                final String[] fields = line.split("\\s+");
                if (fields.length != 4) {
                    throw new RuntimeException("invalid measurement: " + line);
                }

                // replace unicode subscript characters
                final String vertexField = fields[0].replace('₁', '1').replace('₂', '2').replace('₃', '3');

                observed.add(new ObservedMeasurement(Vertex.valueOf(vertexField),
                                                     Double.parseDouble(fields[1]),
                                                     Double.parseDouble(fields[2]),
                                                     Double.parseDouble(fields[3])));
            }
        }

        if (observed.size() < 3) {
            throw new RuntimeException("not enough measurements");
        }

    }

    /** Assess prismatic rule geometry.
     * @param showEvaluations if true, Levenberg-Marquardt evaluations are shown
     */
    void assessGeometry(final boolean showEvaluations) {

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
                maxIterations(20).
                maxEvaluations(20).
                model(new Model(observed, showEvaluations)).
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
     * @exception IOException if input file cannot be read
     */
    public static void main(final String[] args) throws IOException {
        mainWithCustomizedErrorHandling(System.err, System::exit, args);
    }

    /**
     * Program entry point with customized failure handling.
     * @param errorOutput        error output
     * @param errorStatusHandler handler for error status
     * @param args program arguments (path to measurement file)
     * @exception IOException if input file cannot be read
     */
    static void mainWithCustomizedErrorHandling(final PrintStream errorOutput,
                                                final IntConsumer errorStatusHandler,
                                                final String[] args)
        throws IOException {

        // parse program arguments
        boolean showEvaluations = false;
        boolean displayResiduals = false;
        boolean plot = false;
        String measurementsName = null;
        for (final String arg : args) {
            switch (arg) {
                case "--show-evaluations":
                    showEvaluations = true;
                    break;
                case "--residuals":
                    displayResiduals = true;
                    break;
                case "--plot":
                    plot = true;
                    break;
                default:
                    measurementsName = arg;
                    break;
            }
        }
        if (measurementsName == null) {
            errorOutput.append("usage: java org.spaceroots.prima.Prisma [--show-evaluations] [--residuals] [--plot] measurements.txt");
            errorStatusHandler.accept(1);
            return;
        }

        // read input file
        final FileSystem fs = FileSystems.getDefault();
        final Path cwd = fs.getPath(System.getProperty("user.dir"));
        final Prisma prisma = new Prisma(cwd.resolve(fs.getPath(measurementsName)).normalize());

        // assess geometrical properties
        prisma.assessGeometry(showEvaluations);
        final Triangle triangle = prisma.getAssessedGeometry();
        final RealVector sigma = prisma.optimum.getSigma(1.0e-10);

        // display results
        if (showEvaluations) {
            System.out.format(Locale.US, "%n");
        }
        System.out.format(Locale.US, "R = %.3f (±%.3f), α₁ = %.4f (±%.4f), α₂ = %.4f (±%.4f) ⇒ α₃ ≈ %.4f%nRMS = %.4f%n",
                          triangle.getR(), sigma.getEntry(0), FastMath.toDegrees(triangle.getAlpha1()),
                          FastMath.toDegrees(sigma.getEntry(1)), FastMath.toDegrees(triangle.getAlpha2()),
                          FastMath.toDegrees(sigma.getEntry(2)), FastMath.toDegrees(triangle.getAlpha3()),
                          prisma.optimum.getRMS());

        if (displayResiduals) {
            System.out.format(Locale.US, "%n index top    d     h      observed  theoretical residual%n");
            for (int i = 0; i < prisma.observed.size(); i++) {
                final ObservedMeasurement oi = prisma.observed.get(i);
                final double t = triangle.theoreticalMeasurement(oi).getValue();
                System.out.format(Locale.US, "  %2d    %s  %5.2f %5.2f %10.3f %10.3f  %9.3f%n", i, oi.getTop(),
                                  oi.getD(), oi.getH(), oi.getM(), t, oi.getM() - t);
            }
        }

        if (plot) {
            final SortedSet<Residual> faceA1A2 = new TreeSet<>(Comparator.comparingDouble(Residual::getLocation));
            final SortedSet<Residual> faceA2A3 = new TreeSet<>(Comparator.comparingDouble(Residual::getLocation));
            final SortedSet<Residual> faceA3A1 = new TreeSet<>(Comparator.comparingDouble(Residual::getLocation));
            prisma.observed.forEach(o -> triangle.distributeResiduals(o, faceA1A2, faceA2A3, faceA3A1));
            final ProcessBuilder pb = new ProcessBuilder("gnuplot").
                            redirectOutput(ProcessBuilder.Redirect.INHERIT).
                            redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.environment().remove("XDG_SESSION_TYPE");
            final Process gnuplot = pb.start();
            try (PrintStream out = new PrintStream(gnuplot.getOutputStream(), false, StandardCharsets.UTF_8.name())) {
                final String output = measurementsName.substring(0, measurementsName.lastIndexOf('.')) + ".png";
                out.format(Locale.US, "set terminal pngcairo size %d, %d%n", 1000, 1000);
                out.format(Locale.US, "set output '%s'%n", output);
                out.format(Locale.US, "set xlabel 'distance to first vertex (mm)'%n");
                out.format(Locale.US, "set ylabel 'residual (mm)'%n");
                out.format(Locale.US, "set title '%s'%n", "residuals along prismatic rule faces");
                out.format(Locale.US, "$a1a2 <<EOD%n");
                faceA1A2.forEach(r -> out.format(Locale.US, "%.6f %.6f%n", r.getLocation(), r.getResidual()));
                out.format(Locale.US, "EOD%n");
                out.format(Locale.US, "$a2a3 <<EOD%n");
                faceA2A3.forEach(r -> out.format(Locale.US, "%.6f %.6f%n", r.getLocation(), r.getResidual()));
                out.format(Locale.US, "EOD%n");
                out.format(Locale.US, "$a3a1 <<EOD%n");
                faceA3A1.forEach(r -> out.format(Locale.US, "%.6f %.6f%n", r.getLocation(), r.getResidual()));
                out.format(Locale.US, "EOD%n");
                out.format(Locale.US, "plot $a1a2 using 1:2 with linespoints dt 3 pt 5 title 'face A₁-A₂', \\%n");
                out.format(Locale.US, "     $a2a3 using 1:2 with linespoints dt 3 pt 7 title 'face A₂-A₃', \\%n");
                out.format(Locale.US, "     $a3a1 using 1:2 with linespoints dt 3 pt 9 title 'face A₃-A₁'%n");
                System.out.format(Locale.US, "plot written to %s%n", output);
            }
        }

    }

}
