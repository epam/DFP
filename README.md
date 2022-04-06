# Decimal Floating Point Arithmetic for Java/.NET

"...it is a bad idea to use floating point to try to represent exact quantities like monetary amounts. Using floating point for dollars-and-cents calculations is a recipe for disaster. Floating point numbers are best reserved for values such as measurements, whose values are fundamentally inexact to begin with." -- [Brian Goetz](https://www.ibm.com/developerworks/library/j-jtp0114/index.html)

Java lacks built-in type to represent Money or Quantity properties frequently used in financial domain.

Ideal data type for this purpose:

* Use base-10 (rather than base-2) to accurately represent monetary values
* Support wide range of values (ranging from hundred of billions of that represent portfolio values to fractions of 6 to 8 below decimal point to represent smallest tick sizes)
* Allow GC-free arithmetic (Garbage Collection pauses are evil in low-latency systems). This most likely implies using *primitive* data types.
* fast (as fast as non-builtin numeric data type could be)
* Support efficient conversion to String and double


DFP uses Java long to represent base-10 floating point numbers. DFP is based on [IEEE 754-2008 standard](https://en.wikipedia.org/wiki/IEEE_754).

# How to use

Add dependency (Gradle):
```
implementation 'com.epam.deltix:dfp:0.11.26'
```
Use:
```
import com.epam.deltix.dfp.Decimal64Utils;

@Decimal long price = Decimal64Utils.parse ("123.45");
@Decimal long halfPrice = Decimal64Utils.divideByInteger (price, 2);
```


## Description/Usage

* [Quick Start Guide](docs/quickstart.md)
* [Tips and Trick](docs/TipsNTricks.md)
* [FAQ](docs/FAQ.md)
* [How to build this project](docs/build.md)

## What is under the hood?

DFP relies on [Intel Decimal Floating-Point Math Library](https://software.intel.com/content/www/us/en/develop/articles/intel-decimal-floating-point-math-library.html) that is written in C and provides implementation for IEEE 754-2008. Some operations are re-written in Java to avoid JNI calls.

## Supported platforms

* x86 (Windows, Linux, Mac)
* arm64 (Linux, Mac)
* Arm7 (Linux)

## Credits

This project was developed by [Deltix](https://www.deltixlab.com) developers **Vitali Haravy**, **Boris Chuprin**, **Andrei Davydov**.

This software uses Intel Decimal Floating Point Math Library.

## License
This library is released under Apache 2.0 license. See ([license](LICENSE))
