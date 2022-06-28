package com.example.demo;

import com.epam.deltix.dfp.Decimal64Utils;
import com.epam.deltix.dfpmath.Decimal64MathUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.SecureRandom;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("SpringBoot DFP sample");

        final double a, b;
        if (args.length == 2) {
            a = Double.parseDouble(args[0]);
            b = Double.parseDouble(args[1]);
        } else {
            SecureRandom random = new SecureRandom();
            a = Double.longBitsToDouble(random.nextLong());
            b = Double.longBitsToDouble(random.nextLong());
        }
        System.out.println("Double arguments: " + a + "(=0x" + Long.toHexString(Double.doubleToRawLongBits(a)) + "L) + " +
            b + "(=0x" + Long.toHexString(Double.doubleToRawLongBits(b)) + "L)");

        long aDecimal = Decimal64Utils.fromDouble(a);
        long bDecimal = Decimal64Utils.fromDouble(b);
        long sDecimal = Decimal64Utils.add(aDecimal, bDecimal);
        double s = Decimal64Utils.toDouble(sDecimal);

        System.out.println("Decimal: " +
            Decimal64Utils.toScientificString(aDecimal) + "(=0x" + Long.toHexString(aDecimal) + "L) + " +
            Decimal64Utils.toScientificString(bDecimal) + "(=0x" + Long.toHexString(bDecimal) + "L) = " +
            Decimal64Utils.toScientificString(sDecimal) + "(=0x" + Long.toHexString(sDecimal) + "L) is a double " +
            s + "(=0x" + Long.toHexString(Double.doubleToRawLongBits(s)) + "L)");

        long qDecimal = Decimal64MathUtils.sqrt(sDecimal);
        double q = Decimal64Utils.toDouble(qDecimal);

        System.out.println("Decimal: sqrt( " +
            Decimal64Utils.toScientificString(sDecimal) + "(=0x" + Long.toHexString(sDecimal) + "L) ) = " +
            Decimal64Utils.toScientificString(qDecimal) + "(=0x" + Long.toHexString(qDecimal) + "L) ) is a double " +
            q + "(=0x" + Long.toHexString(Double.doubleToRawLongBits(q)) + "L)");
    }
}
