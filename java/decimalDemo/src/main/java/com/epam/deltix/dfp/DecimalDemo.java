package com.epam.deltix.dfp;

public class DecimalDemo {
    public static void main(final String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: <A> <op> <B>");
            System.exit(1);
        }

        final long argA = Decimal64Utils.parse(args[0]);
        final long argB = Decimal64Utils.parse(args[2]);
        final long result = processOperation(args[1], argA, argB);

        System.out.println(Decimal64Utils.toString(argA) + "(=" + argA + ") " + args[1] + " " +
            Decimal64Utils.toString(argB) + "(=" + argB + ") = " +
            Decimal64Utils.toString(result) + "(=" + result + ")");
    }

    private static long processOperation(final String operation, final long argA, final long argB) {
        switch (operation) {
            case "+":
                return Decimal64Utils.add(argA, argB);
            case "-":
                return Decimal64Utils.subtract(argA, argB);
            case "*":
                return Decimal64Utils.multiply(argA, argB);
            case "/":
                return Decimal64Utils.divide(argA, argB);
            default:
                throw new RuntimeException("Unsupported operation '" + operation + "'.");
        }
    }
}
