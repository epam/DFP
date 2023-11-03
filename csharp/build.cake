#tool nuget:?package=NUnit.ConsoleRunner&version=3.7.0
#addin nuget:?package=Cake.Powershell&version=1.0.1
#addin nuget:?package=Cake.FileHelpers&version=4.0.1

//////////////////////////////////////////////////////////////////////
// ARGUMENTS
//////////////////////////////////////////////////////////////////////

var target = Argument("target", "Default");
var configuration = Argument("configuration", "Release");

//////////////////////////////////////////////////////////////////////
// TASKS
//////////////////////////////////////////////////////////////////////

using System.Linq;

void NativeRename(string srcPath, string dstPath)
{
    foreach(var file in GetFiles("../native/" + srcPath + "/**/*.dylib.zst")
         .Union(GetFiles("../native/" + srcPath + "/**/*.so.zst"))
         .Union(GetFiles("../native/" + srcPath + "/**/*.dll.zst")))
    {
        var fileDirectory = file.GetDirectory().ToString();
        if (fileDirectory.EndsWith("/linux/amd64", StringComparison.OrdinalIgnoreCase))
            continue;

        var toDirectory = fileDirectory;
        if (toDirectory.EndsWith("/linux/musl-gcc", StringComparison.OrdinalIgnoreCase))
            toDirectory = toDirectory.Substring(0, toDirectory.Length - 8) + "amd64";

        toDirectory = toDirectory.Replace("/" + srcPath + "/", "/" + dstPath + "/");
        CreateDirectory(toDirectory);
        CopyFile(file, toDirectory + "/" + file.GetFilename().ToString().Replace('.', '_'));
    }
}

Task("Native-Rename")
    .Does(() =>
{
    NativeRename("bin/Release", "binCs/Release");
    NativeRename("binmath/Release", "binmathCs/Release");
});

Task("Clean")
    .Does(() =>
{
    DotNetCoreClean("./EPAM.Deltix.DFP.sln");
});

Task("Restore-NuGet-Packages")
    .IsDependentOn("Clean")
    .Does(() =>
{
    DotNetCoreRestore("./EPAM.Deltix.DFP.sln");
});

Task("Build")
    .IsDependentOn("Restore-NuGet-Packages")
    .IsDependentOn("Native-Rename")
    .Does(() =>
{
    var buildSettings = new DotNetCoreBuildSettings {
        Configuration = configuration,
        NoRestore = true,
        NoDependencies = true,
    };

    if (!IsRunningOnWindows())
        buildSettings.Framework = "netstandard2.0";

    DotNetCoreBuild("./EPAM.Deltix.DFP/EPAM.Deltix.DFP.csproj", buildSettings);
    DotNetCoreBuild("./EPAM.Deltix.DFPMath/EPAM.Deltix.DFPMath.csproj", buildSettings);

    if (!IsRunningOnWindows())
        buildSettings.Framework = "netcoreapp3.1";

    DotNetCoreBuild("./EPAM.Deltix.DFP.Benchmark/EPAM.Deltix.DFP.Benchmark.csproj", buildSettings);
    DotNetCoreBuild("./EPAM.Deltix.DFP.Demo/EPAM.Deltix.DFP.Demo.csproj", buildSettings);
    DotNetCoreBuild("./EPAM.Deltix.DFP.Test/EPAM.Deltix.DFP.Test.csproj", buildSettings);
});

Task("Run-Unit-Tests")
    .IsDependentOn("Build")
    .Does(() =>
{
    var buildSettings = new DotNetCoreTestSettings()
    {
        Configuration = configuration,
        NoRestore = true,
		NoBuild = true
    };

    //settings.NoBuild = true;
    if (!IsRunningOnWindows())
         buildSettings.Framework = "netcoreapp3.1";

	Information("Running tests with .NET Core");
	DotNetCoreTest("./EPAM.Deltix.DFP.Test/EPAM.Deltix.DFP.Test.csproj", buildSettings);

    // Prevent NUnit tests from running on platforms without .NET 4.0
    var glob = $"./EPAM.Deltix.DFP.Test/bin/{configuration}/net40/EPAM.Deltix.DFP.Test.exe";
    if (IsRunningOnWindows() && GetFiles(glob).Count > 0)
    {
		Information("Running tests with NUnit & .NET Framework 4.0");
		NUnit3(glob);
    }
});

Task("Pack")
    .IsDependentOn("Build")
    .Does(() =>
{
    var settings = new DotNetCorePackSettings
    {
        Configuration = configuration,
        OutputDirectory = "./artifacts/"
    };
    DotNetCorePack(".", settings);
});

Task("Push")
    .IsDependentOn("Pack")
    .Does(() =>
{
    var url = (EnvironmentVariable("ARTIFACTORY_URL") ?? "https://artifactory.epam.com/artifactory") + "/api/nuget/" + (EnvironmentVariable("FEED_BASE_NAME") ?? "EPM-RTC-test") + "-net";
    var apiKey = (EnvironmentVariable("ARTIFACTORY_USER") ?? "") + ":" + (EnvironmentVariable("ARTIFACTORY_PASS") ?? "");
    foreach (var file in GetFiles("./artifacts/*.nupkg"))
    {
        DotNetCoreTool(".", "nuget", "push " + file.FullPath + " --source " + url + " --api-key " + apiKey);
    }
});

//////////////////////////////////////////////////////////////////////
// TASK TARGETS
//////////////////////////////////////////////////////////////////////

Task("Default")
    .IsDependentOn("Run-Unit-Tests");

//////////////////////////////////////////////////////////////////////
// EXECUTION
//////////////////////////////////////////////////////////////////////

RunTarget(target);
