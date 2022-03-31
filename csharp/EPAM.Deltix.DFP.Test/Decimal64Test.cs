using System;
using System.Globalization;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using NUnit.Framework;

using static EPAM.Deltix.DFP.Test.TestUtils;

namespace EPAM.Deltix.DFP.Test
{
	[TestFixture]
	class Decimal64Test
	{
		[TestCase()]
		public void IsNullTest()
		{
			Decimal64 a = Decimal64.Null;
			Assert.AreEqual(true, a.IsNull());
			Assert.AreEqual(true, a.IsNaN());

			Decimal64 b = Decimal64.NaN;
			Assert.AreEqual(false, b.IsNull());
			Assert.AreEqual(true, b.IsNaN());
		}

		[TestCase()]
		public void CanonizeTest()
		{
			Decimal64 a = Decimal64.FromFixedPoint(10000, 4);
			Decimal64 b = Decimal64.FromFixedPoint(1, 0);
			Decimal64 c = Decimal64.FromFixedPoint(10, 1);
			Decimal64 d = Decimal64.FromFixedPoint(100, 2);
			Decimal64 e = Decimal64.FromFixedPoint(1000, 3);

			CheckCanonize(a, b);
			CheckCanonize(b, c);
			CheckCanonize(c, d);
			CheckCanonize(d, e);
			CheckCanonize(e, a);

			Decimal64 nan1 = Decimal64.NaN;
			Decimal64 nan2 = Decimal64.FromUnderlying(nan1.Bits + 20);
			// CheckCanonize(nan1, nan2);

			Decimal64 posInf1 = Decimal64.PositiveInfinity;
			Decimal64 posInf2 = Decimal64.FromUnderlying(posInf1.Bits + 10);
			CheckCanonize(posInf1, posInf2);

			Decimal64 negInf1 = Decimal64.PositiveInfinity;
			Decimal64 negInf2 = Decimal64.FromUnderlying(negInf1.Bits + 10);
			CheckCanonize(negInf1, negInf2);

			Decimal64 zero1 = Decimal64.FromFixedPoint(0, 1);
			Decimal64 zero2 = Decimal64.FromFixedPoint(0, 2);
			CheckCanonize(zero1, zero2);
		}

		private void CheckCanonize(Decimal64 value1, Decimal64 value2)
		{
			CheckCanonize(value1.Bits, value2.Bits);
		}

		private void CheckCanonize(ulong value1l, ulong value2l_)
		{
			ulong value2l = value2l_;
			Decimal64 value1 = Decimal64.FromUnderlying(value1l), value2 = Decimal64.FromUnderlying(value2l);
			String msg = "checkCanonize() failed";
			AssertDecimalEqualNotIdentical(value1l, value2l, msg);
			AssertDecimalEqualHashCode(value1l, value2l, false, msg);

			AssertDecimalEqualNotIdentical(value1, value2, msg);
			AssertDecimalEqualHashCode(value1, value2, false, msg);

			Decimal64 value1c = value1.Canonize();
			Decimal64 value2c = value2.Canonize();
			AssertDecimalIdentical(value1c, value2c, msg);

			AssertDecimalIdentical(value1c.Bits, value2c.Bits, msg);
			AssertDecimalEqualHashCode(value1c, value2c, true, msg);

			Assert.AreEqual(value1.ToString(), value2.ToString());
			Assert.IsTrue(value1c.Equals(value2c));
			Assert.IsTrue(value2c.Equals(value1c));
		}

		private void CheckCanonize2(Decimal64 a, Decimal64 b)
		{
			Assert.AreEqual(a.ToString(), b.ToString());
			Decimal64 ac = a.Canonize();
			Decimal64 bc = b.Canonize();
			Assert.AreEqual(a.Canonize(), b.Canonize());
			Assert.AreEqual(ac, bc);
			Assert.AreEqual(a.Canonize().ToString(), b.Canonize().ToString());
			Assert.AreEqual(a.ToString(), b.Canonize().ToString());
			Assert.AreEqual(a.Canonize().ToString(), b.ToString());
			Assert.IsTrue(a.Equals(b));
			Assert.IsTrue(b.Equals(a));
		}

		[Test]
		public void CanonizeZeroTest()
		{
			for (int exp = 398 - 0x2FF; exp <= 398; ++exp)
			{
				Decimal64 zero = Decimal64.FromFixedPoint(0, exp);
				CheckCanonize2(zero, zero.Canonize());
			}
		}

		[Test]
		public void Binary64Conversion()
		{
			Double binary64 = new Random().NextDouble();
			Decimal64 decimal64 = Decimal64.FromDouble(binary64);
			Double result = (Double)decimal64;
			Assert.That(Math.Abs(binary64 - result), Is.LessThanOrEqualTo(1.0E-16));
		}

