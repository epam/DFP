# Quick Start

Add the following dependency to your Java project (Gradle sample):

```groovy
dependencies {
    compile 'com.epam.deltix:dfp:0.11.23'
}
```

Here is a very short example of DFP usage:

```java
import com.epam.deltix.dfp.Decimal64Utils;

@Decimal long commission = Decimal64Utils.fromFixedPoint(8, 4); // 8 basis points, 0.0008
@Decimal long price = Decimal64Utils.fromDouble (123.45);
@Decimal long adjustedPrice = Decimal64Utils.add (price, Decimal64Utils.multiply (price, commission));
```

This may look a bit bulky, but if you are familiar with similar libraries like Joda Money, or BigDecimal, you will find it reasonable. 

## Key points:

* Everything related to DFP is provided by helper class Decimal64Utils.
* Use 'fromXXX' factory methods to construct DFP number. For example
  * fromDouble
  * fromLong
  * fromFixedPoint
  * parse(String)
* There is a helper method for every possible arithmetic operation.
* Special `@Decimal` annotation should be used to mark long values that represent DFP values.

Another example. This time we illustrate conversion from string and back:

```java
@Decimal long parseQuantity (String quantityText) {
   try {
      @Decimal long result = Decimal64Util.parse(quantityText);
      System.out.println("Processed quantity: " + Decimal64Util.toString (result));
      // for lbraries that have built-in support for DFP:
      // logger.info("Processed quantity %s).withDecimal(result);
   } catch (NumberFormatException e) {
      throw new IllegalArgumentException ("Bad quantity: \" + quantityText + '"');
   }  
   return result; 
}
```

# Decimal vs. lava.lang.double vs. java.lang.BigDecimal

```java
double d1 = 0.3;
double d2 = 0.2;
System.out.println("Double: 0,3 - 0,2 = " + (d1 - d2)); // Prints: Double: 0,3 - 0,2 = 0.09999999999999998 <== PROBLEM!

float f1 = 0.3f;
float f2 = 0.2f;
System.out.println("Float: 0,3 - 0,2 = " + (f1 - f2)); // Prints: Float: 0,3 - 0,2 = 0.10000001 <== PROBLEM!

BigDecimal bd1 = new BigDecimal("0.3");
BigDecimal bd2 = new BigDecimal("0.2");
System.out.println("BigDecimal: 0,3 - 0,2 = " + (bd1.subtract(bd2))); // Prints: BigDecimal: 0,3 - 0,2 = 0.1

@Decimal long b1 = Decimal64Utils.parse("0.3");
@Decimal long b2 = Decimal64Utils.parse("0.2");
@Decimal long b3 = Decimal64Utils.subtract(b1, b2);
System.out.println("Decimal64: 0,3 - 0,2 = " + (Decimal64Utils.toString(b3))); // Prints: Decimal64: 0,3 - 0,2 = 0.1
```
# Decimal64 Value Type

For people who prefer strong type safety and want to avoid mixing `long` values with `@Decimal long` Deltix offers Decimal64 value type. 
General idea is that DFP values are represented by immutable instances of class Decimal64. Special runtime agent converts usages of Decimal64 to primitive `long` using nifty bytecode modification technique. 

Decimal64 gives you both type safe Decimal operations and runtime effectiveness of primitive data type.


With Decimal64 the previous example can be written as:

```java
Decimal64 c1 = Decimal64.parse("0.3");
Decimal64 c2 = Decimal64.parse("0.2");
System.out.println("Decimal64 obj: 0,3 - 0,2 = " + (c1.subtract(c2))); // Prints: Decimal64 obj: 0,3 - 0,2 = 0.1

```
See [ValueTypes4Java](https://github.com/deltixlab/ValueTypes4Java/blob/master/docs/INSTALL.md) for more information.


# NaN, infinity

DFP concept of Not-a-Number (NaN) and infinity is somewhat similar to Java double's (and dictated by IEEE 754 spec).

* You must use `isNaN()` to check for NaN. Never check for NaN using comparison operator `==`.
* Similarly use `isPositiveInfinity()` and `isNegativeInfinity` functions for infinity checks.
* One of NaN values is distinguished as decimal NULL.


# What's next?

* [Tips and Tricks](TipsNTricks.md)
* [Common pitfalls](pitfalls.md)
