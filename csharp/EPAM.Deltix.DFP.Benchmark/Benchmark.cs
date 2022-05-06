using System;
using BenchmarkDotNet.Attributes;
using BenchmarkDotNet.Running;
using EPAM.Deltix.DFP.Test;
using BID_UINT64 = System.UInt64;

namespace EPAM.Deltix.DFP.Benchmark
{
	public class Benchmark
	{
		private Decimal64[] sumValues;
		private Decimal64[] prodValues;

		[GlobalSetup]
		public void Setup()
		{
			var random = new RandomDecimalsGenerator(42 * 42 * 42 * 42 * 42);
			sumValues = new Decimal64[1004];
			prodValues = new Decimal64[sumValues.Length];
			for (var i = 0; i < sumValues.Length; i++)
			{
				sumValues[i] = random.Next();
				if (random.Generator.Next(2) > 0)
					sumValues[i] = -sumValues[i];
				prodValues[i] = Decimal64.FromDouble(Math.Exp(random.NextGaussianDouble()));
				if (random.Generator.Next(2) > 0)
					prodValues[i] = -prodValues[i];
			}
		}

		[Benchmark]
		public Decimal64 Add2()
		{
			Decimal64 result = Decimal64.Zero;
			for (var i = 0; i < 1000; i++)
				result = result.Add(sumValues[i]);
			return result;
		}

		[Benchmark]
		public BID_UINT64 Add2Native()
		{
			BID_UINT64 result = Decimal64.Zero.Bits;
			for (var i = 0; i < 1000; i++)
				result = NativeImpl.add2(result, sumValues[i].Bits);
			return result;
		}

		[Benchmark]
		public Decimal64 Add4()
		{
			Decimal64 result = Decimal64.Zero;
			for (var i = 0; i < 1000; i++)
				result = result.Add(sumValues[i], sumValues[i + 1], sumValues[i + 2]);
			return result;
		}

		[Benchmark]
		public BID_UINT64 Add4Native()
		{
			BID_UINT64 result = Decimal64.Zero.Bits;
			for (var i = 0; i < 1000; i++)
				result = NativeImpl.add4(result, sumValues[i].Bits, sumValues[i + 1].Bits, sumValues[i + 2].Bits);
			return result;
		}

		[Benchmark]
		public Decimal64 Multiply2()
		{
			Decimal64 result = Decimal64.One;
			for (var i = 0; i < 1000; i++)
				result = result.Multiply(prodValues[i]);
			return result;
		}

		[Benchmark]
		public BID_UINT64 Multiply2Native()
		{
			BID_UINT64 result = Decimal64.One.Bits;
			for (var i = 0; i < 1000; i++)
				result = NativeImpl.multiply2(result, prodValues[i].Bits);
			return result;
		}

		[Benchmark]
		public Decimal64 Multiply4()
		{
			Decimal64 result = Decimal64.One;
			for (var i = 0; i < 1000; i++)
				result = result.Multiply(prodValues[i], prodValues[i + 1], prodValues[i + 2]);
			return result;
		}

		[Benchmark]
		public BID_UINT64 Multiply4Native()
		{
			BID_UINT64 result = Decimal64.One.Bits;
			for (var i = 0; i < 1000; i++)
				result = NativeImpl.multiply4(result, prodValues[i].Bits, prodValues[i + 1].Bits, prodValues[i + 2].Bits);
			return result;
		}

		[Benchmark]
		public Decimal64 Divide()
		{
			Decimal64 result = Decimal64.One;
			for (var i = 0; i < 1000; i++)
				result = result.Divide(prodValues[i]);
			return result;
		}

		[Benchmark]
		public BID_UINT64 DivideNative()
		{
			BID_UINT64 result = Decimal64.One.Bits;
			for (var i = 0; i < 1000; i++)
				result = NativeImpl.divide(result, prodValues[i].Bits);
			return result;
		}

		public static void Main(String[] args)
		{
			var summary = BenchmarkRunner.Run(typeof(Benchmark).Assembly);
		}
	}
}
