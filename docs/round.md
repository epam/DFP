# Rounding in DFP

# Table of Contents

* [All rounding methods overview](#AllRoundingMethodsOverview)
    - [RoundXXX methods](#RoundXXXMethods)
    - [Commonly used roundings](#CommonlyUsedRoundings)
    - [New optimized rounding methods](#NewOptimizedRoundingMethods)
* [Preferable methods](#PreferableMethods)
* [Why to replace `roundXXX(multiple)` with `roundToReciprocal`](#WhyToReplaceRoundXXXMultipleWithRoundToReciprocal)
    - [Precision loss on `roundTowardsPositiveInfinity(multiple)`](#PrecisionLossOnRoundTowardsPositiveInfinityMultiple)
    - [Precision loss on `roundTowardsNegativeInfinity(multiple)`](#PrecisionLossOnRoundTowardsNegativeInfinityMultiple)
    - [Precision loss on `roundToNearestTiesAwayFromZero(multiple)`](#PrecisionLossOnRoundToNearestTiesAwayFromZeroMultiple)
    - [Precision loss on `roundToNearestTiesToEven(multiple)`](#PrecisionLossOnRoundToNearestTiesToEvenMultiple)
* [How to replace `roundXXX(multiple)` with `roundToReciprocal`](#HowToReplaceRoundXXXMultipleWithRoundToReciprocal)
* [Benchmarks](#Benchmarks)
    - [RoundXXX benchmark](#RoundXXXBenchmark)
    - [`roundToReciprocal` vs `roundXXX(multiple)` benchmark](#RoundToReciprocalBenchmark)
    - [`isRounded` and `isRoundedToReciprocal` benchmark](#IsRoundedBenchmark)

## All rounding methods overview<a name="AllRoundingMethodsOverview"></a>

There are many rounding methods in DFP.

> **Note**<a name="DeprecatedMethods"></a>
> Many rounding methods are deprecated by various reason. See methods description for more details.

All rounding methods can be separated to several groups:

* [RoundXXX methods](#RoundXXXMethods)
* [Commonly used roundings](#CommonlyUsedRoundings)
* [New optimized rounding methods](#NewOptimizedRoundingMethods)

### RoundXXX methods<a name="RoundXXXMethods"></a>

| Rounding method                                                                      | Description                                                                                                                                                                                                                                                                |
|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `roundTowardsPositiveInfinity()`[Deprecated](#DeprecatedMethods)                     | Alias to `ceil()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as deprecated because the `ceiling()` method name was selected as preferable.                                              |
| `roundTowardsNegativeInfinity()`[Deprecated](#DeprecatedMethods)                     | Alias to `floor()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value. <br> Method was marked as deprecated because the `floor()` method name was selected as preferable.                                           |
| `roundTowardsZero()`[Deprecated](#DeprecatedMethods)                                 | Alias to `truncate()`. <br> Rounds value toward zero, returning the nearest integral value that is not larger in magnitude than original value. <br> Method was marked as deprecated because rare usage.                                                                   |
| `roundToNearestTiesAwayFromZero()`[Deprecated](#DeprecatedMethods)                   | Alias to `round()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero. <br> Method was marked as deprecated because the `round()` method name was selected as preferable.                                                |
| `roundToNearestTiesToEven()`[Deprecated](#DeprecatedMethods)                         | Call original `bid64_round_integral_nearest_even()`. <br> Returns the nearest `DFP` value that is equal to a mathematical integer, with ties rounding to even. <br> Method was marked as deprecated because `round(0, RoundingMode.HALF_EVEN)` was selected as preferable. |
| `roundTowardsPositiveInfinity(Decimal64 multiple)`[Deprecated](#DeprecatedMethods)   | Syntactic sugar to `multiply(ceiling(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.CEILING)` provides better performance and precision.                                                               |
| `roundTowardsNegativeInfinity(Decimal64 multiple)`[Deprecated](#DeprecatedMethods)   | Syntactic sugar to `multiply(floor(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.FLOOR)` provides better performance and precision.                                                                   |
| `roundToNearestTiesAwayFromZero(Decimal64 multiple)`[Deprecated](#DeprecatedMethods) | Alias to `round(Decimal64 multiple)`.                                                                                                                                                                                                                                      |
| `roundToNearestTiesToEven(Decimal64 multiple)`[Deprecated](#DeprecatedMethods)       | Syntactic sugar to `multiply(roundToNearestTiesToEven(divide(value, multiple)), multiple);`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.HALF_EVEN)` provides better performance and precision.                                           |

### Commonly used roundings<a name="CommonlyUsedRoundings"></a>

| Rounding method                                             | Description                                                                                                                                                                                                                                                 |
|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ceil()`[Deprecated](#DeprecatedMethods)                    | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as deprecated because the `ceiling()` method name was selected as preferable. |
| `ceiling()`                                                 | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as preferable by Vitali? according to C# naming?                              |
| `floor()`                                                   | Call original `bid64_round_integral_negative()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value. <br>                                                                                             |
| `truncate()`[Deprecated](#DeprecatedMethods)                | Call original `bid64_round_integral_zero()`. <br> Rounds value toward zero, returning the nearest integral value that is not larger in magnitude than original value. <br> Method was marked as deprecated because rare usage.                              |
| `round()`                                                   | Call original `bid64_round_integral_nearest_away()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero.                                                                                                   |
| `round(Decimal64 multiple)`[Deprecated](#DeprecatedMethods) | Syntactic sugar to `multiply(round(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.HALF_UP)` provides better performance and precision.                                                  |

### New optimized rounding methods<a name="NewOptimizedRoundingMethods"></a>

| Rounding method                                    | Description                                                                                                                                                                                                                                                                                                         |
|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `round(int n, RoundingMode roundType)`             | Returns the `DFP` value that is rounded according the selected rounding type.                                                                                                                                                                                                                                       |
| `roundToReciprocal(int r, RoundingMode roundType)` | Returns the `DFP` value that is rounded to the value, reciprocal to r, according the selected rounding type. <br> This method provides better performance and precision than the rounding methods with `multiple` argument. But, of course, this method can be used only if the `1/multiple` is a positive integer. |

Test is the value properly rounded according to specific precision can be performed with next methods.

| Test rounding method           | Description                                                                                                                                                                             |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `isRounded(int n)`             | Returns the sign of the value is rounded to the 10<sup>-n</sup>. This method provides better performance than the `round(n, RoundingMode.ROUND_UNNECESSARY)` call.                      |
| `isRoundedToReciprocal(int r)` | Returns the sign of the value is rounded to the value, reciprocal to r. This method provides better performance than the `roundedToReciprocal(r, RoundingMode.ROUND_UNNECESSARY)` call. |

## Preferable methods<a name="PreferableMethods"></a>

The preferable rounding method are listed in the next table.

| Rounding method                                    | Description                                                                                                                                                                             |
|----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ceiling()`                                        | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value.                                  |
| `floor()`                                          | Call original `bid64_round_integral_negative()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value.                              |
| `round()`                                          | Call original `bid64_round_integral_nearest_away()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero.                               |
| `round(int n, RoundingMode roundType)`             | Returns the `DFP` value that is rounded according the selected rounding type.                                                                                                           |
| `roundToReciprocal(int r, RoundingMode roundType)` | Returns the `DFP` value that is rounded to the value, reciprocal to r, according the selected rounding type.                                                                            |
| `isRounded(int n)`                                 | Returns the sign of the value is rounded to the 10<sup>-n</sup>. This method provides better performance than the `round(n, RoundingMode.ROUND_UNNECESSARY)` call.                      |
| `isRoundedToReciprocal(int r)`                     | Returns the sign of the value is rounded to the value, reciprocal to r. This method provides better performance than the `roundedToReciprocal(r, RoundingMode.ROUND_UNNECESSARY)` call. |

## Why to replace `roundXXX(multiple)` with `roundToReciprocal`<a name="WhyToReplaceRoundXXXMultipleWithRoundToReciprocal"></a>

The methods `roundTowardsPositiveInfinity(Decimal64 multiple)`, `roundTowardsNegativeInfinity(Decimal64 multiple)`,
`roundToNearestTiesAwayFromZero(Decimal64 multiple)`, `roundToNearestTiesToEven(Decimal64 multiple)` are marked as
deprecated because the new `roundToReciprocal(int r, RoundingMode roundType)` method was added.

The method `roundToReciprocal(int r, RoundingMode roundType)` provides better performance, better precision and also
makes API more graceful.

The performance comparison is presented in the [Benchmarks](#Benchmarks) part of this document.

The precision loss on the division is the significant problem of the `roundXXX` methods.
Let's review next examples where the multiplier will be calculated as the reciprocal value of `r` argument.

The reference value will be calculated with BigDecimal.
The reference value is calculated in two ways:

- `bigDecimalMultiplierWay`, where the reference value is calculated as

    ```
    bigDecimalMultiplierWay =
        value.toBigDecimal()
             .divide(bigMultiple, MathContext.DECIMAL128)
             .setScale(0, roundType)
             .multiply(bigMultiple)
    ```

  where

    ```
    bigMultiple = BigDecimal.ONE.divide(new BigDecimal(r), MathContext.DECIMAL128)
    ```

- `bigDecimalReciprocalWay`, where the reference value is calculated as
    ```
    bigDecimalReciprocalWay =
        value.toBigDecimal()
             .multiply(new BigDecimal(r))
             .setScale(0, roundType)
             .divide(new BigDecimal(r), MathContext.DECIMAL128)
    ```

### Precision loss on `roundTowardsPositiveInfinity(multiple)`<a name="PrecisionLossOnRoundTowardsPositiveInfinityMultiple"></a>

| Argument  | Value                                              |
|-----------|----------------------------------------------------|
| value     | 0.08 or underlying `3566850904877432840L`          |
| r         | 3957050                                            |
| multiple  | 0.0000002527135113278831 or `3566850904877432840L` |
| roundType | RoundingMode.CEILING                               |

| Equation                       | Result                                     |
|--------------------------------|--------------------------------------------|
| `roundTowardsPositiveInfinity` | 0.08000025271351131                        |
| `roundToReciprocal`            | 0.08                                       |
| `bigDecimalMultiplierWay`      | 0.0800000000000000000000000000000000127204 |
| `bigDecimalReciprocalWay`      | 0.08                                       |

### Precision loss on `roundTowardsNegativeInfinity(multiple)`<a name="PrecisionLossOnRoundTowardsNegativeInfinityMultiple"></a>

| Argument  | Value                                             |
|-----------|---------------------------------------------------|
| value     | 0.5 or underlying `3575858104132173829L`          |
| r         | 506012                                            |
| multiple  | 0.000001976237717682585 or `3575858104132173829L` |
| roundType | RoundingMode.FLOOR                                |

| Equation                       | Result                                    |
|--------------------------------|-------------------------------------------|
| `roundTowardsNegativeInfinity` | 0.4999980237622824                        |
| `roundToReciprocal`            | 0.5                                       |
| `bigDecimalMultiplierWay`      | 0.499999999999999999999999999999999960058 |
| `bigDecimalReciprocalWay`      | 0.5                                       |

### Precision loss on `roundToNearestTiesAwayFromZero(multiple)`<a name="PrecisionLossOnRoundToNearestTiesAwayFromZeroMultiple"></a>

| Argument  | Value                                                |
|-----------|------------------------------------------------------|
| value     | 0.00004 or underlying `3539829307113209860L`         |
| r         | 891087500                                            |
| multiple  | 0.000000001122224248460449 or `3539829307113209860L` |
| roundType | RoundingMode.HALF_UP                                 |

| Equation                         | Result                                       |
|----------------------------------|----------------------------------------------|
| `roundToNearestTiesAwayFromZero` | 0.00003999943888787578                       |
| `roundToReciprocal`              | 0.00004000056111212423                       |
| `bigDecimalMultiplierWay`        | 0.000040000561112124230224304571661032164220 |
| `bigDecimalReciprocalWay`        | 0.00004000056111212423022430457166103217     |

### Precision loss on `roundToNearestTiesToEven(multiple)`<a name="PrecisionLossOnRoundToNearestTiesToEvenMultiple"></a>

| Argument  | Value                                               |
|-----------|-----------------------------------------------------|
| value     | 0.00144 or underlying `3539829307113210000L`        |
| r         | 55559375                                            |
| multiple  | 0.00000001799876258507228 or `3539829307113210000L` |
| roundType | RoundingMode.HALF_EVEN                              |

| Equation                   | Result                                      |
|----------------------------|---------------------------------------------|
| `roundToNearestTiesToEven` | 0.001439991000618708                        |
| `roundToReciprocal`        | 0.001440008999381293                        |
| `bigDecimalMultiplierWay`  | 0.00144000899938129253613814050284042944546 |
| `bigDecimalReciprocalWay`  | 0.001440008999381292536138140502840430      |

## How to replace `roundXXX(multiple)` with `roundToReciprocal`<a name="HowToReplaceRoundXXXMultipleWithRoundToReciprocal"></a>

Obviously, not all the cases of the `roundXXX(multiple)` can be replaced with `roundToReciprocal`.
Only the cases where `multiple` can be represented as `1/r` are subjects for replacement.
Therefore, only the cases when `0 < multiple < 1` are subject for replacement.

You can easily test is the multiple can be exactly represented by `1 / r` with next part of code:

```java
/**
 * Converts positive multiple to the exact reciprocal, or return -1 if there is no exact integer representation.
 *
 * @param multiple Positive multiplier value.
 * @return Integer value, reciprocal to multiple, or -1.
 */
public static int getExactReciprocal(final Decimal64 multiple) {
    if (!multiple.isPositive())
        throw new IllegalArgumentException("The multiple(=" + multiple + ") must be positive.");

    final int r = Decimal64.ONE.divide(multiple).toInt();

    return Decimal64.ONE.divideByInteger(r).equals(multiple) ? r : -1;
}

@Test
public void testReciprocalCalculation() {
    assertEquals(25600, getExactReciprocal(Decimal64.parse("0.0000390625")));

    // This test will fail, because Decimal64 can't calculate 1/(1/6) without precision loss
    //assertEquals(6, getExactReciprocal(Decimal64.ONE.divideByInteger(6)));
}
```

The cases when the `multiple` can't exactly represent `1 / r`, (e.g. `r = 6`, so `multiple = 0.1666666666666667`)
must be processed with careful according to the business requirements.

Obviously, the `roundToReciprocal` method can be enhanced to the case of rounding to the ratio `n / d`,
where the `n` and `d` are positive integer values. But for now there is no business requirement for such function.

So, in general for the case, then the `r` value can be calculated and cached,
the `roundToReciprocal` must be called instead of `roundXXX(multiple)`. Otherwise, the original function must be called.

## Benchmarks

All the benchmarks where performed on the next platform:

```
11th Gen Intel(R) Core(TM) i7-1185G7 @ 3.00GHz
Windows 10 Pro 21H2 19044.2130 *
JMH version: 1.25
VM version: JDK 11.0.14.1, Eclipse Adoptium OpenJDK 64-Bit Server VM, 11.0.14.1+1
```

## RoundXXX benchmark<a name="RoundXXXBenchmark"></a>

This test report was generated by RoundingBenchmark.java

For this test 1000 random values where generated and each test was processed these values on one call.
I.e. `Score` colum represents average time interval of 1000 values processing.

Next table represents performance of the rounding with `roundXXX` via JNI calls to the compiled native library.

| Benchmark                            | Mode | Cnt | Score     | Error      | Units |
|--------------------------------------|------|-----|-----------|------------|-------|
| roundTowardsPositiveInfinityNative   | avgt | 15  | 13693,661 | ±  182,765 | ns/op |
| roundTowardsNegativeInfinityNative   | avgt | 15  | 13682,684 | ±  176,024 | ns/op |
| roundTowardsZeroNative               | avgt | 15  | 11522,545 | ±  200,853 | ns/op |
| roundToNearestTiesAwayFromZeroNative | avgt | 15  | 11864,454 | ±  655,077 | ns/op |
| roundToNearestTiesToEvenNative       | avgt | 15  | 14170,887 | ± 1140,730 | ns/op |

This table represents performance of the rounding with `roundXXX` functions ported from C to Java.

| Benchmark                            | Mode | Cnt | Score     | Error      | Units |
|--------------------------------------|------|-----|-----------|------------|-------|
| roundTowardsPositiveInfinity         | avgt | 15  | 11779,079 | ± 1445,235 | ns/op |
| roundTowardsNegativeInfinity         | avgt | 15  | 10964,737 | ± 1040,513 | ns/op |
| roundTowardsZero                     | avgt | 15  | 9793,022  | ±  560,275 | ns/op |
| roundToNearestTiesAwayFromZero       | avgt | 15  | 10130,422 | ±  545,036 | ns/op |
| roundToNearestTiesToEven             | avgt | 15  | 13606,541 | ± 1682,309 | ns/op |

This table represents performance of rounding with `round(int n, RoundingMode roundType)` method with
different `roundType` values.

| Benchmark                            | Mode | Cnt | Score     | Error      | Units |
|--------------------------------------|------|-----|-----------|------------|-------|
| roundUp                              | avgt | 15  | 10026,050 | ±  414,826 | ns/op |
| roundDown                            | avgt | 15  |  8956,215 | ±  554,607 | ns/op |
| roundCeiling                         | avgt | 15  | 11290,324 | ±  410,007 | ns/op |
| roundFloor                           | avgt | 15  | 12088,918 | ± 1200,449 | ns/op |
| roundHalfUp                          | avgt | 15  | 10446,453 | ±  260,959 | ns/op |
| roundHalfDown                        | avgt | 15  | 10622,215 | ±  233,144 | ns/op |
| roundHalfEven                        | avgt | 15  | 11821,198 | ±  402,190 | ns/op |

## `roundToReciprocal` vs `roundXXX(multiple)` benchmark<a name="RoundToReciprocalBenchmark"></a>

This test report was generated by RoundToReciprocalBenchmark.java

For this test 1000 random values where generated and each test was processed these values on one call.
I.e. `Score` colum represents average time interval of 1000 values processing.

This table represents performance of rounding with `roundXXX(multiple)` methods.

| Benchmark                      | Mode | Cnt | Score     | Error      | Units |
|--------------------------------|------|-----|-----------|------------|-------|
| roundTowardsPositiveInfinity   | avgt | 15  | 65508,535 | ± 2302,583 | ns/op |
| roundTowardsNegativeInfinity   | avgt | 15  | 65512,331 | ± 1247,969 | ns/op |
| roundToNearestTiesAwayFromZero | avgt | 15  | 62781,442 | ± 2518,620 | ns/op |
| roundToNearestTiesToEven       | avgt | 15  | 67989,410 | ± 6482,265 | ns/op |

This table represents performance of rounding with `roundToReciprocal` methods.

| Benchmark                      | Mode | Cnt | Score     | Error      | Units |
|--------------------------------|------|-----|-----------|------------|-------|
| roundToReciprocalUp            | avgt | 15  | 38691,927 | ± 2390,368 | ns/op |
| roundToReciprocalDown          | avgt | 15  | 31411,574 | ±  667,242 | ns/op |
| roundToReciprocalCeiling       | avgt | 15  | 35522,005 | ± 1030,140 | ns/op |
| roundToReciprocalFloor         | avgt | 15  | 35175,477 | ± 1231,514 | ns/op |
| roundToReciprocalHalfUp        | avgt | 15  | 34517,866 | ± 1163,755 | ns/op |
| roundToReciprocalHalfDown      | avgt | 15  | 38549,762 | ± 4718,530 | ns/op |
| roundToReciprocalHalfEven      | avgt | 15  | 39554,523 | ±  772,916 | ns/op |

## `isRounded` and `isRoundedToReciprocal` benchmark<a name="IsRoundedBenchmark"></a>

This test report was generated by ExceptionCatchBenchmark.java

The test of is value properly rounded can be performed in two ways:
* Call `round(n, RoundingMode.ROUND_UNNECESSARY)` and catch the exception for non properly rounded values.
* Call `isRounded(int n)` and process result.

For the `roundToReciprocal` and `isRoundedToReciprocal` situation is the same.

Obviously, the performance of the data processing with the `round(n, RoundingMode.ROUND_UNNECESSARY)` method
depends on how many exceptions will be generated. The next benchmark show performance of processing
one positive (`0.99`) and one negative (`0.9999999999999999`) case.

| Benchmark                  | (valueStr)         | Mode | Cnt | Score   | Error    | Units |
|----------------------------|--------------------|------|-----|---------|----------|-------|
| isRoundedToTenPower        | 0.99               | avgt | 15  | 2,773   | ±  0,145 | ns/op |
| isRoundedToTenPowerCatch   | 0.99               | avgt | 15  | 4,376   | ±  0,333 | ns/op |
| isRoundedToTenPower        | 0.9999999999999999 | avgt | 15  | 4,166   | ±  0,124 | ns/op |
| isRoundedToTenPowerCatch   | 0.9999999999999999 | avgt | 15  | 921,002 | ± 84,690 | ns/op |
| isRoundedToReciprocal      | 0.99               | avgt | 15  | 21,357  | ±  2,708 | ns/op |
| isRoundedToReciprocalCatch | 0.99               | avgt | 15  | 20,657  | ±  0,319 | ns/op |
| isRoundedToReciprocal      | 0.9999999999999999 | avgt | 15  | 15,031  | ±  1,559 | ns/op |
| isRoundedToReciprocalCatch | 0.9999999999999999 | avgt | 15  | 974,290 | ± 90,568 | ns/op |

So, this test show how costly can be `round(n, RoundingMode.ROUND_UNNECESSARY)` on the data with improper rounding.
