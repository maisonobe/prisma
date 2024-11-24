package org.spaceroots.prisma;

import org.hipparchus.util.FastMath;
import org.hipparchus.util.SinCos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class PrismaTest {

    @Test
    public void testTmp() {
        final SinCos sc = FastMath.sinCos(FastMath.toRadians(60));
        int count = 0;
        for (double[] dh : new double[][] {
            { 20, -3.7 }, { 20, -3.2 }, { 20, -2.8 }, { 20, -2.5 }, { 12,  0.0 }, { 15,  0.0 }, {  8,  2.5 }, {  8,  2.8 },
            {  8,  3.2 }, { 12,  2.6 }, { 12,  3.1 }, { 12,  3.5 }, { 15,  3.2 }, { 15,  3.6 }, { 20,  2.8 }, { 20,  3.2 },
            { 20,  3.7 }, { 20,  4.1 }, { 20,  4.5 }, { 20,  5.0 }, { 20,  5.4 }, { 20,  5.8 }, { 15,  7.5 }, { 20,  6.7 },
            { 20,  7.2 }, { 20,  7.6 }, { 20,  8.0 }, { 20,  8.5 }, { 20,  8.9 }, { 20,  9.3 }, { 20,  9.8 }, { 20, 10.0 }
        }) {
            final double d = dh[0];
            final double h = dh[1];
            final double r = 20.03;
            double m = 2 * r * SinCos.sum(sc, sc).sin() + (d * (1 + sc.sin()) - (d + 2 * h) * sc.cos()) / sc.sin();
            double l = (2 * h + d * (1 - sc.cos())) / (2 * sc.sin());
            if (l >= 0 && l <= 34.6) {
                System.out.format(Locale.US, "%2d %3.0f %4.1f %6.3f %6.3f%n", ++count, d, h, l, m);
            }
        }
    }

    @Test
    public void test606060() throws IOException {
        doTest("prismatic-rule-60-60-60.txt", 22.0, 60.0, 60.0, 60.0, 5.1e-4, 1.3e-2);
    }

    @Test
    public void test454590() throws IOException {
        doTest("prismatic-rule-45-45-90.txt", 40.0, 45.0, 45.0, 90.0, 6.0e-3, 3.6e-2);
    }

    @Test
    public void test456075() throws IOException {
        doTest("prismatic-rule-45-60-75.txt", 60.0, 45.0, 60.0, 75.0, 1.1e-3, 4.0e-3);
    }

    @Test
    public void testPerfect() throws IOException {
        doTest("perfect-measurements.txt", 60.0, 45.0, 60.0, 75.0, 4.0e-11, 1.9e-10);
    }

    @Test
    public void testRealRuleFirstMeasurementsA() throws IOException {
        doTest("real-rule-first-measurements-endpoint-A.txt", 21.275, 59.715, 60.510, 59.775, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleFirstMeasurementsB() throws IOException {
        doTest("real-rule-first-measurements-endpoint-B.txt", 21.229, 59.599, 60.621, 59.780, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSecondMeasurementsA() throws IOException {
        doTest("real-rule-second-measurements-endpoint-A.txt", 20.808, 60.540, 59.815, 59.645, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSecondMeasurementsB() throws IOException {
        doTest("real-rule-second-measurements-endpoint-B.txt", 21.007, 60.529, 60.027, 59.444, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSecondMeasurementsMiddle() throws IOException {
        doTest("real-rule-second-measurements-middle.txt", 20.900, 60.219, 60.313, 59.468, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleThirdMeasurementsA() throws IOException {
        doTest("real-rule-third-measurements-endpoint-A.txt", 20.615, 60.317, 59.834, 59.848, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleThirdMeasurementsB() throws IOException {
        doTest("real-rule-third-measurements-endpoint-B.txt", 20.734, 60.293, 59.870, 59.837, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleFourthMeasurementsA() throws IOException {
        doTest("real-rule-fourth-measurements-endpoint-A.txt", 20.368, 60.163, 59.908, 59.928, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleFourthMeasurementsB() throws IOException {
        doTest("real-rule-fourth-measurements-endpoint-B.txt", 20.286, 60.151, 59.932, 59.918, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleFifthMeasurementsA() throws IOException {
        doTest("real-rule-fifth-measurements-endpoint-A.txt", 20.105, 60.058, 60.015, 59.928, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleFifthMeasurementsB() throws IOException {
        doTest("real-rule-fifth-measurements-endpoint-B.txt", 20.135, 60.036, 60.025, 59.939, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSixthMeasurementsA() throws IOException {
        doTest("real-rule-sixth-measurements-endpoint-A.txt", 20.048, 60.054, 60.002, 59.945, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSixthMeasurementsB() throws IOException {
        doTest("real-rule-sixth-measurements-endpoint-B.txt", 20.079, 60.011, 60.030, 59.958, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSeventhMeasurementsA() throws IOException {
        doTest("real-rule-seventh-measurements-endpoint-A.txt", 20.023, 60.025, 60.067, 59.908, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleSeventhMeasurementsB() throws IOException {
        doTest("real-rule-seventh-measurements-endpoint-B.txt", 20.062, 60.019, 60.077, 59.904, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleEighthMeasurementsA() throws IOException {
        doTest("real-rule-eighth-measurements-endpoint-A.txt", 20.008, 60.002, 60.069, 59.930, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testRealRuleEighthMeasurementsB() throws IOException {
        doTest("real-rule-eighth-measurements-endpoint-B.txt", 20.037, 60.022, 60.068, 59.910, 1.0e-3, 1.0e-3);
    }

    @Test
    public void testInexistentFile() {
        try {
            new Prisma(findPath("prismatic-rule-60-60-60.txt").resolve("inexistent"));
            Assertions.fail("an exception should have been thrown");
        } catch (IOException ioe) {
            Assertions.assertInstanceOf(FileNotFoundException.class, ioe);
        }
    }

    @Test
    public void testCorruptedLine() throws IOException {
        try {
            new Prisma(findPath("corrupted-line.txt"));
            Assertions.fail("an exception should have been thrown");
        } catch (RuntimeException re) {
            Assertions.assertEquals("invalid measurement: A3 12.0  4.0", re.getLocalizedMessage());
        }
    }

    @Test
    public void testNotEnoughMeasurements() throws IOException {
        try {
            new Prisma(findPath("not-enough-measurements.txt"));
            Assertions.fail("an exception should have been thrown");
        } catch (RuntimeException re) {
            Assertions.assertEquals("not enough measurements", re.getLocalizedMessage());
        }
    }

    @Test
    public void testMainError() throws IOException {
        final AtomicInteger returnStatus = new AtomicInteger(0);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Prisma.mainWithCustomizedErrorHandling(new PrintStream(baos), returnStatus::getAndSet, new String[0]);
        Assertions.assertEquals(1, returnStatus.get());
        Assertions.assertEquals("usage: java org.spaceroots.prima.Prisma [--show-evaluations] [--residuals] [--plot] measurements.txt",
                                baos.toString().trim());
    }

    @Test
    public void testMainwithoutOptions() throws IOException {
        Prisma.main(new String[] { findPath("prismatic-rule-45-60-75.txt").toString() });
    }

    @Test
    public void testMainWithOptionsEighthA() throws IOException {
        Prisma.main(new String[] {
            "--show-evaluations", "--residuals", "--plot",
            findPath("real-rule-eighth-measurements-endpoint-A.txt").toString()
        });
    }

    @Test
    public void testMainWithOptionsEighthB() throws IOException {
        Prisma.main(new String[] {
            "--show-evaluations", "--residuals", "--plot",
            findPath("real-rule-eighth-measurements-endpoint-B.txt").toString()
        });
    }

    private void doTest(final String name, final double r,
                        final double alpha1Deg, final double alpha2Deg, final double alpha3Deg,
                        final double tolR, final double tolApha) throws IOException {
        final Path input = findPath(name);
        Prisma prisma = new Prisma(input);
        prisma.assessGeometry(false);
        Triangle triangle = prisma.getAssessedGeometry();
        Assertions.assertEquals(r, triangle.getR(), tolR);
        Assertions.assertEquals(alpha1Deg, FastMath.toDegrees(triangle.getAlpha1()), tolApha);
        Assertions.assertEquals(alpha2Deg, FastMath.toDegrees(triangle.getAlpha2()), tolApha);
        Assertions.assertEquals(alpha3Deg, FastMath.toDegrees(triangle.getAlpha3()), tolApha);
    }

    private static Path findPath(final String name) {
        Path path = null;
        try {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(name);
            if (url == null) {
                Assertions.fail(name);
            }
            path = Paths.get(url.toURI());
        } catch (URISyntaxException use) {
            Assertions.fail(use.getLocalizedMessage());
        }
        return path;
    }

}
