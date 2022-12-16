package com.epam.deltix.dfp;

import java.util.Random;

import static com.epam.deltix.dfp.Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

class Demo {
    interface DecimalOperation {
        @Decimal
        long apply(@Decimal final long a, @Decimal final long b);
    }

    static class Operation {
        final String text;
        final DecimalOperation calc;

        public Operation(final String text, final DecimalOperation calc) {
            this.text = text;
            this.calc = calc;
        }
    }

    static final Operation[] operations = {
        new Operation("+", new DecimalOperation() {
            @Override
            public long apply(@Decimal final long a, @Decimal final long b) {
                return Decimal64Utils.add(a, b);
            }
        }),

        new Operation("-", new DecimalOperation() {
            @Override
            public long apply(@Decimal final long a, @Decimal final long b) {
                return Decimal64Utils.subtract(a, b);
            }
        }),

        new Operation("*", new DecimalOperation() {
            @Override
            public long apply(@Decimal final long a, @Decimal final long b) {
                return Decimal64Utils.multiply(a, b);
            }
        }),

        new Operation("/", new DecimalOperation() {
            @Override
            public long apply(@Decimal final long a, @Decimal final long b) {
                return Decimal64Utils.divide(a, b);
            }
        }),
    };

    public static void main(final String[] args) {
        final long argA;
        final long argB;
        final String operation;

        if (args.length == 0) {
            final int TWICE_OF_MAX_SIGNIFICAND_DIGITS = MAX_SIGNIFICAND_DIGITS * 2;
            final int HALF_OF_MAX_SIGNIFICAND_DIGITS = MAX_SIGNIFICAND_DIGITS / 2;
            final int exponentMin = -TWICE_OF_MAX_SIGNIFICAND_DIGITS - HALF_OF_MAX_SIGNIFICAND_DIGITS;
            final int exponentMax = TWICE_OF_MAX_SIGNIFICAND_DIGITS - HALF_OF_MAX_SIGNIFICAND_DIGITS;
            int exponentRange = exponentMax - exponentMin;

            final Random random = new Random();

            argA = Decimal64Utils.fromFixedPoint(random.nextLong() >> random.nextInt(64),
                -(random.nextInt(exponentRange) + exponentMin));
            argB = Decimal64Utils.fromFixedPoint(random.nextLong() >> random.nextInt(64),
                -(random.nextInt(exponentRange) + exponentMin));

            operation = operations[random.nextInt(operations.length)].text;

        } else if (args.length == 3) {
            argA = Decimal64Utils.parse(args[0]);
            argB = Decimal64Utils.parse(args[2]);
            operation = args[1];

        } else {
            System.err.println("Usage: <A> <operation> <B>");
            System.err.println("List of operations:");
            for (final Operation op : operations)
                System.err.println("    " + op.text);
            System.exit(1);
            return;
        }

        for (final Operation op : operations) {
            if (op.text.equals(operation)) {
                final long result = op.calc.apply(argA, argB);

                System.out.println(Decimal64Utils.toScientificString(argA) + "(=0x" + Long.toHexString(argA) + "L) " + operation + " " +
                    Decimal64Utils.toScientificString(argB) + "(=0x" + Long.toHexString(argB) + "L) = " +
                    Decimal64Utils.toScientificString(result) + "(=0x" + Long.toHexString(result) + "L)");

                return;
            }
        }

        throw new RuntimeException("Unsupported operation '" + operation + "'.");
    }
}