		[Test]
		public void FromFixedPointFastConsts()
		{
			AssertDecimalEqual(Decimal64.Zero.Bits, DotNetImpl.FromFixedPointFast(0, 0));
			AssertDecimalEqual(Decimal64.One, DotNetImpl.FromFixedPointFast(1, 0));
			AssertDecimalEqual(Decimal64.Two, DotNetImpl.FromFixedPointFast(2, 0));
			AssertDecimalEqual(Decimal64.Ten, DotNetImpl.FromFixedPointFast(10, 0));
			AssertDecimalEqual(Decimal64.Ten, DotNetImpl.FromFixedPointFast(1, -1));
			AssertDecimalEqual(Decimal64.Hundred, DotNetImpl.FromFixedPointFast(100, 0));
			AssertDecimalEqual(Decimal64.Hundred, DotNetImpl.FromFixedPointFast(1, -2));
			AssertDecimalEqual(Decimal64.Thousand, DotNetImpl.FromFixedPointFast(1, -3));
			AssertDecimalEqual(Decimal64.Million, DotNetImpl.FromFixedPointFast(1, -6));
			AssertDecimalEqual(Decimal64.OneTenth, DotNetImpl.FromFixedPointFast(1, 1));
			AssertDecimalEqual(Decimal64.OneHundredth, DotNetImpl.FromFixedPointFast(1, 2));

			AssertDecimalEqual(Decimal64.Zero, DotNetImpl.FromFixedPointFastUnsigned(0, 0));
			AssertDecimalEqual(Decimal64.One, DotNetImpl.FromFixedPointFastUnsigned(1, 0));
			AssertDecimalEqual(Decimal64.Two, DotNetImpl.FromFixedPointFastUnsigned(2, 0));
			AssertDecimalEqual(Decimal64.Ten, DotNetImpl.FromFixedPointFastUnsigned(10, 0));
			AssertDecimalEqual(Decimal64.Ten, DotNetImpl.FromFixedPointFastUnsigned(1, -1));
			AssertDecimalEqual(Decimal64.Hundred, DotNetImpl.FromFixedPointFastUnsigned(100, 0));
			AssertDecimalEqual(Decimal64.Hundred, DotNetImpl.FromFixedPointFastUnsigned(1, -2));
			AssertDecimalEqual(Decimal64.Thousand, DotNetImpl.FromFixedPointFastUnsigned(1, -3));
			AssertDecimalEqual(Decimal64.Million, DotNetImpl.FromFixedPointFastUnsigned(1, -6));
		}

		[Test]
		public void FromFixedPointFast()
		{
			int N = 1000;
			for (int exp = 398 - 0x2FF; exp <= 398; ++exp)
			{
				for (int j = 0; j < N; ++j)
				{
					int mantissa = GetRandomInt();
					long mantissa64 = GetRandomLong();

					ulong correct64 = NativeImpl.fromFixedPoint64(mantissa, exp);

					AssertDecimalEqual(correct64, DotNetImpl.FromFixedPointFast(mantissa, exp));

					AssertDecimalEqual(NativeImpl.fromFixedPoint64((UInt32)mantissa, exp), DotNetImpl.FromFixedPointFastUnsigned((UInt32)mantissa, exp));

					AssertDecimalEqual(correct64, Decimal64.FromFixedPoint(mantissa, exp));
					AssertDecimalEqual(NativeImpl.fromFixedPoint64((UInt32)mantissa, exp), Decimal64.FromFixedPoint((UInt32)mantissa, exp));
					AssertDecimalEqual(correct64, Decimal64.FromFixedPoint((Int64)mantissa, exp));

					if (mantissa >= 0)
					{
						AssertDecimalEqual(correct64, DotNetImpl.FromFixedPointFastUnsigned((UInt32)mantissa, exp));
					}

					for (int k = 0; k < 32; ++k)
					{
						AssertDecimalEqual(NativeImpl.fromFixedPoint64(mantissa64, exp), Decimal64.FromFixedPoint(mantissa64, exp));
						mantissa >>= 1;
						mantissa64 >>= 1;
					}
				}

				AssertDecimalEqual(NativeImpl.fromFixedPoint64(0, exp), DotNetImpl.FromFixedPointFast(0, exp));
				AssertDecimalEqual(NativeImpl.fromFixedPoint64(Int32.MinValue, exp), DotNetImpl.FromFixedPointFast(Int32.MinValue, exp));
				AssertDecimalEqual(NativeImpl.fromFixedPoint64(Int32.MaxValue, exp), DotNetImpl.FromFixedPointFast(Int32.MaxValue, exp));
			}
		}

		[Test]
		public void FromFixedPointFastMin()
		{
			Assert.Throws<ArgumentException>(delegate { DotNetImpl.FromFixedPointFast(0, 398 - 0x300); });
		}

		[Test]
		public void FromFixedPointFastMax()
		{
			Assert.Throws<ArgumentException>(delegate { DotNetImpl.FromFixedPointFast(0, 399); });
		}

		[Test]
		public void FromFixedPointFastUMin()
		{
			Assert.Throws<ArgumentException>(delegate { DotNetImpl.FromFixedPointFastUnsigned(0, 398 - 0x300); });
		}

		[Test]
		public void FromFixedPointFastUMax()
		{
			Assert.Throws<ArgumentException>(delegate { DotNetImpl.FromFixedPointFastUnsigned(0, 399); });
		}

