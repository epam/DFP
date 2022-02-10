mkdir buildx86
cd buildx86
cmake -G "Visual Studio 16 2019" -A Win32 -DVERSION_SUFFIX=%1 ../
MSBuild /p:PlatformToolset=ClangCL /p:Platform=Win32 /p:Configuration=Release /t:Rebuild ./native.sln
mkdir ..\binmath\Release\Windows\x86
move /y Release\*math*.dll ..\binmath\Release\Windows\x86
mkdir ..\bin\Release\Windows\x86
move /y Release\*.dll ..\bin\Release\Windows\x86
mkdir ..\binDemo\Release\Windows\x86
move /y EXAMPLES\Release\*.exe ..\binDemo\Release\Windows\x86

cd ..

mkdir buildamd64
cd buildamd64
cmake -G "Visual Studio 16 2019" -A x64 -DVERSION_SUFFIX=%1 ../
MSBuild /p:PlatformToolset=ClangCL /p:Platform=x64 /p:Configuration=Release /t:Rebuild ./native.sln
mkdir ..\binmath\Release\Windows\amd64
move /y Release\*math*.dll ..\binmath\Release\Windows\amd64
mkdir ..\bin\Release\Windows\amd64
move /y Release\*.dll ..\bin\Release\Windows\amd64
mkdir ..\binDemo\Release\Windows\amd64
move /y EXAMPLES\Release\*.exe ..\binDemo\Release\Windows\amd64
cd ..
