# Rounding in DFP

# Table of Contents

* [Overview of all rounding methods](#AllRoundingMethodsOverview)
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

## Overview of all rounding methods<a name="AllRoundingMethodsOverview"></a>

There are many rounding methods in DFP.

> **Note**<a name="DeprecatedMethods"></a><br>
> Some rounding methods are deprecated by various reason. Refer to the description of methods for details.

All rounding methods can be split into three groups:

* [RoundXXX methods](#RoundXXXMethods)
* [Commonly used roundings](#CommonlyUsedRoundings)
* [New optimized rounding methods](#NewOptimizedRoundingMethods)

### RoundXXX methods<a name="RoundXXXMethods"></a>

| Rounding method                                                                      | Description                                                                                                                                                                                                                                                                |
|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `roundTowardsPositiveInfinity()`[Deprecated](#DeprecatedMethods)                     | Alias to `ceil()`. <br> Rounds the value upwards, returning the smallest integral value that is not less than the original value. <br> This method is deprecated because the `ceiling()` is considered a preferred method.                                              |
| `roundTowardsNegativeInfinity()`[Deprecated](#DeprecatedMethods)                     | Alias to `floor()`. <br> Rounds the value downwards, returning the largest integral value that is not greater than the original value. <br> This method is deprecated because the `floor()` is considered a preferred method.                                           |
| `roundTowardsZero()`[Deprecated](#DeprecatedMethods)                                 | Alias to `truncate()`. <br> Rounds the value towards zero, returning the nearest integral value that is not larger in magnitude than the original value. <br> This method is deprecated due to its rare usage.                                                                   |
| `roundToNearestTiesAwayFromZero()`[Deprecated](#DeprecatedMethods)                   | Alias to `round()`. <br> Returns the nearest to the original value, with halfway cases rounded away from zero. <br> This method is deprecated because the `round()` is considered a preferred method.                                                |
| `roundToNearestTiesToEven()`[Deprecated](#DeprecatedMethods)                         | Call original `bid64_round_integral_nearest_even()`. <br> Returns the nearest `DFP` value that is equal to a mathematical integer, with ties rounding to even. <br> This method is deprecated because `round(0, RoundingMode.HALF_EVEN)` is considered a preferred method. |
| `roundTowardsPositiveInfinity(Decimal64 multiple)`[Deprecated](#DeprecatedMethods)   | Syntactic sugar to `multiply(ceiling(divide(value, multiple)), multiple)`. <br> This method is deprecated because `roundToReciprocal(int r, RoundingMode.CEILING)` provides a better performance and precision.                                                               |
| `roundTowardsNegativeInfinity(Decimal64 multiple)`[Deprecated](#DeprecatedMethods)   | Syntactic sugar to `multiply(floor(divide(value, multiple)), multiple)`. <br> This method is deprecated because `roundToReciprocal(int r, RoundingMode.FLOOR)` provides a better performance and precision.                                                                   |
| `roundToNearestTiesAwayFromZero(Decimal64 multiple)`[Deprecated](#DeprecatedMethods) | Alias to `round(Decimal64 multiple)`.                                                                                                                                                                                                                                      |
| `roundToNearestTiesToEven(Decimal64 multiple)`[Deprecated](#DeprecatedMethods)       | Syntactic sugar to `multiply(roundToNearestTiesToEven(divide(value, multiple)), multiple);`. <br> This method is deprecated because `roundToReciprocal(int r, RoundingMode.HALF_EVEN)` provides a better performance and precision.                                           |

### Commonly used roundings<a name="CommonlyUsedRoundings"></a>

| Rounding method                                             | Description                                                                                                                                                                                                                                                 |
|-------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ceil()`[Deprecated](#DeprecatedMethods)                    | Call original `bid64_round_integral_positive()`. <br> Rounds the value upwards, returning the smallest integral value that is not less than the original value. <br> This method is deprecated because the `ceiling()` is considered a preferred method. |
| `ceiling()`                                                 | Call original `bid64_round_integral_positive()`. <br> Rounds the value upwards, returning the smallest integral value that is not less than the original value. <br> This is a preferred method.                              |
| `floor()`                                                   | Call original `bid64_round_integral_negative()`. <br> Rounds the value downwards, returning the largest integral value that is not greater than the original value. <br>                                                                                             |
| `truncate()`[Deprecated](#DeprecatedMethods)                | Call original `bid64_round_integral_zero()`. <br> Rounds the value towards zero, returning the nearest integral value that is not larger in magnitude than the original value. <br> This method is deprecated due to its rare usage.                              |
| `round()`                                                   | Call original `bid64_round_integral_nearest_away()`. <br> Returns the nearest to the original value, with halfway cases rounded away from zero.                                                                                                   |
| `round(Decimal64 multiple)`[Deprecated](#DeprecatedMethods) | Syntactic sugar to `multiply(round(divide(value, multiple)), multiple)`. <br> This method is deprecated because `roundToReciprocal(int r, RoundingMode.HALF_UP)` provides a better performance and precision.                                                  |

### New optimized rounding methods<a name="NewOptimizedRoundingMethods"></a>

| Rounding method                                    | Description                                                                                                                                                                                                                                                                                                         |
|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `round(int n, RoundingMode roundType)`             | Returns the `DFP` value that is rounded according to the selected rounding type.                                                                                                                                                                                                                                       |
| `roundToReciprocal(int r, RoundingMode roundType)` | Returns the `DFP` value that is rounded to the value, reciprocal to `r`, according to the selected rounding type. <br> This method provides a better performance and precision than rounding methods with the `multiple` argument. This method can only be used if `1/multiple` is a positive integer. |

You can use the following methods to test whether the value is properly rounded according to specific precision.

| Test rounding method           | Description                                                                                                                                                                             |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `isRounded(int n)`             | Returns the sign of the value that is rounded to the 10<sup>-n</sup>. This method provides a better performance than `round(n, RoundingMode.ROUND_UNNECESSARY)`.                      |
| `isRoundedToReciprocal(int r)` | Returns the sign of the value that is rounded to the value, reciprocal to `r`. This method provides a better performance than `roundedToReciprocal(r, RoundingMode.ROUND_UNNECESSARY)`. |

## Preferred methods<a name="PreferableMethods"></a>

Preferred rounding methods are listed in the following table.

| Rounding method                                    | Description                                                                                                                                                                             |
|----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ceiling()`                                        | Call original `bid64_round_integral_positive()`. <br> Rounds the value upwards, returning the smallest integral value that is not less than the original value.                                  |
| `floor()`                                          | Call original `bid64_round_integral_negative()`. <br> Rounds the value downwards, returning the largest integral value that is not greater than the original value.                              |
| `round()`                                          | Call original `bid64_round_integral_nearest_away()`. <br> Returns the nearest to the original value, with halfway cases rounded away from zero.                               |
| `round(int n, RoundingMode roundType)`             | Returns the `DFP` value that is rounded according to the selected rounding type.                                                                                                           |
| `roundToReciprocal(int r, RoundingMode roundType)` | Returns the `DFP` value that is rounded to the value, reciprocal to `r`, according to the selected rounding type.                                                                            |
| `isRounded(int n)`                                 | Returns the sign of the value that is rounded to the 10<sup>-n</sup>. This method provides a better performance than `round(n, RoundingMode.ROUND_UNNECESSARY)`.                      |
| `isRoundedToReciprocal(int r)`                     | Returns the sign of the value that is rounded to the value, reciprocal to `r`. This method provides a better performance than `roundedToReciprocal(r, RoundingMode.ROUND_UNNECESSARY)`. |

## Why to replace `roundXXX(multiple)` with `roundToReciprocal`<a name="WhyToReplaceRoundXXXMultipleWithRoundToReciprocal"></a>

Methods 

* `roundTowardsPositiveInfinity(Decimal64 multiple)`
* `roundTowardsNegativeInfinity(Decimal64 multiple)`
* `roundToNearestTiesAwayFromZero(Decimal64 multiple)`
* `roundToNearestTiesToEven(Decimal64 multiple)` 

are deprecated because the new `roundToReciprocal(int r, RoundingMode roundType)` method was added.

The method `roundToReciprocal(int r, RoundingMode roundType)` provides a better performance, a better precision, and also
makes API more graceful.

> Refer to [Benchmarks](#Benchmarks) to learn more about performance comparison.

The precision loss on the division is a significant problem of the `roundXXX` methods.

Let's take a look at examples, where the multiplier is calculated as the reciprocal value of `r` argument and the reference value is calculated with `BigDecimal`.

There are two way to calculate the reference value:

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
| roundType | `RoundingMode.CEILING`                               |

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
| roundType | `RoundingMode.FLOOR`                                |

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
| roundType | `RoundingMode.HALF_UP`                                 |

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
| roundType | `RoundingMode.HALF_EVEN`                              |

| Equation                   | Result                                      |
|----------------------------|---------------------------------------------|
| `roundToNearestTiesToEven` | 0.001439991000618708                        |
| `roundToReciprocal`        | 0.001440008999381293                        |
| `bigDecimalMultiplierWay`  | 0.00144000899938129253613814050284042944546 |
| `bigDecimalReciprocalWay`  | 0.001440008999381292536138140502840430      |

## How to replace `roundXXX(multiple)` with `roundToReciprocal`<a name="HowToReplaceRoundXXXMultipleWithRoundToReciprocal"></a>

`roundXXX(multiple)` cannot always be replaced with `roundToReciprocal`. You can do it only when `multiple` can be represented as `1/r`.
In other words, you can do it only when `0 < multiple < 1`.

Use this code to test whether `multiple` can be represented as `1/r`:

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

The cases, when `multiple` cannot be represented precisely as `1/r`, (e.g. `r = 6`, so `multiple = 0.1666666666666667`)
must be processed with care according to the existing business requirements.

The `roundToReciprocal` method can be enhanced for the rounding to the ratio `n/d`,
where `n` and `d` are positive integer values. There are no business requirements for such function at the moment.

As a general rule, when the `r` value can be calculated and cached,
call `roundToReciprocal` instead of `roundXXX(multiple)`. Otherwise, call the original function.

## Benchmarks

All the benchmarks are performed on this platform:

```
11th Gen Intel(R) Core(TM) i7-1185G7 @ 3.00GHz
Windows 10 Pro 21H2 19044.2130 *
JMH version: 1.25
VM version: JDK 11.0.14.1, Eclipse Adoptium OpenJDK 64-Bit Server VM, 11.0.14.1+1
```

## RoundXXX benchmark<a name="RoundXXXBenchmark"></a>

This test report was generated by `RoundingBenchmark.java`

1000 random values where generated for this test. Each test processed these values on one call. I.e. the `Score` column represents the average time interval for processing 1000 values.

On the next table, you can view the performance of the rounding with `roundXXX` via `JNI` calls to the compiled native library.

| Benchmark                            | Mode | Cnt | Score     | Error      | Units |
|--------------------------------------|------|-----|-----------|------------|-------|
| roundTowardsPositiveInfinityNative   | avgt | 15  | 13693,661 | ±  182,765 | ns/op |
| roundTowardsNegativeInfinityNative   | avgt | 15  | 13682,684 | ±  176,024 | ns/op |
| roundTowardsZeroNative               | avgt | 15  | 11522,545 | ±  200,853 | ns/op |
| roundToNearestTiesAwayFromZeroNative | avgt | 15  | 11864,454 | ±  655,077 | ns/op |
| roundToNearestTiesToEvenNative       | avgt | 15  | 14170,887 | ± 1140,730 | ns/op |

On the next table, you can view the performance of the rounding with `roundXXX` functions ported from C to Java.

| Benchmark                            | Mode | Cnt | Score     | Error      | Units |
|--------------------------------------|------|-----|-----------|------------|-------|
| roundTowardsPositiveInfinity         | avgt | 15  | 11779,079 | ± 1445,235 | ns/op |
| roundTowardsNegativeInfinity         | avgt | 15  | 10964,737 | ± 1040,513 | ns/op |
| roundTowardsZero                     | avgt | 15  | 9793,022  | ±  560,275 | ns/op |
| roundToNearestTiesAwayFromZero       | avgt | 15  | 10130,422 | ±  545,036 | ns/op |
| roundToNearestTiesToEven             | avgt | 15  | 13606,541 | ± 1682,309 | ns/op |

On the next table, you can view the performance of the rounding with the  `round(int n, RoundingMode roundType)` method with
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

This test report was generated by `RoundToReciprocalBenchmark.java`

1000 random values where generated for this test. Each test processed these values on one call. I.e. the `Score` column represents the average time interval for processing 1000 values.

On the next table, you can view the performance of the rounding with `roundXXX(multiple)` methods.

| Benchmark                      | Mode | Cnt | Score     | Error      | Units |
|--------------------------------|------|-----|-----------|------------|-------|
| roundTowardsPositiveInfinity   | avgt | 15  | 65508,535 | ± 2302,583 | ns/op |
| roundTowardsNegativeInfinity   | avgt | 15  | 65512,331 | ± 1247,969 | ns/op |
| roundToNearestTiesAwayFromZero | avgt | 15  | 62781,442 | ± 2518,620 | ns/op |
| roundToNearestTiesToEven       | avgt | 15  | 67989,410 | ± 6482,265 | ns/op |

On the next table, you can view the performance of the rounding with `roundToReciprocal` methods.

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

This test report was generated by `ExceptionCatchBenchmark.java`

There are two ways to test whether the value is rounded properly:
* Call `round(n, RoundingMode.ROUND_UNNECESSARY)` and catch the exception for values that are not rounded properly.
* Call `isRounded(int n)` and process the result.

For the `roundToReciprocal` and `isRoundedToReciprocal`, the situation is the same.

The performance of data processing with the `round(n, RoundingMode.ROUND_UNNECESSARY)` method
depends on the number of generated exceptions. The next benchmark, shows the performance of processing
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

This test shows how costly can `round(n, RoundingMode.ROUND_UNNECESSARY)` be on the data with improper rounding.