		[Test]
		public void FromFixedPointUnsigned()
		{
			int m = 123;
			foreach (var n in new long[] { 0, 1, 2, 10, 100, 1000, int.MaxValue - 1, int.MaxValue, int.MaxValue + 1L, uint.MaxValue - 1L, uint.MaxValue, uint.MaxValue + 1L, long.MaxValue })
			{
				var dII = Decimal64.FromFixedPoint(m, (int)n);
				var dIU = Decimal64.FromFixedPoint(m, (uint)n);
				var dUI = Decimal64.FromFixedPoint((uint)m, (int)n);
				var dUU = Decimal64.FromFixedPoint((uint)m, (uint)n);
				var dLI = Decimal64.FromFixedPoint((long)m, (int)n);
				var dLU = Decimal64.FromFixedPoint((long)m, (uint)n);

				Assert.AreEqual(dII, dUI);
				Assert.AreEqual(dII, dLI);

				Assert.AreEqual(dIU, dUU);
				Assert.AreEqual(dIU, dLU);

				Assert.AreEqual(dUI, dLI);

				Assert.AreEqual(dUU, dLU);

				if (n <= int.MaxValue)
				{
					Assert.AreEqual(dII, dIU);
					Assert.AreEqual(dII, dUU);
					Assert.AreEqual(dII, dLU);

					Assert.AreEqual(dIU, dUI);
					Assert.AreEqual(dIU, dLI);

					Assert.AreEqual(dUI, dUU);
					Assert.AreEqual(dUI, dLU);

					Assert.AreEqual(dUU, dLI);

					Assert.AreEqual(dLI, dLU);
				}
			}
		}


		[Test]
		public void DecimalInternalRepresentation()
		{
			Random random = new Random();
			Assert.AreEqual(16, sizeof(Decimal));
			for (int i = 0; i < 1000; ++i)
			{
				int lo = random.Next(), mid = random.Next(), hi = random.Next();
				byte exp = (byte)random.Next(0, 28);
				bool sign = random.Next() < 0;
				var x = new Decimal(lo, mid, hi, sign, exp);
				int lo2, mid2, hi2;
				byte exp2;
				bool sign2;
				ulong lo64;
				unsafe
				{
					int* f = (int*)&x; // Order (Little endian): flags, hi, lo, mid
					lo64 = ((ulong*)&x)[1];
					lo2 = f[2];
					mid2 = f[3];
					hi2 = f[1];
					short flg16 = ((short*)&x)[1];
					sign2 = flg16 < 0;
					//exp2 = (byte) (f[0] >> 16);
					exp2 = (byte)flg16;
				}

				Assert.AreEqual(lo, lo2);
				Assert.AreEqual(mid, mid2);
				Assert.AreEqual((uint)lo | ((ulong)(uint)mid << 32), lo64);
				Assert.AreEqual(lo, lo2);
				Assert.AreEqual(hi, hi2);
				Assert.AreEqual(sign, sign2);
				Assert.AreEqual(exp, exp2);
			}
		}


		[Test]
		public void DecimalConversionBasicFrom()
		{
			Decimal x = new Decimal(6) / new Decimal(10000);
			var bits = Decimal.GetBits(x);


			AssertEqualToEmbeddedDecimal(x, (Decimal64)x);
			AssertEqualToEmbeddedDecimal(x, (Decimal64)x);
			AssertEqualToEmbeddedDecimal(x, Decimal64.FromDecimal(x));
			AssertEqualToEmbeddedDecimal(0.0006M, (Decimal64)0.0006M);
			AssertEqualToEmbeddedDecimal(0.0000006M, (Decimal64)0.0000006M);
			AssertEqualToEmbeddedDecimal(0.00000000000006M, (Decimal64)0.00000000000006M);
			AssertEqualToEmbeddedDecimal(9.8765432198721M, (Decimal64)9.8765432198721M);
			AssertEqualToEmbeddedDecimal(0.00000098765432198721M, (Decimal64)0.00000098765432198721M);
			AssertEqualToEmbeddedDecimal(0.0000000000000098765432198721M, (Decimal64)0.0000000000000098765432198721M);
			AssertEqualToEmbeddedDecimal(1234567890123456M, (Decimal64)1234567890123456M);
			AssertEqualToEmbeddedDecimal(1234567890123456000M, (Decimal64)1234567890123456000M);
			AssertEqualToEmbeddedDecimal(0.00000000001M, (Decimal64)0.00000000001M);
			AssertNotEquaTolEmbeddedDecimal(1234567890123456111111M, (Decimal64)123456789012345111111M);

			AssertNotEquaTolEmbeddedDecimal(0.0006M, (Decimal64)(double)0.0006M);
			AssertNotEquaTolEmbeddedDecimal(0.0000000000000098765432198721M, (Decimal64)(double)0.0000000000000098765432198721M);
			AssertNotEquaTolEmbeddedDecimal(1234567890123456111111M, (Decimal64)(double)123456789012345111111M);
		}

		[Test]
		public void DecimalConversionTo()
		{
			Decimal x = new Decimal(6) / new Decimal(10000);
			Assert.AreEqual(x, (Decimal)Decimal64.FromFixedPoint(6, 4));
			AssertEqualToEmbeddedDecimal(1234567890123456M, (Decimal)Decimal64.FromLong(1234567890123456));
			AssertEqualToEmbeddedDecimal(0.1234567890123456M, (Decimal)Decimal64.FromFixedPoint(1234567890123456, 16));
			AssertEqualToEmbeddedDecimal(0.00000000001234567890123456M, (Decimal)Decimal64.FromFixedPoint(1234567890123456, 26));

			AssertEqualToEmbeddedDecimal(123456789012345.6M, (Decimal)Decimal64.FromFixedPoint(1234567890123456, 1));
			Assert.AreEqual(123456789012345.600000000M, (Decimal)(Decimal64.FromFixedPoint(1234567890123456, 1)));
			Assert.AreEqual(123456789012300.000000000M, (Decimal)(Decimal64.FromFixedPoint(1234567890123000, 1)));
			Assert.AreEqual(123456789012300.000000000M, (Decimal)(Decimal64.FromFixedPoint(1234567890123, -2)));

			// These are not converted precisely(TODO:)
			AssertNotEquaTolEmbeddedDecimal(12345678901234560M, (Decimal)Decimal64.FromLong(12345678901234560));
			AssertNotEquaTolEmbeddedDecimal(1234567890123456000M, (Decimal)Decimal64.FromLong(1234567890123456000));
			AssertNotEquaTolEmbeddedDecimal(123456789012345600000000M, (Decimal)Decimal64.FromFixedPoint(1234567890123456, -8));
			AssertNotEquaTolEmbeddedDecimal(7345678901234560001M, (Decimal)Decimal64.FromLong(7345678901234560001));
		}

