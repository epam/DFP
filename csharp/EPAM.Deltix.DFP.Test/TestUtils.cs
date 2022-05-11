using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace EPAM.Deltix.DFP.Test
{
	public class TestUtils
	{
		static String ComposeMsg(Object a, String b)
		{
			String aStr;
			return a != null && !(aStr = a.ToString()).Equals("") ? aStr + ", " + Char.ToLower(b[0]) + b.Substring(1) : b;
		}
		static void Fail(ulong expected, ulong actual, String message)
		{
			Assert.Fail($"{message} expected: {DotNetImpl.ToDebugString(expected)}, actual: {DotNetImpl.ToDebugString(actual)}");
		}

		static void Fail(Decimal64 expected, Decimal64 actual, String message)
		{
			Fail(expected.Bits, actual.Bits, message);
		}

		public static void AssertDecimalIdentical(ulong expected, ulong actual, String message = null)
		{
			if (expected != actual)
				Fail(expected, actual, ComposeMsg(message, "Values are different:"));
		}

		public static void AssertDecimalNotIdentical(Decimal64 expected, Decimal64 actual, String message = null)
		{
			if (expected.Bits == actual.Bits)
				Fail(expected, actual, ComposeMsg(message, "Values are identical:"));
		}

		public static void AssertDecimalEqual(Decimal64 expected, Decimal64 actual, String message = null)
		{
			if (!expected.IsEqual(actual))
				Fail(expected, actual, ComposeMsg(message, "Values are not equal:"));
		}

		public static String PrintFp(Object a) => a.ToString().Replace(',', '.');

		public static void AssertEqualToEmbeddedDecimal(Object a, Object b) => Assert.AreEqual(PrintFp(a), PrintFp(b));
		public static void AssertNotEquaTolEmbeddedDecimal(Object a, Object b) => Assert.AreNotEqual(PrintFp(a), PrintFp(b));
		public static void AssertDecimalIdentical(ulong actual, Decimal64 expected, String message = null) => AssertDecimalIdentical(actual, expected.Bits, message);
		public static void AssertDecimalIdentical(Decimal64 expected, Decimal64 actual, String message = null) => AssertDecimalIdentical(expected.ToUnderlying(), actual.ToUnderlying(), message);
		public static void AssertDecimalIdentical(Decimal64 actual, ulong expected, String message = null) => AssertDecimalIdentical(actual.Bits, expected, message);
		public static void AssertDecimalEqual(Decimal64 actual, ulong expected, String message = null) => AssertDecimalEqual(actual, Decimal64.FromUnderlying(expected), message);
		public static void AssertDecimalEqual(ulong actual, Decimal64 expected, String message = null) => AssertDecimalEqual(Decimal64.FromUnderlying(actual), expected, message);
		public static void AssertDecimalEqual(ulong actual, ulong expected, String message = null) => AssertDecimalEqual(Decimal64.FromUnderlying(actual),
																												Decimal64.FromUnderlying(expected),
																												message);
		public static void AssertDecimalEqualNotIdentical(ulong expected, ulong actual, String message = null)
		{
			AssertDecimalEqual(expected, actual, ComposeMsg(message, "Values should be equal"));
			AssertDecimalNotIdentical(expected, actual, ComposeMsg(message, "Values should be equal but different"));
		}

		public static void AssertDecimalEqualNotIdentical(Decimal64 expected, Decimal64 actual, String message = null)
		{
			AssertDecimalEqualNotIdentical(expected.Bits, actual.Bits, message);
		}

		public static void AssertDecimalEqualHashCode(ulong expected, ulong actual, bool expectEqual, String message = null)
		{
			int hashCodeExpected = expected.GetHashCode();
			int hashCodeActual = actual.GetHashCode();
			if ((hashCodeExpected == hashCodeActual) != expectEqual)
			{
				Fail(expected, actual, ComposeMsg(message,
					$"Hash codes should be {(expectEqual ? "equal " : "different")} (expected: {hashCodeExpected} != actual: {hashCodeActual})")
				);
			}
		}
		public static void AssertDecimalNotIdentical(ulong expected, ulong actual, String message = null)
		{
			if (expected == actual)
				Fail(expected, actual, message);
		}
		public static void AssertDecimalEqualHashCode(Decimal64 expected, Decimal64 actual, bool expectEqual, String message = null)
		{
			AssertDecimalEqualHashCode(expected.Bits, actual.Bits, expectEqual, message);
		}
		public static void MantissaZerosCombinations(Action<long, int> func, int n = 1000)
		{
			for (int zerosLen = 1; zerosLen < 16; ++zerosLen)
			{
				long notZeroPart;

				for (int i = 1; i <= 16 - zerosLen; ++i)
				{
					for (int u = 0; u < n; ++u)
					{
						notZeroPart = GetRandomLong(i);
						long mantissa = notZeroPart * PowersOfTen[zerosLen];
						func(mantissa, zerosLen);
					}
				}
			}
		}

		public static void PartsCombinationsWithoutEndingZeros(Action<long, int> func, int n = 50)
		{

			for (int i = 1; i <= 16; ++i)
			{
				for (int u = 0; u < n; ++u)
				{
					long mantissa = GetRandomLong(i);
					for (int exp = 398 - 0x2FF; exp <= 398; ++exp)
						func(mantissa, exp);
				}
			}
		}
		public static long GetRandomLong(int length)
		{
			long randomNum = PowersOfTen[length - 1] +
							((long)(rng.NextDouble() * (PowersOfTen[length] - PowersOfTen[length - 1]))) + 1;
			if (randomNum % 10 == 0)
				--randomNum;
			return randomNum;
		}

		public static long GetRandomLong()
		{
			long result = rng.Next();
			result <<= 32;
			result |= (long)rng.Next();
			return result * rng.Next(-1, 2);
		}

		public static int GetRandomInt() => rng.Next() * rng.Next(-1, 2);


		public static Decimal64 GetRandomDecimal(long maxMantissa)
		{
			long mantissa = GetRandomLong() % maxMantissa;
			int exp = (GetRandomInt() & 127) - 64;
			return Decimal64.FromFixedPoint(mantissa, exp);
		}

		public static Decimal64 GetRandomDecimal()
		{
			return GetRandomDecimal(1000000000000000L);
		}

		public static long[] PowersOfTen = {
			/*  0 */ 1L,
			/*  1 */ 10L,
			/*  2 */ 100L,
			/*  3 */ 1000L,
			/*  4 */ 10000L,
			/*  5 */ 100000L,
			/*  6 */ 1000000L,
			/*  7 */ 10000000L,
			/*  8 */ 100000000L,
			/*  9 */ 1000000000L,
			/* 10 */ 10000000000L,
			/* 11 */ 100000000000L,
			/* 12 */ 1000000000000L,
			/* 13 */ 10000000000000L,
			/* 14 */ 100000000000000L,
			/* 15 */ 1000000000000000L,
			/* 16 */ 10000000000000000L,
			/* 17 */ 100000000000000000L,
			/* 18 */ 1000000000000000000L
		};

		readonly static Random rng = new Random(55);

		public static void CheckInMultipleThreads(ThreadStart target, int threadsCount = 0)
		{
			if (threadsCount <= 0)
				threadsCount = Environment.ProcessorCount;

			if (threadsCount == 1)
			{
				target();
			}
			else
			{
				Thread[] threads = new Thread[threadsCount];
				Exception lastException = null;

				for (int ti = 0; ti < threads.Length; ++ti)
				{
					threads[ti] = new Thread(() =>
					{
						try
						{
							target();
						}
						catch (Exception e)
						{
							lastException = e;
						}
					});
					threads[ti].Start();
				}

				foreach (var thread in threads)
					thread.Join();

				if (lastException != null)
					throw lastException;
			}
		}

		public static readonly Decimal64[] specialValues = {
			Decimal64.FromDouble(Math.PI),
			Decimal64.FromDouble(-Math.E),
			Decimal64.NaN,
			Decimal64.FromUnderlying(Decimal64.NaN.Bits | 1000000000000000UL),
			Decimal64.NaN.Negate(),
			Decimal64.FromUnderlying(Decimal64.NaN.Bits | 1000000000000000UL).Negate(),
			Decimal64.PositiveInfinity,
			Decimal64.FromUnderlying(Decimal64.PositiveInfinity.Bits | 1000000000000000UL),
			Decimal64.NegativeInfinity,
			Decimal64.FromUnderlying(Decimal64.NegativeInfinity.Bits | 1000000000000000UL),
			Decimal64.Zero,
			Decimal64.FromUnderlying(BidInternal.SPECIAL_ENCODING_MASK64 | 1000000000000000UL),
			Decimal64.FromFixedPoint(0L, -300),
			Decimal64.FromFixedPoint(0L, 300),
			Decimal64.FromFixedPoint(1L, DotNetImpl.MinExponent),
			Decimal64.FromFixedPoint(1L, DotNetImpl.MaxExponent),
			Decimal64.MinValue,
			Decimal64.MaxValue,
			Decimal64.MinPositiveValue,
			Decimal64.MaxNegativeValue,
			Decimal64.FromFixedPoint(1L, 398),
			Decimal64.One,
			Decimal64.FromFixedPoint(10000000000000000L, 16),
			Decimal64.FromLong(10000000000000000L),
			Decimal64.FromUnderlying(Decimal64.One.Bits | 0x7000000000000000UL),
			Decimal64.FromUnderlying(Decimal64.One.Bits | 0x7000000000000000UL).Negate(),
		};

		public static void CheckCase(Decimal64 x, Func<ulong, ulong> refFn, Func<Decimal64, Decimal64> testFn)
		{
			var testRet = testFn(x);
			var refRet = Decimal64.FromUnderlying(refFn(x.Bits));

			if (testRet != refRet)
				throw new Exception($"The function(0x{Convert.ToString((long)x.Bits, 16)}UL) = " +
					$"0x{Convert.ToString((long)refRet.Bits, 16)}UL, but test return 0x{Convert.ToString((long)testRet.Bits, 16)}UL");
		}

		public static void CheckWithCoverage(Func<ulong, ulong> refFn, Func<Decimal64, Decimal64> testFn)
		{
			foreach (var x in specialValues)
				foreach (var y in specialValues)
					CheckCase(x, refFn, testFn);

			CheckInMultipleThreads(() =>
			{
				RandomDecimalsGenerator random = new RandomDecimalsGenerator();
				for (int i = 0; i < NTests; ++i)
					CheckCase(random.Next(), refFn, testFn);
			});
		}

		public static void CheckCase(Decimal64 x, Decimal64 y, Func<ulong, ulong, ulong> refFn, Func<Decimal64, Decimal64, Decimal64> testFn)
		{
			var testRet = testFn(x, y);
			var refRet = Decimal64.FromUnderlying(refFn(x.Bits, y.Bits));

			if (testRet != refRet)
				throw new Exception($"The function(0x{Convert.ToString((long)x.Bits, 16)}UL, 0x{Convert.ToString((long)y.Bits, 16)}UL) = " +
					$"0x{Convert.ToString((long)refRet.Bits, 16)}UL, but test return 0x{Convert.ToString((long)testRet.Bits, 16)}UL");
		}

		public static void CheckWithCoverage(Func<ulong, ulong, ulong> refFn, Func<Decimal64, Decimal64, Decimal64> testFn)
		{
			foreach (var x in specialValues)
				foreach (var y in specialValues)
					CheckCase(x, y, refFn, testFn);

			CheckInMultipleThreads(() =>
			{
				RandomDecimalsGenerator random = new RandomDecimalsGenerator();
				for (int i = 0; i < NTests; ++i)
					CheckCase(random.Next(), random.Next(), refFn, testFn);
			});
		}

		public static void CheckCase(Decimal64 x, Decimal64 y, Decimal64 z, Func<ulong, ulong, ulong, ulong> refFn, Func<Decimal64, Decimal64, Decimal64, Decimal64> testFn)
		{
			var testRet = testFn(x, y, z);
			var refRet = Decimal64.FromUnderlying(refFn(x.Bits, y.Bits, z.Bits));

			if (testRet != refRet)
				throw new Exception($"The function(0x{Convert.ToString((long)x.Bits, 16)}UL, 0x{Convert.ToString((long)y.Bits, 16)}UL, 0x{Convert.ToString((long)z.Bits, 16)}UL) = " +
					$"0x{Convert.ToString((long)refRet.Bits, 16)}UL, but test return 0x{Convert.ToString((long)testRet.Bits, 16)}UL");
		}

		public static void CheckWithCoverage(Func<ulong, ulong, ulong, ulong> refFn, Func<Decimal64, Decimal64, Decimal64, Decimal64> testFn)
		{
			foreach (var x in specialValues)
				foreach (var y in specialValues)
					foreach (var z in specialValues)
						CheckCase(x, y, z, refFn, testFn);

			CheckInMultipleThreads(() =>
			{
				RandomDecimalsGenerator random = new RandomDecimalsGenerator();
				for (int i = 0; i < NTests; ++i)
					CheckCase(random.Next(), random.Next(), random.Next(), refFn, testFn);
			});
		}

		public static void CheckCase(Decimal64 x, Decimal64 y, Decimal64 z, Decimal64 t, Func<ulong, ulong, ulong, ulong, ulong> refFn, Func<Decimal64, Decimal64, Decimal64, Decimal64, Decimal64> testFn)
		{
			var testRet = testFn(x, y, z, t);
			var refRet = Decimal64.FromUnderlying(refFn(x.Bits, y.Bits, z.Bits, t.Bits));

			if (testRet != refRet)
				throw new Exception($"The function(0x{Convert.ToString((long)x.Bits, 16)}UL, 0x{Convert.ToString((long)y.Bits, 16)}UL, 0x{Convert.ToString((long)z.Bits, 16)}UL, 0x{Convert.ToString((long)t.Bits, 16)}UL) = " +
					$"0x{Convert.ToString((long)refRet.Bits, 16)}UL, but test return 0x{Convert.ToString((long)testRet.Bits, 16)}UL");
		}

		public static void CheckWithCoverage(Func<ulong, ulong, ulong, ulong, ulong> refFn, Func<Decimal64, Decimal64, Decimal64, Decimal64, Decimal64> testFn)
		{
			foreach (var x in specialValues)
				foreach (var y in specialValues)
					foreach (var z in specialValues)
						foreach (var t in specialValues)
							CheckCase(x, y, z, t, refFn, testFn);

			CheckInMultipleThreads(() =>
			{
				RandomDecimalsGenerator random = new RandomDecimalsGenerator();
				for (int i = 0; i < NTests; ++i)
					CheckCase(random.Next(), random.Next(), random.Next(), random.Next(), refFn, testFn);
			});
		}

		public static void CheckCase<T>(Decimal64 x, Decimal64 y, Func<Decimal64, T> yConverter, Func<ulong, T, ulong> refFn, Func<Decimal64, T, Decimal64> testFn)
		{
			var yT = yConverter(y);
			var testRet = testFn(x, yT);
			var refRet = Decimal64.FromUnderlying(refFn(x.Bits, yT));

			if (testRet != refRet)
				throw new Exception($"The function(0x{Convert.ToString((long)x.Bits, 16)}UL, {yT}) = " +
					$"0x{Convert.ToString((long)refRet.Bits, 16)}UL, but test return 0x{Convert.ToString((long)testRet.Bits, 16)}UL");
		}

		public static void CheckWithCoverage<T>(Func<Decimal64, T> yConverter, Func<ulong, T, ulong> refFn, Func<Decimal64, T, Decimal64> testFn)
		{
			foreach (var x in specialValues)
				foreach (var y in specialValues)
					CheckCase(x, y, yConverter, refFn, testFn);

			CheckInMultipleThreads(() =>
			{
				RandomDecimalsGenerator random = new RandomDecimalsGenerator();
				for (int i = 0; i < NTests; ++i)
					CheckCase(random.Next(), random.Next(), yConverter, refFn, testFn);
			});
		}

		public const int NTests = 10000000;
	}
}
