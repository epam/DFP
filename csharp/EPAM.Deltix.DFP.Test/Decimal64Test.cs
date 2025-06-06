using System;
using System.Globalization;
using System.IO;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
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
			Assert.AreEqual("9.888888888888888", Decimal64.FromDecimalDouble(9.888888888888888).ToString());
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
				Decimal64 x = GetRandomDecimal(9999999999999999L);
				CheckDoubleConversion(x, Decimal64.FromDouble(x.ToDouble()));
			}
		}


		private void CheckDecimalDoubleConversion(Decimal64 x, String s)
		{
			Decimal64 x2 = Decimal64.FromDecimalDouble(x.ToDouble());
			Assert.AreEqual(x.ToDouble(), x2.ToDouble());
			if (!x2.Equals(Decimal64.FromDouble(x.ToDouble())))
			{
				AssertDecimalIdentical(x2.Canonize(), x2, "FromDecimalDouble(x) failed to Canonize() the result");
			}

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
				Decimal64 x = GetRandomDecimal(999999999999999L);
				CheckDecimalDoubleConversion(x);
			}
		}

		[Test]
		public void TestFromDecimalDoubleConversionsLongMantissa()
		{
			for (int i = 0; i < N; i++)
			{
				Decimal64 x = GetRandomDecimal(1000000000000000L, 9999999999999999L);
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
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.Zero, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_EXACT_STATUS);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "00..";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.NaN, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INVALID_FORMAT);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "000235";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.FromInt(235), value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_EXACT_STATUS);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "00.0000235";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.FromFixedPoint(235, 7), value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_EXACT_STATUS);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "1234512345123451234500000";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.FromFixedPoint(1234512345123451, -9), value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "1234512345123451234500000e+12345123451234512345";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.PositiveInfinity, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION | DotNetReImpl.BID_OVERFLOW_EXCEPTION);
				Assert.AreEqual(/*doubleParseOk*/ true, DecimalParseOk(fpsf));
			}
			{
				var testStr = "-5000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.NegativeInfinity, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION | DotNetReImpl.BID_OVERFLOW_EXCEPTION);
				Assert.AreEqual(/*doubleParseOk*/ true, DecimalParseOk(fpsf));
			}
			{
				var testStr = "0.00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000005";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
				double doubleValue;
				var doubleParseOk = Double.TryParse(testStr, NumberStyles.Float, CultureInfo.InvariantCulture, out doubleValue);
				Assert.AreEqual(Decimal64.Zero, value);
				Assert.AreEqual(fpsf, DotNetReImpl.BID_INEXACT_EXCEPTION | DotNetReImpl.BID_UNDERFLOW_EXCEPTION);
				Assert.AreEqual(doubleParseOk, DecimalParseOk(fpsf));
			}
			{
				var testStr = "123 x99";
				uint fpsf;
				var value = Decimal64.FromUnderlying(DotNetReImpl.bid64_from_string(testStr, Decimal64.DecimalMarkAny, out fpsf, roundMode));
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

		private static String Round(String valueIn, int n, RoundingMode roundType)
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
				case RoundingMode.Up:
					return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);

				case RoundingMode.Down:
					return formatMantissaExp(isNegSign, fixedPart, fixedExp);

				case RoundingMode.Ceiling:
					if (!isNegSign)
						return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);
					else
						return formatMantissaExp(isNegSign, fixedPart, fixedExp);

				case RoundingMode.Floor:
					if (!isNegSign)
						return formatMantissaExp(isNegSign, fixedPart, fixedExp);
					else
						return formatMantissaExp(isNegSign, isNonZero(value, latestPoint + 1) ? incMantissa(fixedPart) : fixedPart, fixedExp);

				case RoundingMode.HalfUp:
					{
						if (latestPoint + 1 >= value.Length)
							return formatMantissaExp(isNegSign, fixedPart, fixedExp);
						char nextChar = '0';
						if (latestPoint + 1 < value.Length)
							nextChar = value[latestPoint + 1];
						if (nextChar == '.')
							nextChar = latestPoint + 2 < value.Length ? value[latestPoint + 2] : '0';
						return formatMantissaExp(isNegSign, nextChar >= '5' ? incMantissa(fixedPart) : fixedPart, fixedExp);
					}

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
					RoundingMode roundType = (RoundingMode)random.Next((int)RoundingMode.HalfDown);

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
					for (int roundType = 0; roundType < (int)RoundingMode.HalfDown; ++roundType)
						//foreach (RoundingMode roundType in Enum.GetValues(typeof(RoundingMode)))
						checkRound(testValue, roundPoint, (RoundingMode)roundType);

			unchecked
			{
				checkRound(Decimal64.FromUnderlying((ulong)-5787416479386436811L), 1, RoundingMode.HalfUp);
				checkRound(Decimal64.FromUnderlying(3439124486823148033L), 1, RoundingMode.Floor);
				checkRound(Decimal64.FromUnderlying((ulong)-1444740417884338647L), 0, RoundingMode.HalfUp);
				checkRound(Decimal64.FromUnderlying(3439028411434681001L), -7, RoundingMode.HalfUp);
				checkRound(Decimal64.FromUnderlying((ulong)-5778759999361643774L), 2, RoundingMode.HalfUp);
				checkRound(Decimal64.FromUnderlying(3448058746773778910L), -4, RoundingMode.Ceiling);
				checkRound(Decimal64.FromUnderlying(1417525816301142050L), -209, RoundingMode.Ceiling);
				checkRound(Decimal64.FromUnderlying(2996092184105885832L), -61, RoundingMode.Ceiling);
				checkRound(Decimal64.FromUnderlying((ulong)-922689384669825404L), -236, RoundingMode.Floor);
			}
		}

		private static void checkRound(Decimal64 inValue, int roundPoint, RoundingMode roundType)
		{
			var testValue = inValue.Round(roundPoint, roundType);
			String inStr = inValue.ToString();
			String roundStr = Round(inStr, roundPoint, roundType);
			String testStr = testValue.ToString();
			if (!roundStr.Equals(testStr))
				throw new Exception("Case checkRound(" + inValue + "L, " + roundPoint + ", RoundingMode." + roundType +
					"); error: input value (=" + inStr + ") string rounding (=" + roundStr + ") != decimal rounding (=" + testStr + ")");
		}

		[Test]
		public void unCanonizedRound()
		{
			var zeroU = Decimal64.FromUnderlying(0x2FE0000000000000UL);
			var f = Decimal64.FromUnderlying(0x2F638D7EA4C68000UL); // 0.0001
			var zeroP = zeroU.Multiply(f);

			Assert.AreEqual(Decimal64.Zero, zeroP.Round(0, RoundingMode.Ceiling));
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

		[Test]
		public void TestRoundToReciprocal()
		{
			unchecked
			{
				TestRoundToReciprocalCase(/*0.0000000006*/ Decimal64.FromUnderlying(3485786111584763964UL), 1353409007, RoundingMode.Up);

				TestRoundToReciprocalCase(/*0.000000001*/ Decimal64.FromUnderlying(3503800510094245889UL), 789577771, RoundingMode.HalfUp);

				TestRoundToReciprocalCase(/*0.00000000005*/ Decimal64.FromUnderlying(3485786111584763909UL), 1329821241, RoundingMode.Up);

				TestRoundToReciprocalCase(/*8277765774399323000*/ Decimal64.FromUnderlying(3620164666925537115UL), 92111324, RoundingMode.HalfUp);

				TestRoundToReciprocalCase(Decimal64.Parse("0.9999999999999999"), UInt32.MaxValue, RoundingMode.Down);

				//@@@ TestRoundToReciprocalCase(Decimal64.Parse("0.125"), 8, RoundingMode.Unnecessary);

				//@@@ TestRoundToReciprocalCase(/*687034157780582.4*/ Decimal64.FromUnderlying(3582728445709979648UL), 1440395186, RoundingMode.HalfEven);

				TestRoundToReciprocalCase(/*0.000000000093*/ Decimal64.FromUnderlying(3476778912330023005UL), 76984627, RoundingMode.Ceiling);

				TestRoundToReciprocalCase(/*-0.000000000079*/ Decimal64.FromUnderlying((ulong)-5746593124524752817L), 1850110060, RoundingMode.Down);

				TestRoundToReciprocalCase(/*-0.0001*/ Decimal64.FromUnderlying((ulong)-5674535530486824959L), 579312130, RoundingMode.Down);

				TestRoundToReciprocalCase(/*-0.000000000923*/ Decimal64.FromUnderlying((ulong)-5746593124524751973L), 1, RoundingMode.Up);
				TestRoundToReciprocalCase(/*-0.000000000923*/ Decimal64.FromUnderlying((ulong)-5746593124524751973L), 15292403, RoundingMode.Up);
				TestRoundToReciprocalCase(/*0.00000000000043*/ Decimal64.FromUnderlying(3458764513820540971UL), 63907328, RoundingMode.Up);

				TestRoundToReciprocalCase(Decimal64.Parse("0.0009999999999999999"), Int32.MaxValue, RoundingMode.Down);

				RoundingMode[] roundingModes = { RoundingMode.Up, RoundingMode.Down,
					 RoundingMode.Ceiling, RoundingMode.Floor,
					 RoundingMode.HalfUp/*@@@, RoundingMode.HalfDown, RoundingMode.HalfEven*/};

				ThreadLocal<Random> tlsRandom = new ThreadLocal<Random>(() => new Random());

				Parallel.For(0, 10000000, (i, state) =>
				{
					Random random = tlsRandom.Value;

					int mantissaLen = random.Next(Decimal64.MaxSignificandDigits) + 1;
					long mantissa = (((long)random.Next() << 32) | ((long)random.Next() & 0xFFFFFFFFL)) % (long)DotNetImpl.PowersOfTen[mantissaLen];

					int exp = random.Next(20) - Decimal64.MaxSignificandDigits;

					Decimal64 value = Decimal64.FromFixedPoint(mantissa, -exp);

					uint n = (uint)Math.Abs((long)random.Next() + Int32.MinValue);
					if (n == 0)
						n = 1;

					RoundingMode roundingMode = roundingModes[random.Next(roundingModes.Length)];

					TestRoundToReciprocalCase(value, n, roundingMode);
				});
			}
		}

		private static void TestRoundToReciprocalCase(Decimal64 value, uint r, RoundingMode roundingMode)
		{
			var roundedValue = value.RoundToReciprocal(r, roundingMode);

			var valMulR = decimal.Parse(value.ToString()) * r;
			var valMulRRounded = Round(valMulR.ToString(), 0, roundingMode);
			var refValue = decimal.Parse(valMulRRounded) / r;
			var refValueStr = refValue.ToString();

			if (refValue != decimal.Zero)
			{
				int dotIndex = refValueStr.IndexOf('.');
				if (dotIndex >= 0)
					refValueStr = Round(refValueStr, roundedValue.ToString().Length - dotIndex - 1, RoundingMode.HalfUp);
			}

			refValue = decimal.Parse(refValueStr);

			var ulp = decimal.Parse((roundedValue.NextUp() - roundedValue).ToString());

			if (refValue - decimal.Parse(roundedValue.ToString()) > ulp)
				ThrowRoundToReciprocalCaseException(value, r, roundingMode, $"roundedValue(={roundedValue}) != refValue(={refValue})");
		}

		private static void ThrowRoundToReciprocalCaseException(Decimal64 value, uint r, RoundingMode roundingMode, string message)
		{
			throw new Exception($"TestRoundToReciprocalCase(/*{value}*/ Decimal64.FromUnderlying({value.ToUnderlying()}UL), {r}, RoundingMode.{roundingMode}); // {message}");
		}

		[Test]
		public void TestRoundDiv()
		{
			Assert.False(Decimal64.Parse("1").IsRounded(-1));
			Assert.True(Decimal64.Parse("1").IsRounded(0));
			Assert.True(Decimal64.Parse("1").IsRounded(1));
			Assert.False(Decimal64.Parse("1.23").IsRounded(1));
			Assert.False(Decimal64.Parse("1.23").IsRounded(-10));
			Assert.True(Decimal64.Parse("1.23").IsRounded(2));
			Assert.True(Decimal64.Parse("1.23").IsRounded(3));
			Assert.False(Decimal64.Parse("1.23456789").IsRounded(-0));
			Assert.False(Decimal64.Parse("1.23456789").IsRounded(7));
			Assert.True(Decimal64.Parse("1.23456789").IsRounded(8));
			Assert.True(Decimal64.Parse("1.23456789").IsRounded(9));
			Assert.True(Decimal64.Parse("123E10").IsRounded(-9));
			Assert.True(Decimal64.Parse("123E10").IsRounded(-10));
			Assert.False(Decimal64.Parse("123E10").IsRounded(-11));
			Assert.False(Decimal64.Parse("-10E-10").IsRounded(8));
			Assert.True(Decimal64.Parse("-10E-10").IsRounded(9));
			Assert.True(Decimal64.Parse("-10E-10").IsRounded(10));
			Assert.True(Decimal64.Parse("0").IsRounded(-11));
			Assert.False(Decimal64.Parse("Inf").IsRounded(0));
			Assert.False(Decimal64.Parse("NaN").IsRounded(-11));
			Assert.False(Decimal64.Parse("-Inf").IsRounded(10));
		}

		[Test]
		public void TestToDecimalAndBack()
		{
			{
				var refStr = "-0.00009147639003404085";
				var dn = decimal.Parse(refStr + "00000001");
				var d64 = Decimal64.FromDecimal(dn);
				Assert.AreEqual(refStr, d64.ToString());
			}

			TestToDecimalAndBackCase(9937272758032147L, -24); // d1(=0.000000009937272758032147) != d2(=0.000000009937272758032149)
			TestToDecimalAndBackCase(957877248391343793L, -23); // d1(=0.000009578772483913438) != d2(=0.000009578772483913439)
			TestToDecimalAndBackCase(-9147639003404085469L, -23); // d1(=0.00009147639003404085) != d2(=0.00009147639003404083)

			var random = new Random();
			for (int ri = 0; ri < 1000000; ++ri)
			{
				long mantissa = ((long)random.Next(int.MinValue, int.MaxValue) << 32) | ((long)random.Next(int.MinValue, int.MaxValue) & 0xFFFFFFFFL);

				int exp = random.Next(24) - 12 - Decimal64.MaxSignificandDigits;

				TestToDecimalAndBackCase(mantissa, exp);
			}
		}

		private static void TestToDecimalAndBackCase(long mantissa, int exp)
		{
			var d0 = Decimal64.FromFixedPoint(mantissa, -exp);
			var d1 = d0.ToDecimal();
			var d2 = Decimal64.FromDecimal(d1);

			if (d0 != d2)
				throw new Exception($"TestToDecimalAndBackCase({mantissa}L, {exp}); // d1(={d0}) != d2(={d2})");
		}

		private class ToStringData
		{
			public Decimal64 TestValue;
			public string NormalOut;
			public string FloatOut;

			public ToStringData(Decimal64 testValue, string normalOut, string floatOut)
			{
				this.TestValue = testValue;
				this.NormalOut = normalOut;
				this.FloatOut = floatOut;
			}
		}

		[Test]
		public void Issue91ToFloatString()
		{
			var testCases = new ToStringData[]
			{
				new ToStringData(Decimal64.FromFixedPoint(14L, 0), "14", "14.0"),
				new ToStringData(Decimal64.FromFixedPoint(140000000000L, 10), "14", "14.0"),
				new ToStringData(Decimal64.Zero, "0", "0.0")
			};

			foreach(var testCase in testCases)
			{
				var testValue = testCase.TestValue;
				Assert.AreEqual(testCase.NormalOut, testValue.ToString());
				Assert.AreEqual(testCase.FloatOut, testValue.ToFloatString());

				{
					var sb = new StringBuilder();
					Assert.AreEqual(testCase.NormalOut, testValue.AppendTo(sb).ToString());
				}

				{
					var sb = new StringBuilder();
					Assert.AreEqual(testCase.FloatOut, testValue.FloatAppendTo(sb).ToString());
				}
			}
		}

		[Test]
		public void TestShortenMantissaBigDelta()
		{
			Assert.AreEqual(Decimal64.Parse("10000000000000000"),
				Decimal64.Parse("9999000000000000").ShortenMantissa(DotNetImpl.MaxCoefficient / 10, 2));
		}

		[Test]
		public void TestShortenMantissaCase006()
		{
			String testString = "0.006";
			var testValue = Double.Parse(testString);
			var testX = 0.005999999999998265; // Math.nextDown(testValue);

			var d64 = Decimal64.FromDouble(testX).ShortenMantissa(1735, 1);
			Assert.AreEqual(testString, d64.ToString());

			Decimal64.FromDouble(9.060176071990028E-7).ShortenMantissa(2, 1);
		}

		[Test]
		public void TestShortenMantissaRandom()
		{
			var randomSeed = new Random().Next();
			var random = new Random(randomSeed);

			try
			{
				for (int iteration = 0; iteration < N; ++iteration)
				{
					var mantissa = GenerateMantissa(random, Decimal64.MaxSignificandDigits);
					int error = random.Next(3) - 1;
					mantissa = Math.Min(DotNetImpl.MaxCoefficient, Math.Max(0, (ulong)((long)mantissa + error)));
					if (mantissa <= DotNetImpl.MaxCoefficient / 10)
						mantissa = mantissa * 10;

					var delta = GenerateMantissa(random, 0);
					if (delta > DotNetImpl.MaxCoefficient / 10)
						delta = delta / 10;

					CheckShortenMantissaCase(mantissa, delta);
				}
			}
			catch (Exception e)
			{
				throw new Exception("Random seed " + randomSeed + " exception: " + e.Message, e);
			}
		}

		[Test]
		public void TestShortenMantissaCase()
		{
			CheckShortenMantissaCase(9999888877776001UL, 1000);
			CheckShortenMantissaCase(1230000000000000UL, 80);
			CheckShortenMantissaCase(1230000000000075UL, 80);
			CheckShortenMantissaCase(1229999999999925UL, 80);
			CheckShortenMantissaCase(4409286553495543UL, 900);
			CheckShortenMantissaCase(4409286553495000UL, 1000);
			CheckShortenMantissaCase(4409286550000000UL, 81117294);
			CheckShortenMantissaCase(9010100000000001UL, 999999999999999L);
			CheckShortenMantissaCase(8960196546869015UL, 1);
			CheckShortenMantissaCase(4700900091799999UL, 947076117508L);
			CheckShortenMantissaCase(5876471737721999UL, 91086);
			CheckShortenMantissaCase(6336494570000000UL, 6092212816L);
			CheckShortenMantissaCase(8960196546869011UL, 999999999999999L);
			CheckShortenMantissaCase(1519453608576584UL, 3207L);
		}

		private static void CheckShortenMantissaCase(ulong mantissa, ulong delta)
		{
			try
			{
				var bestSolution = ShortenMantissaDirect(mantissa, delta);

				var test64 = Decimal64.FromULong(mantissa).ShortenMantissa(delta, 0).ToULong();

				if (test64 != bestSolution)
					throw new Exception("The mantissa(=" + mantissa + ") and delta(=" + delta + ") produce test64(=" + test64 + ") != bestSolution(=" + bestSolution + ").");
			}
			catch (Exception e)
			{
				throw new Exception("The mantissa(=" + mantissa + ") and delta(=" + delta + ") produce exception.", e);
			}
		}

		private static ulong ShortenMantissaDirect(ulong mantissaIn, ulong deltaIn)
		{
			var mantissa = (long)mantissaIn;
			var delta = (long)deltaIn;
			var rgUp = mantissa + delta;
			var rgDown = mantissa - delta;

			if (mantissaIn <= DotNetImpl.MaxCoefficient / 10 || mantissaIn > DotNetImpl.MaxCoefficient)
				throw new ArgumentException("The mantissa(=" + mantissa + ") must be in (" + DotNetImpl.MaxCoefficient / 10 + ".." + DotNetImpl.MaxCoefficient + "] range");

			long bestSolution = long.MinValue;
			if (rgDown > 0)
			{
				long mUp = (mantissa / 10) * 10;
				long mFactor = 1;

				long bestDifference = long.MaxValue;
				int bestPosition = -1;

				for (int replacePosition = 0;
					 replacePosition < Decimal64.MaxSignificandDigits + 1;
					 ++replacePosition, mUp = (mUp / 100) * 10, mFactor *= 10)
				{
					for (uint d = 0; d < 10; ++d)
					{
						long mTest = (mUp + d) * mFactor;
						if (rgDown <= mTest && mTest <= rgUp)
						{
							var md = Math.Abs(mantissa - mTest);
							if (bestPosition < replacePosition ||
								(bestPosition == replacePosition && bestDifference >= md))
							{
								bestPosition = replacePosition;
								bestDifference = md;
								bestSolution = mTest;
							}
						}
					}
				}
			}
			else
			{
				bestSolution = 0;
			}

			return (ulong)bestSolution;
		}

		private static ulong GenerateMantissa(Random random, int minimalLength)
		{
			int mLen = (1 + random.Next(Decimal64.MaxSignificandDigits) /*[1..16]*/);
			ulong m = 1 + (ulong)random.Next(9);
			int i = 1;
			for (; i < mLen; ++i)
				m = m * 10 + (ulong)random.Next(10);
			for (; i < minimalLength; ++i)
				m = m * 10;
			return m;
		}

		readonly int N = 5000000;

		static void Main()
		{
		}
	}
}