		[Test]
		public void DecimalConversion2()
		{
			Decimal x = new Decimal(6) / new Decimal(10000);
			AssertEqualToEmbeddedDecimal(x, (Decimal)(Decimal64)x);
		}


		[Test]
		public void NumberConversionTest()
		{
			for (long i0 = 0; i0 < 9999999999999999L; i0 = (i0 << 1) + 1)
			{
				long x = i0;
				for (int i = 0; i < 2; i++, x = -x)
				{
					Assert.That(x, Is.EqualTo((long)Decimal64.FromDouble(x).ToDouble()));
					Assert.That(x, Is.EqualTo((long)Decimal64.FromDouble(x).ToLong()));
					Assert.True(Decimal64.FromDouble(x).Equals(Decimal64.FromLong(x)));
					Assert.True(Decimal64.FromLong(x).Equals(Decimal64.FromDouble(x)));
					Assert.That(x, Is.EqualTo((long)Decimal64.FromDouble(x)));

					if (x >= 0)
					{
						Assert.That(x, Is.EqualTo((long)(ulong)Decimal64.FromDouble(x)));
					}
					else
					{
						//Assert.True(1UL << 31 == (UInt32)Decimal64.FromLong(x));
						//Assert.True(1UL << 63 == (ulong)Decimal64.FromLong(x));
					}

					if (Math.Abs(x) <= Int32.MaxValue)
					{
						Assert.That(x, Is.EqualTo((long)Decimal64.FromLong(x).ToInt()));
						Assert.That(x, Is.EqualTo((long)Decimal64.FromInt((int)x).ToDouble()));
						Assert.That(x, Is.EqualTo((long)Decimal64.FromInt((int)x).ToLong()));
						Assert.That(x, Is.EqualTo((long)Decimal64.FromInt((int)x).ToInt()));
						Assert.True(Decimal64.FromDouble(x).Equals(Decimal64.FromInt((int)x)));
						Assert.True(Decimal64.FromLong(x).Equals(Decimal64.FromInt((int)x)));

						Assert.That(x, Is.EqualTo((int)Decimal64.FromDouble(x)));

						if (x >= 0)
						{
							Assert.That(x, Is.EqualTo((UInt32)Decimal64.FromDouble(x)));
						}
						else
						{
							//Assert.That(1UL << 31, Is.EqualTo((UInt32)Decimal64.FromDouble(x)));
						}
					}
					else
					{
						// Expect integer overflow at some stage
						Assert.That(x, Is.Not.EqualTo((long)Decimal64.FromLong(x).ToInt()));
						Assert.That(x, Is.Not.EqualTo((long)Decimal64.FromInt((int)x).ToDouble()));
						Assert.That(x, Is.Not.EqualTo((long)Decimal64.FromInt((int)x).ToLong()));
						Assert.That(x, Is.Not.EqualTo((long)Decimal64.FromInt((int)x).ToInt()));
						Assert.False(Decimal64.FromDouble(x).Equals(Decimal64.FromInt((int)x)));
						Assert.False(Decimal64.FromLong(x).Equals(Decimal64.FromInt((int)x)));
					}
				}
			}
		}

		[Test]
		public void Min()
		{
			Assert.That(Decimal64.NaN.Min(Decimal64.Zero).Bits, Is.EqualTo(Decimal64.NaN.Bits));
			Assert.That(Decimal64.Zero.Min(Decimal64.NaN).Bits, Is.EqualTo(Decimal64.NaN.Bits));
			Assert.That(Decimal64.NaN.Min(Decimal64.NaN).Bits, Is.EqualTo(Decimal64.NaN.Bits));
		}

		[Test]
		public void Max()
		{
			Assert.That(Decimal64.NaN.Max(Decimal64.Zero).Bits, Is.EqualTo(Decimal64.NaN.Bits));
			Assert.That(Decimal64.Zero.Max(Decimal64.NaN).Bits, Is.EqualTo(Decimal64.NaN.Bits));
			Assert.That(Decimal64.NaN.Max(Decimal64.NaN).Bits, Is.EqualTo(Decimal64.NaN.Bits));
		}

		[Test]
		public void CompareTo()
		{
			Assert.That(Decimal64.FromDouble(1.0).CompareTo(Decimal64.Zero), Is.EqualTo(1.0.CompareTo(0.0)));
			Assert.That(Decimal64.Zero.CompareTo(Decimal64.Zero), Is.EqualTo(0.0.CompareTo(0.0)));
			Assert.That(Decimal64.Zero.CompareTo(Decimal64.FromDouble(1.0)), Is.EqualTo(0.0.CompareTo(1.0)));

			Assert.That(Decimal64.NaN.CompareTo(Decimal64.Zero), Is.EqualTo(Double.NaN.CompareTo(0.0)));
			Assert.That(Decimal64.Zero.CompareTo(Decimal64.NaN), Is.EqualTo(0.0.CompareTo(Double.NaN)));
			Assert.That(Decimal64.NaN.CompareTo(Decimal64.NaN), Is.EqualTo(Double.NaN.CompareTo(Double.NaN)));
		}

