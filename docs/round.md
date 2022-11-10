# Rounding in DFP

# Table of Contents

* [All rounding methods overview](#All rounding methods overview)
    - [RoundXXX methods](#RoundXXX methods)
    - [Commonly used roundings](#Commonly used roundings)
    - [New optimized rounding methods](#New optimized rounding methods)
* [Preferable methods](#Preferable methods)
* [Why to replace `roundXXX(multiple)` with `roundToReciprocal`](#WhyToReplaceRoundXXXMultipleWithRoundToReciprocal)
    - [Precision loss on `roundTowardsPositiveInfinity(multiple)`](#PrecisionLossOnRoundTowardsPositiveInfinityMultiple)
    - [Precision loss on `roundTowardsNegativeInfinity(multiple)`](#PrecisionLossOnRoundTowardsNegativeInfinityMultiple)
    - [Precision loss on `roundToNearestTiesAwayFromZero(multiple)`](#PrecisionLossOnRoundToNearestTiesAwayFromZeroMultiple)
    - [Precision loss on `roundToNearestTiesToEven(multiple)`](#PrecisionLossOnRoundToNearestTiesToEvenMultiple)
* [How to replace `roundXXX(multiple)` with `roundToReciprocal`](#HowToReplaceRoundXXXMultipleWithRoundToReciprocal)
* [Benchmarks](#Benchmarks)

## All rounding methods overview

There are many rounding methods in DFP.

> Many methods are deprecated by various reason.

All rounding methods can be separated to several groups:

* [RoundXXX methods](#RoundXXX methods)
* [Commonly used roundings](#Commonly used roundings)
* [New optimized rounding methods](#New optimized rounding methods)

### RoundXXX methods

| Rounding method                                                                       | Description                                                                                                                                                                                                                                                                |
|---------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `roundTowardsPositiveInfinity()`[Deprecated](#Deprecated methods)                     | Alias to `ceil()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as deprecated because the `ceiling()` method name was selected as preferable.                                              |
| `roundTowardsNegativeInfinity()`[Deprecated](#Deprecated methods)                     | Alias to `floor()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value. <br> Method was marked as deprecated because the `floor()` method name was selected as preferable.                                           |
| `roundTowardsZero()`[Deprecated](#Deprecated methods)                                 | Alias to `truncate()`. <br> Rounds value toward zero, returning the nearest integral value that is not larger in magnitude than original value. <br> Method was marked as deprecated because rare usage.                                                                   |
| `roundToNearestTiesAwayFromZero()`[Deprecated](#Deprecated methods)                   | Alias to `round()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero. <br> Method was marked as deprecated because the `round()` method name was selected as preferable.                                                |
| `roundToNearestTiesToEven()`[Deprecated](#Deprecated methods)                         | Call original `bid64_round_integral_nearest_even()`. <br> Returns the nearest `DFP` value that is equal to a mathematical integer, with ties rounding to even. <br> Method was marked as deprecated because `round(0, RoundingMode.HALF_EVEN)` was selected as preferable. |
| `roundTowardsPositiveInfinity(Decimal64 multiple)`[Deprecated](#Deprecated methods)   | Syntactic sugar to `multiply(ceiling(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.CEILING)` provides better performance and precision.                                                               |
| `roundTowardsNegativeInfinity(Decimal64 multiple)`[Deprecated](#Deprecated methods)   | Syntactic sugar to `multiply(floor(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.FLOOR)` provides better performance and precision.                                                                   |
| `roundToNearestTiesAwayFromZero(Decimal64 multiple)`[Deprecated](#Deprecated methods) | Alias to `round(Decimal64 multiple)`.                                                                                                                                                                                                                                      |
| `roundToNearestTiesToEven(Decimal64 multiple)`[Deprecated](#Deprecated methods)       | Syntactic sugar to `multiply(roundToNearestTiesToEven(divide(value, multiple)), multiple);`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.HALF_EVEN)` provides better performance and precision.                                           |

### Commonly used roundings

| Rounding method                                              | Description                                                                                                                                                                                                                                                 |
|--------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ceil()`[Deprecated](#Deprecated methods)                    | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as deprecated because the `ceiling()` method name was selected as preferable. |
| `ceiling()`                                                  | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as preferable by Vitali? according to C# naming?                              |
| `floor()`                                                    | Call original `bid64_round_integral_negative()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value. <br>                                                                                             |
| `truncate()`[Deprecated](#Deprecated methods)                | Call original `bid64_round_integral_zero()`. <br> Rounds value toward zero, returning the nearest integral value that is not larger in magnitude than original value. <br> Method was marked as deprecated because rare usage.                              |
| `round()`                                                    | Call original `bid64_round_integral_nearest_away()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero.                                                                                                   |
| `round(Decimal64 multiple)`[Deprecated](#Deprecated methods) | Syntactic sugar to `multiply(round(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.HALF_UP)` provides better performance and precision.                                                  |

### New optimized rounding methods

| Rounding method                                    | Description                                                                                                                                                                                                                                                                                                         |
|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `round(int n, RoundingMode roundType)`             | Returns the `DFP` value that is rounded according the selected rounding type.                                                                                                                                                                                                                                       |
| `roundToReciprocal(int r, RoundingMode roundType)` | Returns the `DFP` value that is rounded to the value, reciprocal to r, according the selected rounding type. <br> This method provides better performance and precision than the rounding methods with `multiple` argument. But, of course, this method can be used only if the `1/multiple` is a positive integer. |

Test is the value properly rounded according to specific precision can be performed with next methods.

| Test rounding method           | Description                                                                                                                                                                             |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `isRounded(int n)`             | Returns the sign of the value is rounded to the 10<sup>-n</sup>. This method provides better performance than the `round(n, RoundingMode.ROUND_UNNECESSARY)` call.                      |
| `isRoundedToReciprocal(int r)` | Returns the sign of the value is rounded to the value, reciprocal to r. This method provides better performance than the `roundedToReciprocal(r, RoundingMode.ROUND_UNNECESSARY)` call. |

## Preferable methods

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
| value     | 0.08 or `3566850904877432840L`                     |
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
| value     | 0.5 or `3575858104132173829L`                     |
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
| value     | 0.00004 or `3539829307113209860L`                    |
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
| value     | 0.00144 or `3539829307113210000L`                   |
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

## Benchmarks
