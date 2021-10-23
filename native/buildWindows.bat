rem mkdir buildx86
rem cd buildx86
rem cmake -G "Visual Studio 16 2019" -A Win32 -DAPI_PREFIX=%1 ../
rem MSBuild /p:PlatformToolset=ClangCL /p:Platform=Win32 /p:Configuration=Release /t:Rebuild ./native.sln
rem mkdir ..\bin\Release\Windows\x86
rem copy /y Release\*.dll ..\bin\Release\Windows\x86
rem mkdir ..\binDemo\Release\Windows\x86
rem copy /y EXAMPLES\Release\*.exe ..\binDemo\Release\Windows\x86

rem cd ..

mkdir buildamd64
cd buildamd64
cmake -G "Visual Studio 16 2019" -A x64 -DAPI_PREFIX=%1 ../
rem MSBuild /p:PlatformToolset=ClangCL /p:Platform=x64 /p:Configuration=Release /t:Rebuild ./native.sln
rem mkdir ..\bin\Release\Windows\amd64
rem copy /y Release\*.dll ..\bin\Release\Windows\amd64
rem mkdir ..\binDemo\Release\Windows\amd64
rem copy /y EXAMPLES\Release\*.exe ..\binDemo\Release\Windows\amd64
rem cd ..