		[Test]
		public void TestSerializable()
		{
			var a1 = Decimal64.FromDouble(123.45);
			var b1 = Decimal64.FromLong(42);

			var formatter = new BinaryFormatter();
			var stream = new MemoryStream();
			formatter.Serialize(stream, a1);
			for (int i = 0; i < 40; ++i)
			{
				formatter.Serialize(stream, b1);
			}

			formatter.Serialize(stream, a1);

			stream.Seek(0, SeekOrigin.Begin);

			Assert.True(a1.Equals(formatter.Deserialize(stream)));
			for (int i = 0; i < 40; ++i)
			{
				Assert.True(b1.Equals(formatter.Deserialize(stream)));
			}

			Assert.True(a1.Equals(formatter.Deserialize(stream)));
			stream.Close();
		}

		[Test]
		public void TestFromDecimalDoubleBasic()
		{
			Assert.AreNotEqual("9.2", Decimal64.FromDouble(9.2).ToString());
			Assert.AreEqual("9.199999999999999", Decimal64.FromDouble(9.2).ToString());
			Assert.AreEqual("9.2", Decimal64.FromDecimalDouble(9.2).ToString());
			Assert.AreEqual("-0.065013624", Decimal64.FromDecimalDouble(-0.065013624).ToString());
			Assert.AreEqual("0", Decimal64.FromDecimalDouble(0).ToString());
		}

		void CheckDoubleConversion(Decimal64 x, Decimal64 x2)
		{
			AssertDecimalEqual(x, x2);
		}

		// [Test]
		public void TestDecimalFromDoubleConversions1()
		{
			for (int i = 0; i < N; i++)
			{
				Decimal64 x = GetRandomDecimal();
				CheckDoubleConversion(x, Decimal64.FromDouble(x.ToDouble()));
			}
		}


		private void CheckDecimalDoubleConversion(Decimal64 x, String s)
		{
			Decimal64 x2;
			CheckDoubleConversion(x, x2 = Decimal64.FromDecimalDouble(x.ToDouble()));
			AssertDecimalEqual(x, x2);

			if (null != s)
			{
				Assert.AreEqual(s, x2.ToString());
			}
		}

		private void CheckDecimalDoubleConversion(Decimal64 x)
		{
			CheckDecimalDoubleConversion(x, null);
		}


		private void CheckDecimalDoubleConversion(String s)
		{
			CheckDecimalDoubleConversion(Decimal64.Parse(s), s);
		}

		[Test]
		public void TestFromDecimalDoubleConversions2()
		{
			for (int i = 0; i < N; i++)
			{
				Decimal64 x = GetRandomDecimal();
				CheckDecimalDoubleConversion(x);
			}
		}

		[Test]
		public void MantissaZerosCombinationsTest()
		{
			MantissaZerosCombinations((m, l) => CheckDecimalDoubleConversion(Decimal64.FromFixedPoint(m, l)));
			MantissaZerosCombinations((m, l) => CheckDecimalDoubleConversion(Decimal64.FromFixedPoint(m / PowersOfTen[l], 0)));
		}

		[Test]
		public void CanonizeRandomTest()
		{
			MantissaZerosCombinations(delegate (long m, int l)
			{
				Decimal64 x = Decimal64.FromFixedPoint(m, l);
				Decimal64 y = Decimal64.FromFixedPoint(m / PowersOfTen[l], 0);

				CheckCanonize(x, y);
			});
			PartsCombinationsWithoutEndingZeros(delegate (long m, int e)
			{
				Decimal64 x = Decimal64.FromFixedPoint(m, e);
				CheckCanonize2(x, x.Canonize());
			});
		}


		[Test]
		public void ExtremeValuesOfExponentTest()
		{
			Decimal64 x = Decimal64.FromFixedPoint(1, 383 + 15);
			AssertDecimalEqual(x.ToUnderlying(), DotNetImpl.MinPositiveValue);
			x = Decimal64.FromFixedPoint(100, 400);
			AssertDecimalEqual(x.ToUnderlying(), DotNetImpl.MinPositiveValue);
			x = Decimal64.FromFixedPoint(1000000000000000L, 413);
			AssertDecimalEqual(x.ToUnderlying(), DotNetImpl.MinPositiveValue);

			x = Decimal64.FromFixedPoint(-1, 383 + 15);
			AssertDecimalEqual(x.ToUnderlying(), DotNetImpl.MaxNegativeValue);

			x = Decimal64.FromFixedPoint(-1, 308);
			CheckDecimalDoubleConversion(x);
			x = Decimal64.FromFixedPoint(-1000000000000000L, 322);
			CheckDecimalDoubleConversion(x);
			x = Decimal64.FromFixedPoint(1, 0);
			CheckDecimalDoubleConversion(x);
			x = Decimal64.FromFixedPoint(1000000000000000L, 0);
			CheckDecimalDoubleConversion(x);
		}

