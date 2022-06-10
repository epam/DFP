using EPAM.Deltix.Utilities;
using System;
using System.IO;
using System.Runtime.InteropServices;
#if NET40
using System.Diagnostics;
using System.Linq;
using System.Text.RegularExpressions;
#endif

namespace EPAM.Deltix.DFPMath
{
	internal static class NativeMathImplLoader
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

				var varMapper = new VariablesMapper(typeof(NativeMathImplLoader));

				var unpackEnvVarName = varMapper.PackageLast.ToUpperInvariant() + "_UNPACK_ROOT";
				var unpackPath = Environment.GetEnvironmentVariable(unpackEnvVarName);
				if (unpackPath == null)
					unpackPath = "$(TEMP)/$(PACKAGE)/$(VERSION)/$(ARCH)";

				var loader = ResourceLoader
					.From($"EPAM.Deltix.DFPMath.{varMapper.Os}.{varMapper.Arch}.*")
					.To(varMapper.Substitute(unpackPath))
					// .LowercasePathOnLinux(false)
					.TryRandomFallbackSubDirectory(true)
					.Load();

				isLoaded = true;
			}
		}
	}
}
