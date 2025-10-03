# Frequently Asked Questions

## Why not use `decimal` C# data type ?
Decimal in C# is just not that good:
* It is uses 12 bytes for mantissa (effectively 16)
* It does not have CLR support. i.e. all operators will be working as functions (slower than DFP).TODO: Benchmark proof.

## What is the `Decimal64Utils.NULL`?
In Java, all custom data types can only be represented by classes, which is very expensive.
This approach requires a significant amount of memory and creates considerable pressure on the garbage collector.
However, you can avoid this by working with decimal values packed into Java's `long` primitives.

Replacing the `Decimal64` class with `long` values seems like a great idea until you encounter the problem of handling `null` references. How can `null` be represented using a `long`?

`Decimal64` addresses this issue by introducing a special value: `Decimal64Utils.NULL`.
According to the IEEE 754-2008 standard, NaN (Not a Number) values can be encoded in various ways.
`Decimal64Utils.NULL` is one specific binary value from the possible NaN binary representations that was chosen as `null` value in DFP for Java.

Since this value is technically considered `NaN` according to the standard, the method `Decimal64Utils.isNaN(Decimal64Utils.NULL)` returns `true`.
You should also keep this in mind if you intend to use the ValueTypeAgent.

Key points:
* There is no `null` value defined in the IEEE 754-2008 standard.
* `Decimal64Utils.NULL` is simply one specific binary representation of the possible `NaN` values.
* The method `Decimal64Utils.isNaN(Decimal64Utils.NULL)` returns `true`.

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

# Do we have a method of parsing value like "1.703E-5"?

All the parse() and tryParse() methods supports this scientific notation.
Use the method you like best

```
Decimal64Utils.parse() or Decimal64Utils.tryParse()
Decimal64.parse() or Decimal64.tryParse()
```


