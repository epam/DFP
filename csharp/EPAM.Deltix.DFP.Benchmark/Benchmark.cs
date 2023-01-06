using System;
using BenchmarkDotNet.Attributes;
using BenchmarkDotNet.Running;

namespace EPAM.Deltix.DFP.Benchmark
{
	public static class Decimal64Managed
	{
		public static Decimal64 Add(Decimal64 a, Decimal64 b)
		{
			return Decimal64.FromUnderlying(Bid64Add.bid64_add(a.Bits, b.Bits));
		}

		public static Decimal64 Mul(Decimal64 a, Decimal64 b)
		{
			return Decimal64.FromUnderlying(Bid64Mul.bid64_mul(a.Bits, b.Bits));
		}

		public static Decimal64 Div(Decimal64 a, Decimal64 b)
		{
			return Decimal64.FromUnderlying(Bid64Div.bid64_div(a.Bits, b.Bits));
		}
	}

	public class Benchmark
	{
        [Params(509104287)]
        public int randomSeed { get; set; }

        [Params(1000_000)]
		public int N { get; set; }

		public Decimal64[] values;

		[GlobalSetup]
		public void Setup()
		{
			values = new Decimal64[N + 1];
			var generator = new RandomDecimalsGenerator(randomSeed);

			for (int i = 0; i < values.Length; ++i)
				values[i] = generator.NextX();
		}

		[Benchmark]
		public void AddNative()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = values[i] + values[i + 1];
			}
		}

		[Benchmark]
		public void MultiplyNative()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = values[i] * values[i + 1];
			}
		}

		[Benchmark]
		public void DivisionNative()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = values[i] / values[i + 1];
			}
		}

		[Benchmark]
		public void AddManaged()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = Decimal64Managed.Add(values[i], values[i + 1]);
			}
		}

		[Benchmark]
		public void MultiplyManaged()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = Decimal64Managed.Mul(values[i], values[i + 1]);
			}
		}

		[Benchmark]
		public void DivisionManaged()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = Decimal64Managed.Div(values[i], values[i + 1]);
			}
		}


		public static void Main(String[] args)
		{
			var summary = BenchmarkRunner.Run<Benchmark>();
		}
	}
}
