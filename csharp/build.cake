#tool nuget:?package=NUnit.ConsoleRunner&version=3.7.0
#addin "Cake.Powershell"
#addin "Cake.FileHelpers"

//////////////////////////////////////////////////////////////////////
// ARGUMENTS
//////////////////////////////////////////////////////////////////////

var target = Argument("target", "Default");
var configuration = Argument("configuration", "Release");

//////////////////////////////////////////////////////////////////////
// TASKS
//////////////////////////////////////////////////////////////////////

Task("Native-Rename")
    .Does(() =>
{
	var files = GetFiles("../native/bin/Release/**/*.zst");
	foreach(var file in files)
	{
		MoveFile(file, file.ToString().Replace('.', '_'));
	}
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
    DotNetCoreBuild("./EPAM.Deltix.DFP/EPAM.Deltix.DFP.Math.csproj", buildSettings);

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
