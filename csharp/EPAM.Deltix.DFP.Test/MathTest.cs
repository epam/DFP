using MathNet.Numerics;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Text;

using static EPAM.Deltix.DFP.Decimal64;
using static EPAM.Deltix.DFPMath.Decimal64Math;
using static EPAM.Deltix.DFP.Test.TestUtils;

namespace EPAM.Deltix.DFP.Test
{
	[TestFixture]
	class MathTest
	{
		private static readonly int N = 10000;

		private static int GetRandomSeed()
		{
			return 42;
		}

#if NET40
		static readonly double log2 = Math.Log(2.0);

		private static double MathLog2(double x)
		{
			return Math.Log(x) / log2;
		}
#else
		private static double MathLog2(double x)
		{
			return Math.Log2(x);
		}
#endif


		public static double Ulp(double x)
		{
			x = Math.Abs(x);
			return BitConverter.Int64BitsToDouble(1 + BitConverter.DoubleToInt64Bits(x)) - x;
		}

		static double DoubleDiff(double refValue, double testValue)
		{
			return MathLog2(Math.Abs(refValue - testValue) /
				Math.Max(Ulp(refValue), Ulp(testValue)));
		}

		class TestCaseData
		{
			public Random random;
			public double rnd01;
			public double doubleOut;
			public Decimal64 decimalOut;
		}

		delegate void TestCaseDataConsumer(TestCaseData testData);

		static void TestMathPrecision(long N, TestCaseDataConsumer testFn, double maxAcceptableError)
		{
			if (N < 0)
				throw new ArgumentException($"The N(={N}) must be positive.");

			var randomSeed = GetRandomSeed();
			var random = new Random(randomSeed);

			double maxError = Double.NegativeInfinity;
			long maxErrorIteration = -1;
			double maxErrorRnd01 = Double.NaN;
			double maxErrorDoubleOut = Double.NaN;
			double maxErrorDecimalOut = Double.NaN;

			var testData = new TestCaseData();
			testData.random = random;

			for (long i = 0; i < N; ++i)
			{
				double rnd01 = random.NextDouble();

				testData.rnd01 = rnd01;
				testFn(testData);
				double decimalOut = testData.decimalOut.ToDouble();
				double error = DoubleDiff(testData.doubleOut, decimalOut);

				if (maxError < error)
				{
					maxError = error;
					maxErrorIteration = i;
					maxErrorRnd01 = rnd01;
					maxErrorDoubleOut = testData.doubleOut;
					maxErrorDecimalOut = decimalOut;
				}
			}

			if (maxError > maxAcceptableError)
				throw new Exception($"[RandomSeed={randomSeed}][Iteration={maxErrorIteration}][Rnd01={maxErrorRnd01:E17}] The error(={maxError}) between" +
					$" doubleValue(={maxErrorDoubleOut:E17}) and decimalValue(={maxErrorDecimalOut:E17}) is greater than maxAcceptableError(={maxAcceptableError}).");
		}

