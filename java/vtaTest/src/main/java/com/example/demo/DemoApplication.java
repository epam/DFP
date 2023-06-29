package com.example.demo;

import com.epam.deltix.dfp.Decimal64;
import com.epam.deltix.dfpmath.Decimal64Math;

import java.security.SecureRandom;

public class DemoApplication {
    public static void main(String[] args) {
        System.out.println("vtaTest");

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

        Decimal64 aDecimal = Decimal64.fromDouble(a);
        Decimal64 bDecimal = Decimal64.fromDouble(b);
        Decimal64 sDecimal = aDecimal.add(bDecimal);
        double s = sDecimal.toDouble();

        System.out.println("Decimal: " +
            aDecimal.toScientificString() + "(=0x" + Long.toHexString(Decimal64.toUnderlying(aDecimal)) + "L) + " +
            bDecimal.toScientificString() + "(=0x" + Long.toHexString(Decimal64.toUnderlying(bDecimal)) + "L) = " +
            sDecimal.toScientificString() + "(=0x" + Long.toHexString(Decimal64.toUnderlying(sDecimal)) + "L) is a double " +
            s + "(=0x" + Long.toHexString(Double.doubleToRawLongBits(s)) + "L)");

        Decimal64 qDecimal = Decimal64Math.sqrt(sDecimal);
        double q = qDecimal.toDouble();

        System.out.println("Decimal: sqrt( " +
            sDecimal.toScientificString() + "(=0x" + Long.toHexString(Decimal64.toUnderlying(sDecimal)) + "L) ) = " +
            qDecimal.toScientificString() + "(=0x" + Long.toHexString(Decimal64.toUnderlying(qDecimal)) + "L) ) is a double " +
            q + "(=0x" + Long.toHexString(Double.doubleToRawLongBits(q)) + "L)");
    }
}
