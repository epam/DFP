# Rounding in DFP

## All rounding methods overview

There are many rounding methods in DFP.
These methods can be separated to several groups:
Note: method marked with asterisk(*) are deprecated.

* RoundX methods:

| Rounding method                                       | Description                                                                                                                                                                                                                                                                |
|-------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `roundTowardsPositiveInfinity()*`                     | Alias to `ceil()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as deprecated because the `ceiling()` method name was selected as preferable.                                              |
| `roundTowardsNegativeInfinity()*`                     | Alias to `floor()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value. <br> Method was marked as deprecated because the `floor()` method name was selected as preferable.                                           |
| `roundTowardsZero()*`                                 | Alias to `truncate()`. <br> Rounds value toward zero, returning the nearest integral value that is not larger in magnitude than original value. <br> Method was marked as deprecated because rare usage.                                                                   |
| `roundToNearestTiesAwayFromZero()*`                   | Alias to `round()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero. <br> Method was marked as deprecated because the `round()` method name was selected as preferable.                                                |
| `roundToNearestTiesToEven()*`                         | Call original `bid64_round_integral_nearest_even()`. <br> Returns the nearest `DFP` value that is equal to a mathematical integer, with ties rounding to even. <br> Method was marked as deprecated because `round(0, RoundingMode.HALF_EVEN)` was selected as preferable. |
| `roundTowardsPositiveInfinity(Decimal64 multiple)*`   | Syntactic sugar to `multiply(ceiling(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.CEILING)` provides better performance and precision.                                                               |
| `roundTowardsNegativeInfinity(Decimal64 multiple)*`   | Syntactic sugar to `multiply(floor(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.FLOOR)` provides better performance and precision.                                                                   |
| `roundToNearestTiesAwayFromZero(Decimal64 multiple)*` | Alias to `round(Decimal64 multiple)`.                                                                                                                                                                                                                                      |
| `roundToNearestTiesToEven(Decimal64 multiple)*`       | Syntactic sugar to `multiply(roundToNearestTiesToEven(divide(value, multiple)), multiple);`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.HALF_EVEN)` provides better performance and precision.                                           |

* Widely used namings:

| Rounding method              | Description                                                                                                                                                                                                                                                 |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ceil()*`                    | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as deprecated because the `ceiling()` method name was selected as preferable. |
| `ceiling()`                  | Call original `bid64_round_integral_positive()`. <br> Rounds value upward, returning the smallest integral value that is not less than original value. <br> Method was marked as preferable by Vitali? according to C# naming?                              |
| `floor()`                    | Call original `bid64_round_integral_negative()`. <br> Rounds value downward, returning the largest integral value that is not greater than original value. <br>                                                                                             |
| `truncate()*`                | Call original `bid64_round_integral_zero()`. <br> Rounds value toward zero, returning the nearest integral value that is not larger in magnitude than original value. <br> Method was marked as deprecated because rare usage.                              |
| `round()`                    | Call original `bid64_round_integral_nearest_away()`. <br> Returns the value that is nearest to original value, with halfway cases rounded away from zero.                                                                                                   |
| `round(Decimal64 multiple)*` | Syntactic sugar to `multiply(round(divide(value, multiple)), multiple)`. <br> Was marked as deprecated because `roundToReciprocal(int r, RoundingMode.HALF_UP)` provides better performance and precision.                                                  |

* New optimized rounding methods:

| Rounding method                                    | Description                                                                                                                                                                                                                                                                                                         |
|----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `round(int n, RoundingMode roundType)`             | Returns the `DFP` value that is rounded according the selected rounding type.                                                                                                                                                                                                                                       |
| `roundToReciprocal(int r, RoundingMode roundType)` | Returns the `DFP` value that is rounded to the value, reciprocal to r, according the selected rounding type. <br> This method provides better performance and precision than the rounding methods with `multiple` argument. But, of course, this method can be used only if the `1/multiple` is a positive integer. |

* Test is value rounded methods:

| Rounding method                | Description                                                             |
|--------------------------------|-------------------------------------------------------------------------|
| `isRounded(int n)`             | Returns the sign of the value is rounded to the 10<sup>-n</sup>. This   |
| `isRoundedToReciprocal(int r)` | Returns the sign of the value is rounded to the value, reciprocal to r. |


## Preferable methods

## How to replace `roundXXX(multiple)` with `roundToReciprocal()`

## Benchmarks
