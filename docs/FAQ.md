# Frequently Asked Questions

## Why not use `decimal` C# data data type ?
Decimal in C# is just not that good.
* It is uses 12 bytes for mantissa (effectively 16)
* It does not have CLR support. i.e. all operators will be working as functions (slower than DFP).TODO: Benchmark proof.

## What are the *Checked functions in Java?
The *Checked functions in Java are not intended to be used directly.
These special functions are required just for ValueTypeAgent support.
