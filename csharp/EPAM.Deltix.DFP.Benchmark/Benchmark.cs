using System;
using BenchmarkDotNet.Attributes;
using BenchmarkDotNet.Running;

using BID_UINT64 = System.UInt64;

namespace EPAM.Deltix.DFP.Benchmark
{
	public class Benchmark
	{
		private Decimal64 a1, b1;
		private Decimal a2, b2;

		[GlobalSetup]
		public void Setup()
		{
			Random random = new Random();
			Double a = random.NextDouble();
			Double b = random.NextDouble();
			a1 = Decimal64.FromDouble(a);
			b1 = Decimal64.FromDouble(b);
			a2 = (Decimal) a;
			b2 = (Decimal) b;
		}

		[Benchmark]
		public Decimal64 AddDecimal64()
		{
			return a1 + b1;
		}

		[Benchmark]
		public Decimal64 AddDecimal64Native()
		{
			return Decimal64.FromUnderlying(NativeImpl.add2(a1.Bits, b1.Bits));
		}

		[Benchmark]
		public Decimal AddSystem()
		{
			return a2 + b2;
		}

		[Benchmark]
		public Decimal64 MultiplyDecimal64()
		{
			return a1 * b1;
		}

		[Benchmark]
		public Decimal MultiplySystem()
		{
			return a2 * b2;
		}

		[Benchmark]
		public Decimal64 DivisionDecimal64()
		{
			return a1 / b1;
		}

		[Benchmark]
		public Decimal DivisionSystem()
		{
			return a2 / b2;
		}

		public static void Main(String[] args)
		{
			var summary = BenchmarkRunner.Run(typeof(Benchmark).Assembly);
		}
	}
}