		[Test]
		public void FastSignCheck()
		{
			var testValues = new[]
			{
				Decimal64.FromDouble(Math.PI),
				Decimal64.MinValue,
				Decimal64.MaxValue,
				Decimal64.MinPositiveValue,
				Decimal64.MaxNegativeValue,
				Decimal64.Zero,
				Decimal64.PositiveInfinity,
				Decimal64.NegativeInfinity,
				Decimal64.NaN,
				Decimal64.Null
			};

			foreach (var testValue in testValues)
			{
				var negTestValue = testValue.Negate();
				CheckValues(testValue, NativeImpl.isPositive(testValue.Bits), testValue.IsPositive());
				CheckValues(negTestValue, NativeImpl.isPositive(negTestValue.Bits), negTestValue.IsPositive());

				CheckValues(testValue, NativeImpl.isNonPositive(testValue.Bits), testValue.IsNonPositive());
				CheckValues(negTestValue, NativeImpl.isNonPositive(negTestValue.Bits), negTestValue.IsNonPositive());

				CheckValues(testValue, NativeImpl.isNegative(testValue.Bits), testValue.IsNegative());
				CheckValues(negTestValue, NativeImpl.isNegative(negTestValue.Bits), negTestValue.IsNegative());

				CheckValues(testValue, NativeImpl.isNonNegative(testValue.Bits), testValue.IsNonNegative());
				CheckValues(negTestValue, NativeImpl.isNonNegative(negTestValue.Bits), negTestValue.IsNonNegative());

				CheckValues(testValue, NativeImpl.isZero(testValue.Bits), testValue.IsZero());
				CheckValues(negTestValue, NativeImpl.isZero(negTestValue.Bits), negTestValue.IsZero());

				CheckValues(testValue, NativeImpl.isNonZero(testValue.Bits), testValue.IsNonZero());
				CheckValues(negTestValue, NativeImpl.isNonZero(negTestValue.Bits), negTestValue.IsNonZero());

				if (!testValue.IsNaN())
				{
					CheckValues(testValue, NativeImpl.isPositive(testValue.Bits), testValue.CompareTo(Decimal64.Zero) > 0);
					CheckValues(negTestValue, NativeImpl.isPositive(negTestValue.Bits), negTestValue.CompareTo(Decimal64.Zero) > 0);

					CheckValues(testValue, NativeImpl.isNonPositive(testValue.Bits), testValue.CompareTo(Decimal64.Zero) <= 0);
					CheckValues(negTestValue, NativeImpl.isNonPositive(negTestValue.Bits), negTestValue.CompareTo(Decimal64.Zero) <= 0);

					CheckValues(testValue, NativeImpl.isNegative(testValue.Bits), testValue.CompareTo(Decimal64.Zero) < 0);
					CheckValues(negTestValue, NativeImpl.isNegative(negTestValue.Bits), negTestValue.CompareTo(Decimal64.Zero) < 0);

					CheckValues(testValue, NativeImpl.isNonNegative(testValue.Bits), testValue.CompareTo(Decimal64.Zero) >= 0);
					CheckValues(negTestValue, NativeImpl.isNonNegative(negTestValue.Bits), negTestValue.CompareTo(Decimal64.Zero) >= 0);
				}

				CheckValues(testValue, NativeImpl.isZero(testValue.Bits), testValue.CompareTo(Decimal64.Zero) == 0);
				CheckValues(negTestValue, NativeImpl.isZero(negTestValue.Bits), negTestValue.CompareTo(Decimal64.Zero) == 0);

				CheckValues(testValue, NativeImpl.isNonZero(testValue.Bits), testValue.CompareTo(Decimal64.Zero) != 0);
				CheckValues(negTestValue, NativeImpl.isNonZero(negTestValue.Bits), negTestValue.CompareTo(Decimal64.Zero) != 0);
			}
		}

		[Test]
		public void TestFormatting()
		{
			CheckInMultipleThreads(() =>
			{
				Random random = new Random();
				for (int i = 0; i < 1000000; ++i)
				{
					var x = Decimal64.FromFixedPoint(random.Next() << 32 | random.Next(), -(random.Next(80) - 40 - 15));

					CheckFormattingValue(x);
				}
			});
		}

		[Test]
		public void TestFormattingCase()
		{
			CheckFormattingValue(Decimal64.FromUnderlying(0x3420000037ffff73UL));
		}

		private void CheckFormattingValue(Decimal64 x)
		{
			{
				var xs = x.ToString();
				var y = Decimal64.Parse(xs);
				if (!Decimal64.Equals(x, y))
					throw new Exception("ToString error: The decimal " + xs + "(0x" + Convert.ToString((long)x.Bits, 16) + "L) != " + y.ToString() + "(0x" + Convert.ToString((long)y.Bits, 16) + "L)");
			}

			{
				var xs = x.ToScientificString();
				var y = Decimal64.Parse(xs);
				if (!Decimal64.Equals(x, y))
					throw new Exception("ToScientificString error: The decimal " + xs + "(0x" + Convert.ToString((long)x.Bits, 16) + "L) != " + y.ToScientificString() + "(0x" + Convert.ToString((long)y.Bits, 16) + "L)");
			}

			{
				var xs = x.AppendTo(new StringBuilder()).ToString();
				var y = Decimal64.Parse(xs);
				if (!Decimal64.Equals(x, y))
					throw new Exception("AppendTo error: The decimal " + xs + "(0x" + Convert.ToString((long)x.Bits, 16) + "L) != " + y.ToScientificString() + "(0x" + Convert.ToString((long)y.Bits, 16) + "L)");
			}

			{
				var xs = x.ScientificAppendTo(new StringBuilder()).ToString();
				var y = Decimal64.Parse(xs);
				if (!Decimal64.Equals(x, y))
					throw new Exception("ScientificAppendTo error: The decimal " + xs + "(0x" + Convert.ToString((long)x.Bits, 16) + "L) != " + y.ToScientificString() + "(0x" + Convert.ToString((long)y.Bits, 16) + "L)");
			}
		}

