using System;
using System.Collections.Generic;
using System.Text;

namespace EPAM.Deltix.DFP.Benchmark
{
	internal class RandomDecimalsGenerator
	{
		readonly Random generator;
		Decimal64 x = Decimal64.NaN;
		int xExp = 0;
		Decimal64 y = Decimal64.NaN;
		int yExp = 0;

		readonly int mantissaMaxShift;
		readonly int exponentRange;
		readonly int exponentOffset;

		private static readonly int TwiceOfMaxSignificandDigits = Decimal64.MaxSignificandDigits * 2;
		private static readonly int HalfOfMaxSignificandDigits = Decimal64.MaxSignificandDigits / 2;

		public RandomDecimalsGenerator() : this((int)DateTime.Now.Ticks)
		{
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
				throw new ArgumentException("The random argument is null.");
			if (mantissaMinBits < 1 || 64 < mantissaMinBits)
				throw new ArgumentException("The mantissaMinBits(=" + mantissaMinBits + ") must lie in [1..64] range");
			if (exponentMin < Decimal64.MinExponent || Decimal64.MaxExponent < exponentMin)
				throw new ArgumentException("The exponentMin(=" + exponentMin + ") must lie in [" +
					Decimal64.MinExponent + ".." + Decimal64.MaxExponent + "] range.");
			if (exponentMax < Decimal64.MinExponent || Decimal64.MaxExponent < exponentMax)
				throw new ArgumentException("The exponentMax(=" + exponentMax + ") must lie in [" +
					Decimal64.MinExponent + ".." + Decimal64.MaxExponent + "] range.");
			if (exponentMax <= exponentMin)
				throw new ArgumentException("The exponentMin(=" + exponentMin +
					") must be less than the exponentMax(=" + exponentMax + ".");

			this.generator = generator;
			this.mantissaMaxShift = 64 - mantissaMinBits + 1 /*  for random.nextInt() exclusive upper bound */;
			this.exponentRange = exponentMax - exponentMin;
			this.exponentOffset = exponentMin;
		}

		public void MakeNextPair()
		{
			NextX();
			NextY();
		}

		private long NextLong()
		{
			return ((long)generator.Next(4) << 62) + ((long)generator.Next() << 31) + (long)generator.Next();
		}

		public Decimal64 NextX()
		{
			xExp = generator.Next(exponentRange) + exponentOffset;
			return x = Decimal64.FromFixedPoint(NextLong() >> generator.Next(mantissaMaxShift), -xExp);
		}

		public Decimal64 NextY()
		{
			yExp = generator.Next(exponentRange) + exponentOffset;
			return y = Decimal64.FromFixedPoint(NextLong() >> generator.Next(mantissaMaxShift), -yExp);
		}

		public Decimal64 X => x;

		public int XExp => xExp;

		public Decimal64 Y => y;

		public int YExp => yExp;

		public override string ToString()
		{
			return "RandomDecimalsGenerator{" +
				"x=" + x.ToScientificString() +
				", xExp=" + xExp +
				", y=" + y.ToScientificString() +
				", yExp=" + yExp +
				'}';
		}
	}
}
