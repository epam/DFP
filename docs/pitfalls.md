# Common pitfalls when using Decimal64

Frequent problems with @Decimal observed in our codebases:

### Lack of @Decimal annotation
Make sure you annotate all decimal fields/parameters/variables with ``@Decimal`` annotation to explain your intent. Also, at some point we plan to introduce automatic type safety checker that will rely on these annotation.

### Mixing constants:

```java
@Decimal long actualPositionSize = 0; // BUG

private boolean isFlat() {
	return (actualPositionSize == 0); // BUG
}
```
We recommend using Decimal64Util.fromLong(123) or predefined constants like Decimal64Utils.ZERO.


### Infix operators and similar

Examples that probably won't work as intended (assuming all variables are @Decimal):

* `` remainingQuantity = childQuantity/100; ``
* `` if (remainingQuantity >= childQuantity) {} ``
* `` remainingQuantity -= childQuantity; ``


### Using of `java.lang.Math`` functions:

Accidental (or not) usage of Math library function to operate on @Decimal numbers. This will result in error in almost all cases. Example:

```java
@Decimal long childQuantity = Decimal64Utils.subtract(Math.min(remainingQuantity, displayQuantity), quantityOnTheMarket); // BUG
```
 
### Conversion from ``double``

Many Java APIs use `double` for market data prices and sizes. Problem is - when you convert double to decimal64 rounding errors are likely. For example, double value 99.085 will be converted as 99.08499999999999. You need to round results. 
 For example, to convert price of some instrument use tick size:

```java 
  @Decimal rawPrice = Decimal64Utils.fromDouble(99.085); // 99.08499999999999
  @Decimal long tickSize = instrument.getTickSize(); // e.g. 0.005 
  Decimal64Utils.round(rawPrice, tickSize); // produces 99.085
 ```
 
