package org.spaceroots.prisma;

import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

public class PrismaTest {

    @Test
    public void test606060() {
        doTest("prismatic-rule-60-60-60.txt", 22.0, 60.0, 60.0, 60.0, 5.0e-4, 1.2e-3);
    }

    @Test
    public void test454590() {
        doTest("prismatic-rule-45-45-90.txt", 40.0, 45.0, 45.0, 90.0, 7.6e-4, 5.0e-3);
    }

    @Test
    public void test456075() {
        doTest("prismatic-rule-45-60-75.txt", 60.0, 45.0, 60.0, 75.0, 4.4e-4, 1.0e-3);
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
    public void testMainError() {
        final AtomicInteger returnStatus = new AtomicInteger(0);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Prisma.mainWithCustomizedErrorHandling(new PrintStream(baos), returnStatus::getAndSet, new String[0]);
        Assertions.assertEquals(1, returnStatus.get());
        Assertions.assertEquals("usage: java org.spaceroots.prima.Prisma [--residuals] measurements.txt",
                                baos.toString().trim());
    }

    @Test
    public void testMain() {
        Prisma.main(new String[] { "-residuals", findPath("prismatic-rule-45-45-90.txt").toString() });
    }

    private void doTest(final String name, final double r,
                        final double alpha1Deg, final double alpha2Deg, final double alpha3Deg,
                        final double tolR, final double tolApha) {
        try {
            final Path input  = findPath(name);
            Prisma prisma = new Prisma(input);
            prisma.assessGeometry();
            Triangle triangle = prisma.getAssessedGeometry();
            Assertions.assertEquals(r,         triangle.getR(),                          tolR);
            Assertions.assertEquals(alpha1Deg, FastMath.toDegrees(triangle.getAlpha1()), tolApha);
            Assertions.assertEquals(alpha2Deg, FastMath.toDegrees(triangle.getAlpha2()), tolApha);
            Assertions.assertEquals(alpha3Deg, FastMath.toDegrees(triangle.getAlpha3()), tolApha);
        } catch (IOException ioe) {
            Assertions.fail(ioe.getLocalizedMessage());
        }
    }

    private static Path findPath(final String name)
    {
        Path path = null;
        try
        {
            final URL url = Thread.currentThread().getContextClassLoader().getResource(name);
            if (url == null)
            {
                Assertions.fail(name);
            }
            path = Paths.get(url.toURI());
        } catch (URISyntaxException use)
        {
            Assertions.fail(use.getLocalizedMessage());
        }
        return path;
    }

}
