using System;
using System.Security.Cryptography;

namespace EPAM.Deltix.DFP.Test
{
	public class RandomDecimalsGenerator
	{
		public Random Generator { get; private set; }

		readonly int mantissaMaxShift;
		readonly int exponentRange;
		readonly int exponentOffset;

		static readonly int TwiceOfMaxSignificandDigits = Decimal64.MaxSignificandDigits * 2;
		static readonly int HalfOfMaxSignificandDigits = Decimal64.MaxSignificandDigits / 2;

		public RandomDecimalsGenerator() : this(GenerateSeed())
		{
		}

		private static Int32 GenerateSeed()
		{
			var cryptoResult = new byte[4];
			RandomNumberGenerator.Create().GetBytes(cryptoResult);

			return BitConverter.ToInt32(cryptoResult, 0);
		}

		public RandomDecimalsGenerator(int randomSeed) : this(new Random(randomSeed), 1,
				-TwiceOfMaxSignificandDigits - HalfOfMaxSignificandDigits,
				TwiceOfMaxSignificandDigits - HalfOfMaxSignificandDigits)
		{
		}

		public RandomDecimalsGenerator(
			Random generator,
			int mantissaMinBits,
			int exponentMin,
			int exponentMax)
		{
			if (generator == null)
				throw new ArgumentException("The random argument is null.", nameof(generator));
			if (mantissaMinBits < 1 || 64 < mantissaMinBits)
				throw new ArgumentOutOfRangeException(nameof(mantissaMinBits), mantissaMinBits, $"The mantissaMinBits(={mantissaMinBits}) must lie in [1..64] range");
			if (exponentMin < Decimal64.MinExponent || Decimal64.MaxExponent < exponentMin)
				throw new ArgumentOutOfRangeException(nameof(exponentMin), exponentMin, $"The exponentMin(={exponentMin}) must lie in [{Decimal64.MinExponent}..{Decimal64.MaxExponent}] range.");
			if (exponentMax < Decimal64.MinExponent || Decimal64.MaxExponent < exponentMax)
				throw new ArgumentOutOfRangeException(nameof(exponentMax), exponentMax, $"The exponentMax(={exponentMax}) must lie in [{Decimal64.MinExponent}..{Decimal64.MaxExponent}] range.");
			if (exponentMax <= exponentMin)
				throw new ArgumentException($"The exponentMin(={exponentMin}) must be less than the exponentMax(={exponentMax}).");

			this.Generator = generator;
			this.mantissaMaxShift = 64 - mantissaMinBits + 1 /*  for random.nextInt() exclusive upper bound */;
			this.exponentRange = exponentMax - exponentMin;
			this.exponentOffset = exponentMin;
		}

		public Decimal64 Next() => Decimal64.FromFixedPoint((/*NextLong*/((long)Generator.Next() << 32) | (uint)Generator.Next()) >> Generator.Next(mantissaMaxShift),
					-(Generator.Next(exponentRange) + exponentOffset));

		bool haveNextNextGaussian = false;
		double nextNextGaussian = Double.NaN;

		public double NextGaussianDouble()
		{
			// See Knuth, TAOCP, Vol. 2, 3rd edition, Section 3.4.1 Algorithm C.
			if (haveNextNextGaussian)
			{
				haveNextNextGaussian = false;
				return nextNextGaussian;
			}
			else
			{
				double v1, v2, s;
				do
				{
					v1 = 2 * Generator.NextDouble() - 1; // between -1 and 1
					v2 = 2 * Generator.NextDouble() - 1; // between -1 and 1
					s = v1 * v1 + v2 * v2;
				} while (s >= 1 || s == 0);
				double multiplier = Math.Sqrt(-2 * Math.Log(s) / s);
				nextNextGaussian = v2 * multiplier;
				haveNextNextGaussian = true;
				return v1 * multiplier;
			}
		}

		public Decimal64 NextGaussian() => Decimal64.FromDouble(NextGaussianDouble());

		public double NextGammaDouble(double shape, double scale)
		{
			if (shape <= 0 || Double.IsNaN(shape) || Double.IsInfinity(shape))
				throw new ArgumentOutOfRangeException(nameof(shape), shape, "The value must be finite and positive.");
			if (scale <= 0 || Double.IsNaN(scale) || Double.IsInfinity(scale))
				throw new ArgumentOutOfRangeException(nameof(scale), scale, "The value must be finite and positive.");

			if (shape < 1)
			{
				// [1]: p. 228, Algorithm GS

				while (true)
				{
					// Step 1:
					double u = Generator.NextDouble();
					double bGS = 1 + shape / Math.E;
					double p = bGS * u;

					if (p <= 1)
					{
						// Step 2:

						double x = Math.Pow(p, 1 / shape);
						double u2 = Generator.NextDouble();

						if (u2 > Math.Exp(-x))
						{
							// Reject
							continue;
						}
						else
						{
							return scale * x;
						}
					}
					else
					{
						// Step 3:

						double x = -1 * Math.Log((bGS - p) / shape);
						double u2 = Generator.NextDouble();

						if (u2 > Math.Pow(x, shape - 1))
						{
							// Reject
							continue;
						}
						else
						{
							return scale * x;
						}
					}
				}
			}

			// Now shape >= 1

			double d = shape - 0.333333333333333333;
			double c = 1 / (3 * Math.Sqrt(d));

			while (true)
			{
				double x = NextGaussianDouble();
				double v = (1 + c * x) * (1 + c * x) * (1 + c * x);

				if (v <= 0)
				{
					continue;
				}

				double x2 = x * x;
				double u = Generator.NextDouble();

				// Squeeze
				if (u < 1 - 0.0331 * x2 * x2)
				{
					return scale * d * v;
				}

				if (Math.Log(u) < 0.5 * x2 + d * (1 - v + Math.Log(v)))
				{
					return scale * d * v;
				}
			}
		}

		public Decimal64 NextGamma(double shape, double scale) => Decimal64.FromDouble(NextGammaDouble(shape, scale));
	}
}
