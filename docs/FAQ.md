# Frequently Asked Questions

## Why not use `decimal` C# data data type ?
Decimal in C# is just not that good.
* It is uses 12 bytes for mantissa (effectively 16)
* It does not have CLR support. i.e. all operators will be working as functions (slower than DFP).TODO: Benchmark proof.

## What is the `Decimal64Utils.NULL`?
Is Java all the custom data types can be represented only by the classes. This is very expensive.
This requires much amount of memory and make a big pressure on the garbage collector.
But you can avoid this. You can work with decimal values packed to the `long` primitives.
So, the replacing the `Decimal64` class with the `long` values looks as a great idea till you
encounter `null` reference. How to represent `null` with the `long`?
`Decimal64` solves this problem by introducing special `Decimal64Utils.NULL` value.
According to the IEEE 754-2008, the NaN value can be encoded in different ways.
Just one value from the whole NaN combinations was used as `Decimal64Utils.NULL`.
But, since this value is NaN according to the standard, the `Decimal64Utils.isNaN(Decimal64Utils.NULL)` returns `true`.
You should also keep this in mind if you plan to use the ValueTypeAgent.
So, key points:
* There is no `NULL` value in IEEE 754-2008 standard.
* The `NULL` is just a one combination of possible `NaN` values.
* `Decimal64Utils.isNaN(Decimal64Utils.NULL)` returns `true`.

## What are the *Checked functions in Java?
The *Checked functions in Java are not intended to be used directly.
These special functions are required just for ValueTypeAgent support.

## How to quickly check a sign?
There are many `is*` functions in the DFP library for the fast sign check.
The output values of these functions are presented in the next table.

| Function | -Infinity | -1 | 0 | 1 | Infinity | NaN |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| isPositive | false | false | false | **TRUE** | **TRUE** | false |
| isNonNegative | false | false | **TRUE** | **TRUE** | **TRUE** | false |
| isNegative | **TRUE** | **TRUE** | false | false | false | false |
| isNonPositive | **TRUE** | **TRUE** | **TRUE** | false | false | false |
| isInfinity | **TRUE** | false | false | false | **TRUE** | false |
| isPositiveInfinity | false | false | false | false | **TRUE** | false |
| isNegativeInfinity | **TRUE** | false | false | false | false | false |
| isNaN | false | false | false | false | false | **TRUE** |
| isFinite | false | **TRUE** | **TRUE** | **TRUE** | false | false |
| isNonFinite | **TRUE** | false | false | false | **TRUE** | **TRUE** |
| isZero | false | false | **TRUE** | false | false | false |
| isNonZero | **TRUE** | **TRUE** | false | **TRUE** | **TRUE** | **TRUE** |