		private static bool DecimalParseOk(uint fpsf)
		{
			return (fpsf & DotNetReImpl.BID_INVALID_FORMAT) == 0;
		}

		[Test]
		public void TestParseReImpl()
		{
			int roundMode = 10; // DotNetReImpl.BID_ROUNDING_TO_NEAREST;
			{
				var testStr = "000";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.Zero, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_EXACT_STATUS);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "00..";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.NaN, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INVALID_FORMAT);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "000235";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.FromInt(235), value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_EXACT_STATUS);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "00.0000235";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.FromFixedPoint(235, 7), value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_EXACT_STATUS);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "1234512345123451234500000";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.FromFixedPoint(1234512345123451, -9), value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "1234512345123451234500000e+12345123451234512345";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.PositiveInfinity, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION | DotNetReImpl.BID_OVERFLOW_EXCEPTION);
				Assert.AreEqual(/*doubleParseOk*/ true, DecimalParseOk(fpsf));
			}
			{
				var testStr = "-5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.NegativeInfinity, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION | DotNetReImpl.BID_OVERFLOW_EXCEPTION);
				Assert.AreEqual(/*doubleParseOk*/ true, DecimalParseOk(fpsf));
			}
			{
				var testStr = "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.Zero, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION | DotNetReImpl.BID_UNDERFLOW_EXCEPTION);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "123 x99";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.NaN, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INVALID_FORMAT);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
		}

		private static void CheckValues(Decimal64 value, bool refCond, bool testCond)
		{
			if (refCond != testCond)
				throw new Exception("TestValue(=" + value + ") check error: refCond(=" + refCond + ") != testCond(" + testCond + ").");
		}

		private static String Round(String valueIn, int n, RoundType roundType)
		{
			string value = valueIn;

			bool isNegSign = false;
			if (value[0] == '-' || value[0] == '+')
			{
				isNegSign = value[0] == '-';
				value = value.Substring(1);
			}

			if (value.Contains("NaN") || value.Contains("Infinity"))
				return valueIn;

			int latestPoint;
			{
				int dotPoint = value.IndexOf('.');
				if (dotPoint < 0)
					dotPoint = value.Length;
				latestPoint = dotPoint + n + (n > 0 ? 0 : -1);
				if (latestPoint >= value.Length - 1)
					return (isNegSign ? "-" : "") + value;
				if (latestPoint < 0)
				{
					string zerosStr;
					{
						StringBuilder zeros = new StringBuilder(-latestPoint);
						for (int i = 0; i < -latestPoint; ++i)
							zeros.Append('0');
						zerosStr = zeros.ToString();
					}
					value = zerosStr + value;
					latestPoint += zerosStr.Length;
				}
			}

			{
				value = '0' + value;
				latestPoint += 1;
			}

			string fixedPart = value.Substring(0, latestPoint + 1);
			int fixedExp;
			{
				int dotPoint = value.IndexOf('.');
				if (dotPoint < 0)
					dotPoint = value.Length;
				fixedExp = Math.Max(0, dotPoint - 1 - latestPoint);
			}
			switch (roundType)
			{
				case RoundType.Round:
					if (latestPoint + 1 >= value.Length)
						return formatMantissaExp(isNegSign, fixedPart, fixedExp);
					char nextChar = '0';
					if (latestPoint + 1 < value.Length)
						nextChar = value[latestPoint + 1];
					if (nextChar == '.')
						nextChar = latestPoint + 2 < value.Length ? value[latestPoint + 2] : '0';
					return formatMantissaExp(isNegSign, nextChar >= '5' ? incMantissa(fixedPart) : fixedPart, fixedExp);
				case RoundType.Trunc:
					return formatMantissaExp(isNegSign, fixedPart, fixedExp);
				case RoundType.Floor:
					if (!isNegSign)
						return formatMantissaExp(isNegSign, fixedPart, fixedExp);
					else
						return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);
				case RoundType.Ceil:
					if (!isNegSign)
						return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);
					else
						return formatMantissaExp(isNegSign, fixedPart, fixedExp);
				default:
					throw new ArgumentException("Unsupported roundType(=" + roundType + ") value.");
			}
		}

		private static String formatMantissaExp(bool isNegSign, String value, int exp)
		{
			if (exp > 0)
			{
				StringBuilder sb = new StringBuilder(exp);
				for (int i = 0; i < exp; ++i)
					sb.Append('0');
				value = value + sb;
			}

			{
				int leftIndex;
				for (leftIndex = 0; leftIndex < value.Length; ++leftIndex)
					if (value[leftIndex] != '0')
						break;
				if (leftIndex < value.Length && value[leftIndex] == '.')
					leftIndex--;
				value = value.Substring(leftIndex);
			}

			{
				int dotIndex = value.IndexOf('.');
				if (dotIndex >= 0)
				{
					int rightIndex = value.Length;
					while (rightIndex > dotIndex && (value[rightIndex - 1] == '0'))
						--rightIndex;
					if (rightIndex - 1 == dotIndex)
						rightIndex = dotIndex;
					value = value.Substring(0, rightIndex);
				}
			}

			return string.IsNullOrEmpty(value) || value.Equals("0") ? "0" : (isNegSign ? "-" : "") + value;
		}

		private static String incMantissa(String str)
		{
			char[] chars = str.ToCharArray();
			int carry = 1;
			for (int ii = chars.Length - 1; ii >= 0 && carry > 0; --ii)
			{
				if (chars[ii] == '.' || chars[ii] == '-')
					continue;
				if (chars[ii] < '0' && chars[ii] > '9')
					throw new ArgumentException("Unsupported character at [" + ii + "] in string '" + str + "'.");
				int ch = chars[ii] - '0' + carry;
				if (ch > 9)
				{
					chars[ii] = '0';
					carry = 1;
				}
				else
				{
					chars[ii] = (char)('0' + ch);
					carry = 0;
				}
			}
			if (carry != 0)
			{
				var charsTmp = new char[chars.Length];
				Array.Copy(chars, charsTmp, chars.Length);
				chars = charsTmp;
				int firstDigit = chars[0] == '-' ? 1 : 0;
				Array.Copy(chars, firstDigit, chars, firstDigit + 1, chars.Length - 1 - firstDigit);
				chars[firstDigit] = '1';
			}
			return new String(chars);
		}

		private static bool isNonZero(String str, int i)
		{
			for (int ii = i, ie = str.Length; ii < ie; ++ii)
			{
				char c = str[ii];
				if (c >= '1' && c <= '9')
					return true;
			}
			return false;
		}

		[Test]
		public void TestRoundRandomly()
		{
			CheckInMultipleThreads(() =>
			{
				var random = new Random();
				for (int ri = 0; ri < 1000000; ++ri)
				{
					double mantissa = random.NextDouble() * 2 - 1;
					int tenPower = random.Next(308 * 2 + 1) - 308;
					int randomOffset = random.Next(20 * 2 + 1) - 20;

					var inValue = Decimal64.FromDouble(mantissa * Math.Pow(10, tenPower));
					int roundPoint = tenPower + randomOffset;
					RoundType roundType;
					switch (random.Next(4))
					{
						case 0:
							roundType = RoundType.Round;
							break;
						case 1:
							roundType = RoundType.Trunc;
							break;
						case 2:
							roundType = RoundType.Floor;
							break;
						case 3:
							roundType = RoundType.Ceil;
							break;
						default:
							throw new Exception("Unsupported case for round type generation.");
					}

					checkRound(inValue, -roundPoint, roundType);
				}
			});
		}

		static Decimal64[] specialValues = {
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
			Decimal64.FromUnderlying(DotNetReImpl.SPECIAL_ENCODING_MASK64 | 1000000000000000UL),
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

		[Test]
		public void TestRoundCase()
		{
			foreach (var testValue in specialValues)
				foreach (var roundPoint in new int[] { 20, 10, 5, 3, 1, 0, -1, -2, -6, -11, -19 })
					foreach (RoundType roundType in Enum.GetValues(typeof(RoundType)))
						checkRound(testValue, roundPoint, roundType);

			unchecked
			{
				checkRound(Decimal64.FromUnderlying((ulong)-5787416479386436811L), 1, RoundType.Round);
				checkRound(Decimal64.FromUnderlying(3439124486823148033L), 1, RoundType.Floor);
				checkRound(Decimal64.FromUnderlying((ulong)-1444740417884338647L), 0, RoundType.Round);
				checkRound(Decimal64.FromUnderlying(3439028411434681001L), -7, RoundType.Round);
				checkRound(Decimal64.FromUnderlying((ulong)-5778759999361643774L), 2, RoundType.Round);
				checkRound(Decimal64.FromUnderlying(3448058746773778910L), -4, RoundType.Ceil);
				checkRound(Decimal64.FromUnderlying(1417525816301142050L), -209, RoundType.Ceil);
				checkRound(Decimal64.FromUnderlying(2996092184105885832L), -61, RoundType.Ceil);
				checkRound(Decimal64.FromUnderlying((ulong)-922689384669825404L), -236, RoundType.Floor);
			}
		}

		private static void checkRound(Decimal64 inValue, int roundPoint, RoundType roundType)
		{
			var testValue = inValue.Round(roundPoint, roundType);
			String inStr = inValue.ToString();
			String roundStr = Round(inStr, roundPoint, roundType);
			String testStr = testValue.ToString();
			if (!roundStr.Equals(testStr))
				throw new Exception("Case checkRound(" + inValue + "L, " + roundPoint + ", RoundType." + roundType +
					"); error: input value (=" + inStr + ") string rounding (=" + roundStr + ") != decimal rounding (=" + testStr + ")");
		}

		[Test]
		public void unCanonizedRound()
		{
			var zeroU = Decimal64.FromUnderlying(0x2FE0000000000000UL);
			var f = Decimal64.FromUnderlying(0x2F638D7EA4C68000UL); // 0.0001
			var zeroP = zeroU.Multiply(f);

			Assert.AreEqual(Decimal64.Zero, zeroP.Round(0, RoundType.Ceil));
		}

		[Test]
		public void TestPartsSplit()
		{
			foreach (var testValue in specialValues)
			{
				if (!testValue.IsFinite())
					continue;

				var mantissa = testValue.GetUnscaledValue();
				var exp = testValue.GetScale();

				Assert.AreEqual(testValue, Decimal64.FromFixedPoint(mantissa, exp),
					$"The decimal 0x{testValue.Bits:X}UL(={testValue.ToScientificString()}) reconstruction error.");
			}
		}

		readonly int N = 5000000;

		static void Main()
		{
		}
	}
}
