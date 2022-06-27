using EPAM.Deltix.Utilities;
using System;

namespace EPAM.Deltix.DFP
{
	internal static class NativeImplLoader
	{
		internal static bool isLoaded = false;
		internal static object isLoadedLock = new object();
		internal static void Load()
		{
			if (isLoaded)
				return;
			lock (isLoadedLock)
			{
				if (isLoaded)
					return;

				var varMapper = new VariablesMapper(typeof(NativeImplLoader));

				var unpackEnvVarName = varMapper.PackageLast.ToUpperInvariant() + "_UNPACK_ROOT";
				var unpackPath = Environment.GetEnvironmentVariable(unpackEnvVarName);
				if (unpackPath == null)
					unpackPath = "$(TEMP)/$(PACKAGE)/$(VERSION)/$(ARCH)";

				var loader = ResourceLoader
					.From("$(PACKAGE).$(OS).$(ARCH).*")
					.To(varMapper.Substitute(unpackPath))
					.Load();

				isLoaded = true;
			}
		}
	}
}
