# Decimal Floating Point Arithmetic for Java/.NET/C/C++

**DFP** is implementation of IEEE 754-2008 **Decimal64** for Java/.NET/C/C++.

## Why?

> "...it is a bad idea to use floating point to try to represent exact quantities like monetary amounts. Using floating point for dollars-and-cents calculations is a recipe for disaster. Floating point numbers are best reserved for values such as measurements, whose values are fundamentally inexact to begin with." — [Brian Goetz](https://www.ibm.com/developerworks/library/j-jtp0114/index.html)

Java lacks built-in type to represent Money or Quantity properties frequently used in financial domain.

Ideal data type for this purpose:

* Use base-10 (rather than base-2) to accurately represent monetary values
* Support wide range of values (ranging from hundred of billions of that represent portfolio values to fractions of 6 to 8 below decimal point to represent smallest tick sizes)
* Allow GC-free arithmetic (Garbage Collection pauses are evil in low-latency systems). This most likely implies using *primitive* data types.
* fast (as fast as non-builtin numeric data type could be)
* Support efficient conversion to String and double


DFP uses Java `long` primitive type to represent base-10 floating point numbers. DFP is based on [IEEE 754-2008 standard](https://en.wikipedia.org/wiki/IEEE_754) and supports up to 16 significant decimal digits.

## Supported languages
* Java - pure Java implementation (since version 0.12). Supported on all platforms where Java is supported.
* .NET - wrapper over C implementation with some functions re-written in C#. Supported platforms:
  * x86-64 (Windows, Linux, Mac)
  * x86 (Windows, Linux)
  * arm64 (Linux, Mac)
  * arm7 (Linux)
* C/C++ - based on [Intel Decimal Floating Point Math Library](https://www.intel.com/content/www/us/en/developer/articles/tool/intel-decimal-floating-point-math-library.html) with some additional APIs (like string parsing and specialized math functions).
  * Please see [DFP on Conan](https://conan.io/center/recipes/dfp) for supported platforms.


# How to use
## Java
Add dependency (Gradle):
```groovy
implementation 'com.epam.deltix:dfp:1.0.10'
```
Use (allocation free):
```java
import com.epam.deltix.dfp.Decimal64Utils;

@Decimal long price = Decimal64Utils.parse("123.45");
@Decimal long halfPrice = Decimal64Utils.divideByInteger(price, 2);
System.out.println(Decimal64Utils.toString(halfPrice));
System.out.println(Decimal64Utils.toScientificString(halfPrice));
System.out.println(Decimal64Utils.toFloatString(halfPrice));
```

With value type wrapper (allocation on object creation):
```java
import com.epam.deltix.dfp.Decimal64;

Decimal64 price = Decimal64.parse("123.45");
Decimal64 halfPrice = price.divide(Decimal64.fromLong (2));
System.out.println(halfPrice.toString());
System.out.println(halfPrice.toScientificString());
System.out.println(halfPrice.toFloatString());
```


## Description/Usage

* [Quick Start Guide](docs/quickstart.md)
* [Tips and Tricks](docs/TipsNTricks.md)
* [FAQ](docs/FAQ.md)
* [How to build this project](docs/build.md)

## What is under the hood?

DFP was inspired on [Intel Decimal Floating-Point Math Library](https://software.intel.com/content/www/us/en/develop/articles/intel-decimal-floating-point-math-library.html) that is written in C and provides implementation for IEEE 754-2008. Early DFP versions used JNI wrappers for this Intel library. Starting from the release 0.12 DFP for Java does not depend on native code.

## Credits

This project was developed by [Deltix](https://www.deltixlab.com) developers **Vitali Haravy**, **Boris Chuprin**, **Andrei Davydov**.

This software uses Intel Decimal Floating Point Math Library.

## License
This library is released under Apache 2.0 license. See ([license](LICENSE))
