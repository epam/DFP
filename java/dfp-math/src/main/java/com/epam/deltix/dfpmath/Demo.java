package com.epam.deltix.dfpmath;

import com.epam.deltix.dfp.Decimal;
import com.epam.deltix.dfp.Decimal64Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.epam.deltix.dfp.Decimal64Utils.MAX_SIGNIFICAND_DIGITS;

class Demo {
    static final Method[] operations = initOperations(Decimal64MathUtils.class);

    static Method[] initOperations(final Class<?> clazz) {
        final List<Method> operations = new ArrayList<>();

        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()))
                continue;

            if (!method.getReturnType().equals(long.class) ||
                method.getAnnotation(Decimal.class) == null)
                continue;

            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1 || !parameterTypes[0].equals(long.class))
                continue;

            boolean isDecimalArgAnnotation = false;
            {
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (final Annotation annotation : parameterAnnotations[0]) {
                    if (annotation.annotationType().equals(Decimal.class)) {
                        isDecimalArgAnnotation = true;
                        break;
                    }
                }
            }
            if (!isDecimalArgAnnotation)
                continue;

            operations.add(method);
        }

        Collections.sort(operations, new Comparator<Method>() {
            @Override
            public int compare(final Method o1, final Method o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        return operations.toArray(new Method[0]);
    }

    public static void main(final String[] args) {
        final long argX;
        final String operation;

        if (args.length == 0) {
            final int TWICE_OF_MAX_SIGNIFICAND_DIGITS = MAX_SIGNIFICAND_DIGITS * 2;
            final int HALF_OF_MAX_SIGNIFICAND_DIGITS = MAX_SIGNIFICAND_DIGITS / 2;
            final int exponentMin = -TWICE_OF_MAX_SIGNIFICAND_DIGITS - HALF_OF_MAX_SIGNIFICAND_DIGITS;
            final int exponentMax = TWICE_OF_MAX_SIGNIFICAND_DIGITS - HALF_OF_MAX_SIGNIFICAND_DIGITS;
            int exponentRange = exponentMax - exponentMin;

            final Random random = new Random();

            argX = Decimal64Utils.fromFixedPoint(random.nextLong() >> random.nextInt(64),
                -(random.nextInt(exponentRange) + exponentMin));

            operation = operations[random.nextInt(operations.length)].getName();

        } else if (args.length == 2) {
            argX = Decimal64Utils.parse(args[1]);
            operation = args[0];

        } else {
            System.err.println("Usage: <Operation> <X>");
            System.err.println("List of operations:");
            for (final Method op : operations)
                System.err.println("    " + op.getName());
            System.exit(1);
            return;
        }

        for (final Method op : operations) {
            if (op.getName().equals(operation)) {
                final long result;
                try {
                    result = (long)op.invoke(null, argX);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

                System.out.println(operation + "( " + Decimal64Utils.toScientificString(argX) + "(=0x" + Long.toHexString(argX) + "L) ) = " +
                    Decimal64Utils.toScientificString(result) + "(=0x" + Long.toHexString(result) + "L)");

                return;
            }
        }

        throw new RuntimeException("Unsupported operation '" + operation + "'.");
    }
}