		[Test]
		public void TestExp()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 200;
				testCaseData.doubleOut = Math.Exp(x);
				testCaseData.decimalOut = Exp(FromDouble(x));
			}, 6);
		}

		[Test]
		public void TestExp2()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 200;
				testCaseData.doubleOut = Math.Pow(2.0, x);
				testCaseData.decimalOut = Exp2(FromDouble(x));
			}, 6);
		}

		[Test]
		public void TestExp10()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 200;
				testCaseData.doubleOut = Math.Pow(10.0, x);
				testCaseData.decimalOut = Exp10(FromDouble(x));
			}, 7);
		}

		[Test]
		public void TestExpm1()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 200;
				testCaseData.doubleOut = Math.Exp(x) - 1;
				testCaseData.decimalOut = Expm1(FromDouble(x));
			}, 18);
		}

		[Test]
		public void TestLog()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+100;
				testCaseData.doubleOut = Math.Log(x);
				testCaseData.decimalOut = Log(FromDouble(x));
			}, 1);
		}

		[Test]
		public void TestLog2()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+100;
				testCaseData.doubleOut = MathLog2(x);
				testCaseData.decimalOut = Log2(FromDouble(x));
			}, 1);
		}

		[Test]
		public void TestLog10()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+100;
				testCaseData.doubleOut = Math.Log10(x);
				testCaseData.decimalOut = Log10(FromDouble(x));
			}, 0);
		}

		[Test]
		public void TestLog1p()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+100;
				testCaseData.doubleOut = Math.Log(1 + x);
				testCaseData.decimalOut = Log1p(FromDouble(x));
			}, 1);
		}

		[Test]
		public void TestPow()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 6;
				var y = (testCaseData.random.NextDouble() - 0.5) * 200;
				testCaseData.doubleOut = Math.Pow(x, y);
				testCaseData.decimalOut = Pow(FromDouble(x), FromDouble(y));
			}, 10);
		}

		[Test]
		public void TestFmod()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 1e+10;
				testCaseData.doubleOut = x % y;
				testCaseData.decimalOut = Fmod(FromDouble(x), FromDouble(y));
			}, 30);
		}

		[Test]
		public void TestHypot()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 1e+10;
				testCaseData.doubleOut = Math.Sqrt(x * x + y * y);
				testCaseData.decimalOut = Hypot(FromDouble(x), FromDouble(y));
			}, 4);
		}

		[Test]
		public void TestSin()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				testCaseData.doubleOut = Math.Sin(x);
				testCaseData.decimalOut = Sin(FromDouble(x));
			}, 54);
		}

		[Test]
		public void TestCos()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				testCaseData.doubleOut = Math.Cos(x);
				testCaseData.decimalOut = Cos(FromDouble(x));
			}, 54);
		}

		[Test]
		public void TestTan()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				testCaseData.doubleOut = Math.Tan(x);
				testCaseData.decimalOut = Tan(FromDouble(x));
			}, 54);
		}

		[Test]
		public void TestAsin()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 2;
				testCaseData.doubleOut = Math.Asin(x);
				testCaseData.decimalOut = Asin(FromDouble(x));
			}, 11);
		}

		[Test]
		public void TestAcos()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 2;
				testCaseData.doubleOut = Math.Acos(x);
				testCaseData.decimalOut = Acos(FromDouble(x));
			}, 25);
		}

		[Test]
		public void TestAtan()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 10;
				testCaseData.doubleOut = Math.Atan(x);
				testCaseData.decimalOut = Atan(FromDouble(x));
			}, 3);
		}

		[Test]
		public void TestAtan2()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+10;
				var y = testCaseData.random.NextDouble() * 1e+10;
				testCaseData.doubleOut = Math.Atan2(x, y);
				testCaseData.decimalOut = Atan2(FromDouble(x), FromDouble(y));
			}, 4);
		}

		[Test]
		public void TestSinh()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 20;
				testCaseData.doubleOut = Math.Sinh(x);
				testCaseData.decimalOut = Sinh(FromDouble(x));
			}, 4);
		}

		[Test]
		public void TestCosh()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 20;
				testCaseData.doubleOut = Math.Cosh(x);
				testCaseData.decimalOut = Cosh(FromDouble(x));
			}, 4);
		}

		[Test]
		public void TestTanh()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 20;
				testCaseData.doubleOut = Math.Tanh(x);
				testCaseData.decimalOut = Tanh(FromDouble(x));
			}, 3);
		}

		[Test]
		public void TestAsinh()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				testCaseData.doubleOut = Math.Log(x + Math.Sqrt(x * x + 1));
				testCaseData.decimalOut = Asinh(FromDouble(x));
			}, 49);
		}

		[Test]
		public void TestAcosh()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+10 + 1;
				testCaseData.doubleOut = Math.Log(x + Math.Sqrt(x * x - 1));
				testCaseData.decimalOut = Acosh(FromDouble(x));
			}, 2);
		}

		[Test]
		public void TestAtanh()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 2;
				testCaseData.doubleOut = 0.5 * Math.Log((1 + x) / (1 - x));
				testCaseData.decimalOut = Atanh(FromDouble(x));
			}, 25);
		}

		[Test]
		public void TestErf()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 100;
				testCaseData.doubleOut = SpecialFunctions.Erf(x);
				testCaseData.decimalOut = Erf(FromDouble(x));
			}, 5);
		}

		[Test]
		public void TestErfc()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 100;
				testCaseData.doubleOut = SpecialFunctions.Erfc(x);
				testCaseData.decimalOut = Erfc(FromDouble(x));
			}, 12);
		}

		[Test]
		public void TestTgamma()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 10;
				testCaseData.doubleOut = SpecialFunctions.Gamma(x);
				testCaseData.decimalOut = Tgamma(FromDouble(x));
			}, 27);
		}

		[Test]
		public void TestLgamma()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 10;
				testCaseData.doubleOut = SpecialFunctions.GammaLn(x);
				testCaseData.decimalOut = Lgamma(FromDouble(x));
			}, 26);
		}

		[Test]
		public void TestSqrt()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+10;
				testCaseData.doubleOut = Math.Sqrt(x);
				testCaseData.decimalOut = Sqrt(FromDouble(x));
			}, 3);
		}

		[Test]
		public void TestCbrt()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = testCaseData.rnd01 * 1e+10;
				testCaseData.doubleOut = Math.Pow(x, 1.0 / 3);
				testCaseData.decimalOut = Cbrt(FromDouble(x));
			}, 4);
		}

		[Test]
		public void TestAdd()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 10;
				testCaseData.doubleOut = x + y;
				testCaseData.decimalOut = FromDouble(x) + FromDouble(y);
			}, 26);
		}

		[Test]
		public void TestSub()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 10;
				testCaseData.doubleOut = x - y;
				testCaseData.decimalOut = FromDouble(x) - FromDouble(y);
			}, 26);
		}

		[Test]
		public void TestMul()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 1e+10;
				testCaseData.doubleOut = x * y;
				testCaseData.decimalOut = FromDouble(x) * FromDouble(y);
			}, 4);
		}

		[Test]
		public void TestDiv()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 1e+10;
				testCaseData.doubleOut = x / y;
				testCaseData.decimalOut = FromDouble(x) / FromDouble(y);
			}, 4);
		}

		[Test]
		public void TestFma()
		{
			TestMathPrecision(N, testCaseData =>
			{
				var x = (testCaseData.rnd01 - 0.5) * 1e+10;
				var y = (testCaseData.random.NextDouble() - 0.5) * 1e+10;
				var z = (testCaseData.random.NextDouble() - 0.5) * 1e+10;
				testCaseData.doubleOut = x * y + z;
				testCaseData.decimalOut = Fma(FromDouble(x), FromDouble(y), FromDouble(z));
			}, 4);
		}

		[Test]
		public void TestQuietGreaterUnordered()
		{
			var a = FromLong(5);
			var b = FromLong(-10);
			var n = NaN;

			Assert.AreEqual(a > b, a.IsGreaterUnordered(b));
			Assert.AreEqual(b > a, b.IsGreaterUnordered(a));
			Assert.True(a.IsGreaterUnordered(n));
			Assert.True(n.IsGreaterUnordered(a));
			Assert.True(b.IsGreaterUnordered(n));
			Assert.True(n.IsGreaterUnordered(b));
		}

		[Test]
		public void TestQuietLessUnordered()
		{
			var a = FromLong(5);
			var b = FromLong(-10);
			var n = NaN;

			Assert.AreEqual(a < b, a.IsLessUnordered(b));
			Assert.AreEqual(b < a, b.IsLessUnordered(a));
			Assert.True(a.IsLessUnordered(n));
			Assert.True(n.IsLessUnordered(a));
			Assert.True(b.IsLessUnordered(n));
			Assert.True(n.IsLessUnordered(b));
		}

		[Test]
		public void TestNotGreater()
		{
			var a = FromLong(5);
			var b = FromLong(-10);
			var n = NaN;

			Assert.AreEqual(a <= b, a.IsNotGreater(b));
			Assert.AreEqual(b <= a, b.IsNotGreater(a));
			Assert.True(a.IsNotGreater(n));
			Assert.True(n.IsNotGreater(a));
			Assert.True(b.IsNotGreater(n));
			Assert.True(n.IsNotGreater(b));
		}

		[Test]
		public void TestNotLess()
		{
			var a = FromLong(5);
			var b = FromLong(-10);
			var n = NaN;

			Assert.AreEqual(a.IsGreaterOrEqual(b), a.IsNotLess(b));
			Assert.AreEqual(b.IsGreaterOrEqual(a), b.IsNotLess(a));
			Assert.True(a.IsNotLess(n));
			Assert.True(n.IsNotLess(a));
			Assert.True(b.IsNotLess(n));
			Assert.True(n.IsNotLess(b));
		}

		[Test]
		public void TestOrdered()
		{
			var a = FromLong(5);
			var b = FromLong(-10);
			var n = NaN;

			Assert.True(a.IsOrdered(b));
			Assert.False(a.IsOrdered(n));
			Assert.False(n.IsOrdered(a));
		}

		[Test]
		public void TestUnordered()
		{
			var a = FromLong(5);
			var b = FromLong(-10);
			var n = NaN;

			Assert.False(a.IsUnordered(b));
			Assert.True(a.IsUnordered(n));
			Assert.True(n.IsUnordered(a));
		}

		private static readonly String[] roundInput = {
	"0.1", "0.5", "0.9", // 0
        "1.1", "1.5", "1.9", // 1
        "2.1", "2.5", "2.9", // 2
        "3.1", "3.5", "3.9", // 3
        "-0.1", "-0.5", "-0.9", // -0
        "-1.1", "-1.5", "-1.9", // -1
        "-2.1", "-2.5", "-2.9", // -2
        "-3.1", "-3.5", "-3.9"}; // -2

		private delegate Decimal64 RoundFunc(Decimal64 x);

		private static void TestRoundFunc(String[] refRound, RoundFunc roundFunc)
		{
			Assert.AreEqual(roundInput.Length, refRound.Length);

			for (int i = 0; i < roundInput.Length; ++i)
			{
				var roundedValue = roundFunc(Parse(roundInput[i]));
				if (!Equals(roundedValue, Parse(refRound[i])))
					throw new Exception("The " + roundInput[i] + " rounded to " +
						roundedValue + " instead of " + refRound[i]);
			}
		}

		[Test]
		public void TestRoundIntegralExact()
		{
			TestRoundFunc(new String[]{
			"0", "0", "1", // 0
            "1", "2", "2", // 1
            "2", "2", "3", // 2
            "3", "4", "4", // 3
            "0", "0", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-2", "-3", // -2
            "-3", "-4", "-4" // -3
        }, aLong => RoundIntegralExact(aLong));
		}

		[Test]
		public void TestIntegralNearestEven()
		{
			TestRoundFunc(new String[]{
			"0", "0", "1", // 0
            "1", "2", "2", // 1
            "2", "2", "3", // 2
            "3", "4", "4", // 3
            "0", "0", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-2", "-3", // -2
            "-3", "-4", "-4" // -3
        }, aLong => RoundIntegralNearestEven(aLong));
		}

		[Test]
		public void TestIntegralNegative()
		{
			TestRoundFunc(new String[]{
			"0", "0", "0", // 0
            "1", "1", "1", // 1
            "2", "2", "2", // 2
            "3", "3", "3", // 3
            "-1", "-1", "-1", // -0
            "-2", "-2", "-2", // -1
            "-3", "-3", "-3", // -2
            "-4", "-4", "-4" // -3
        }, aLong => RoundIntegralNegative(aLong));
		}

		[Test]
		public void TestIntegralPositive()
		{
			TestRoundFunc(new String[]{
			"1", "1", "1", // 0
            "2", "2", "2", // 1
            "3", "3", "3", // 2
            "4", "4", "4", // 3
            "0", "0", "0", // -0
            "-1", "-1", "-1", // -1
            "-2", "-2", "-2", // -2
            "-3", "-3", "-3" // -3
        }, aLong => RoundIntegralPositive(aLong));
		}

		[Test]
		public void TestIntegralZero()
		{
			TestRoundFunc(new String[]{
			"0", "0", "0", // 0
            "1", "1", "1", // 1
            "2", "2", "2", // 2
            "3", "3", "3", // 3
            "0", "0", "0", // -0
            "-1", "-1", "-1", // -1
            "-2", "-2", "-2", // -2
            "-3", "-3", "-3" // -3
        }, aLong => RoundIntegralZero(aLong));
		}

		[Test]
		public void TestIntegralNearestAway()
		{
			TestRoundFunc(new String[]{
			"0", "1", "1", // 0
            "1", "2", "2", // 1
            "2", "3", "3", // 2
            "3", "4", "4", // 3
            "0", "-1", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-3", "-3", // -2
            "-3", "-4", "-4" // -3
        }, aLong => RoundIntegralNearestAway(aLong));
		}

		[Test]
		public void TestNextAfter()
		{
			var x = Scalbn(FromInt32(314), -2);
			AssertDecimalEqual(NextUp(x), NextAfter(x, Million));
			AssertDecimalEqual(NextDown(x), NextAfter(x, -Million));
		}

		[Test]
		public void TestMinNum()
		{
			var a = FromInt32(5);
			var b = FromInt32(-10);
			AssertDecimalEqual(b, MinNum(a, b));
		}

		[Test]
		public void TestMinNumMag()
		{
			var a = FromInt32(5);
			var b = FromInt32(-10);
			AssertDecimalEqual(a, MinNumMag(a, b));
		}

		[Test]
		public void TestMaxNum()
		{
			var a = FromInt32(5);
			var b = FromInt32(-10);
			AssertDecimalEqual(a, MaxNum(a, b));
		}

		[Test]
		public void TestMaxNumMag()
		{
			var a = FromInt32(5);
			var b = FromInt32(-10);
			AssertDecimalEqual(b, MaxNumMag(a, b));
		}

		[Test]
		public void TestFromInt32()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var rv = random.Next(int.MinValue, int.MaxValue);
				AssertDecimalEqual(Parse(rv.ToString()), FromInt32(rv), "RandomValue=" + rv);
			}
		}

		[Test]
		public void TestFromUInt32()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var rv = (uint)random.Next();
				AssertDecimalEqual(Parse(rv.ToString()), FromUInt32(rv), "RandomValue=" + rv);
			}
		}

		[Test]
		public void TestFromInt64()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var rv = (long)((random.NextDouble() - 0.5) * 2 * 1000000000000000L);
				AssertDecimalEqual(Parse(rv.ToString()), FromInt64(rv), "RandomValue=" + rv);
			}
		}

		[Test]
		public void TestFromUInt64()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var rv = (ulong)(random.NextDouble() * 1000000000000000L);
				AssertDecimalEqual(Parse(rv.ToString()), FromUInt64(rv), "RandomValue=" + rv);
			}
		}

		[Test]
		[Obsolete]
		public void TestIsSigned()
		{
			var random = new Random(GetRandomSeed());
			for (int i = -100; i < 100; ++i)
			{
				var rv = random.Next(int.MinValue, int.MaxValue);
				Assert.AreEqual(FromInt32(rv).IsSigned(), rv < 0);
			}
		}

		[Test]
		public void TestIsNormal()
		{
			Assert.True(Scalbn(FromInt32(314), -2).IsNormal());
			Assert.True(Scalbn(FromInt32(-314), -2).IsNormal());

			Assert.False(Zero.IsNormal());

			Assert.False(NaN.IsNormal());
			Assert.False(PositiveInfinity.IsNormal());
			Assert.False(NegativeInfinity.IsNormal());
		}

		[Test]
		public void TestIsSubnormal()
		{
			Assert.False(Scalbn(FromInt32(314), -2).IsSubnormal());
			Assert.False(Scalbn(FromInt32(-314), -2).IsSubnormal());

			Assert.False(Zero.IsSubnormal());

			Assert.False(NaN.IsSubnormal());
			Assert.False(PositiveInfinity.IsSubnormal());
			Assert.False(NegativeInfinity.IsSubnormal());
		}

		[Test]
		public void TestIsInf()
		{
			Assert.False(Scalbn(FromInt32(314), -2).IsInfinity());
			Assert.False(Scalbn(FromInt32(-314), -2).IsInfinity());

			Assert.False(Zero.IsInfinity());

			Assert.False(NaN.IsInfinity());
			Assert.True(PositiveInfinity.IsInfinity());
			Assert.True(NegativeInfinity.IsInfinity());
		}

		[Test]
		public void TestIsCanonical()
		{
			Assert.True(Scalbn(FromInt32(314), -2).IsCanonical());
			Assert.True(Scalbn(FromInt32(-314), -2).IsCanonical());

			Assert.True(Zero.IsCanonical());

			Assert.True(NaN.IsCanonical());
			Assert.True(PositiveInfinity.IsCanonical());
			Assert.True(NegativeInfinity.IsCanonical());
		}

		[Test]
		public void TestCopySign()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var x = random.Next(int.MinValue, int.MaxValue);
				var y = random.Next(int.MinValue, int.MaxValue);
				var z = y >= 0 ? Math.Abs(x) : -Math.Abs(x);
				AssertDecimalEqual(FromInt64(z),
					CopySign(FromInt64(x), FromInt64(y)),
					"x=" + x + "; y=" + y + "; z=" + z);
			}
		}

		[Test]
		public void TestClassOfValue()
		{
			Assert.AreEqual(ClassOfValue(Scalbn(FromInt32(314), -2)), 8);
			Assert.AreEqual(ClassOfValue(Scalbn(FromInt32(-314), -2)), 3);

			Assert.AreEqual(ClassOfValue(Zero), 6);

			Assert.AreEqual(ClassOfValue(NaN), 1);
			Assert.AreEqual(ClassOfValue(PositiveInfinity), 9);
			Assert.AreEqual(ClassOfValue(NegativeInfinity), 2);
		}

		[Test]
		public void TestIsSameQuantum()
		{
			Assert.True(IsSameQuantum(FromInt32(10), FromInt32(100)));
			Assert.False(IsSameQuantum(FromInt32(10), Scalbn(FromInt32(1), 1)));
		}

		[Test]
		public void TestIsTotalOrder()
		{
			var x = FromInt32(5);
			var y = FromInt32(-10);
			Assert.False(IsTotalOrder(x, y));
			Assert.True(IsTotalOrder(y, x));
			Assert.True(IsTotalOrder(x, PositiveInfinity));
			Assert.True(IsTotalOrder(NegativeInfinity, x));
			Assert.True(IsTotalOrder(NegativeInfinity, NaN));
			Assert.True(IsTotalOrder(PositiveInfinity, NaN));
		}

		[Test]
		public void TestIsTotalOrderMag()
		{
			var x = FromInt32(5);
			var y = FromInt32(-10);
			Assert.True(IsTotalOrderMag(x, y));
			Assert.False(IsTotalOrderMag(y, x));
			Assert.True(IsTotalOrderMag(x, PositiveInfinity));
			Assert.False(IsTotalOrderMag(NegativeInfinity, x));
			Assert.True(IsTotalOrderMag(NegativeInfinity, NaN));
			Assert.True(IsTotalOrderMag(PositiveInfinity, NaN));
		}

		[Test]
		public void TestRadix()
		{
			Assert.AreEqual(10, Radix(FromInt32(2)));
		}

		[Test]
		public void TestRem()
		{
			for (int x = -1000; x < 1000; ++x)
				for (int y = -11; y < 11; ++y)
				{
					if (y == 0)
						continue;
					var X = FromInt32(x);
					var Y = FromInt32(y);
					var R = RoundIntegralNearestEven(X / Y);
					var Z = X - Y * R;
					AssertDecimalEqual(Z, Rem(X, Y), "x=" + x + "; y=" + y);
				}
		}

		[Test]
		public void TestIlogb()
		{
			Assert.AreEqual(3, Ilogb(FromInt32(5432)));
			Assert.AreEqual(2, Ilogb(Scalbn(FromInt32(8), 2)));
			Assert.AreEqual(0, Ilogb(FromDouble(Math.PI)));
			Assert.AreEqual(-6, Ilogb(FromDouble(3.34034e-6)));
			Assert.AreEqual(-4, Ilogb(Scalbn(FromInt32(6), -4)));
		}

		[Test]
		public void TestScalbn()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var x = random.Next(int.MinValue, int.MaxValue);
				var y = random.Next(-100, 100);
				AssertDecimalEqual(FromInt32(x) * Exp10(FromInt32(y)), Scalbn(FromInt32(x), y), "x=" + x + "; y=" + y);
			}
		}

		[Test]
		public void TestLdexp()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var x = random.Next(int.MinValue, int.MaxValue);
				var y = random.Next(-100, 100);
				var testValue = Ldexp(FromInt32(x), y);
				AssertDecimalEqual(Scalbn(FromInt32(x), y), testValue, "x=" + x + "; y=" + y);
			}
		}

		[Test]
		public void TestQuantize()
		{
			var x = Scalbn(FromInt32(1234), 5);
			var y = Scalbn(FromInt32(56789), -2);
			var z = Quantize(x, y);
			AssertDecimalEqual(x, z);
		}

		[Test]
		public void TestToBinary32()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var m = random.Next(-10000000, 10000000);
				var e = random.Next(-30, 30);
				var rv = float.Parse(m + "e" + e);
				var testValue = FromDouble(rv).ToBinary32();
				Assert.AreEqual(testValue, rv);
			}
		}

		[Test]
		public void TestToBinary64()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				long m = (long)((random.NextDouble() - 0.5) * 2 * 1000000000000L);
				var e = random.Next(-200, 200);
				var rv = double.Parse(m + "e" + e);
				var testValue = FromDouble(rv).ToBinary64();
				Assert.AreEqual(testValue, rv, Math.Abs(rv * 1e-12));
			}
		}

		[Test]
		public void TestLogb()
		{
			AssertDecimalEqual(FromInt32(0), Logb(Scalbn(FromInt32(314), -2)));
			AssertDecimalEqual(FromInt32(7), Logb(Scalbn(FromInt32(314), 5)));
			AssertDecimalEqual(FromInt32(-3), Logb(Scalbn(FromInt32(314), -5)));
			AssertDecimalEqual(FromInt32(-3), Logb(Scalbn(FromInt32(-314), -5)));
		}

		[Test]
		public void TestNearByInt()
		{
			TestRoundFunc(new String[]{
			"0", "0", "1", // 0
            "1", "2", "2", // 1
            "2", "2", "3", // 2
            "3", "4", "4", // 3
            "0", "0", "-1", // -0
            "-1", "-2", "-2", // -1
            "-2", "-2", "-3", // -2
            "-3", "-4", "-4" // -3
        }, aLong => NearByInt(aLong));
		}

		[Test]
		public void TestFdim()
		{
			var random = new Random(GetRandomSeed());
			for (int i = 0; i < N; ++i)
			{
				var x = Scalbn(FromInt32(random.Next(int.MinValue, int.MaxValue)), random.Next(-100, 100));
				var y = Scalbn(FromInt32(random.Next(int.MinValue, int.MaxValue)), random.Next(-100, 100));
				AssertDecimalEqual(x > y ? x - y : Zero, Fdim(x, y));
			}
		}

		[Test]
		public void TestQuantExp()
		{
			var x = Scalbn(FromInt32(1234), 5);
			var y = Scalbn(FromInt32(56789), -2);

			Assert.AreEqual(5, QuantExp(x));
			Assert.AreEqual(-2, QuantExp(y));
		}

		[Test]
		public void TestQuantum()
		{
			var x = Scalbn(FromInt32(1234), 5);
			var y = Scalbn(FromInt32(56789), -2);

			AssertDecimalEqual(Scalbn(FromInt32(1), 5), Quantum(x));
			AssertDecimalEqual(Scalbn(FromInt32(1), -2), Quantum(y));
		}
	}
}
