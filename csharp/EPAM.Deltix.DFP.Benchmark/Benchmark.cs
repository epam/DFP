using System;
using BenchmarkDotNet.Attributes;
using BenchmarkDotNet.Running;

namespace EPAM.Deltix.DFP.Benchmark
{
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
		public void AddDecimal64()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = values[i] + values[i + 1];
			}
		}

		[Benchmark]
		public void MultiplyDecimal64()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = values[i] * values[i + 1];
			}
		}

		[Benchmark]
		public void DivisionDecimal64()
		{
			for (int i = 0; i < N; ++i)
			{
				var c = values[i] / values[i + 1];
			}
		}

		public static void Main(String[] args)
		{
			var summary = BenchmarkRunner.Run<Benchmark>();
		}
	}
}
