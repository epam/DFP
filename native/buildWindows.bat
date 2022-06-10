mkdir buildi386
cd buildi386
cmake -G "Visual Studio 16 2019" -A Win32 -DVERSION_SUFFIX=%1 ../
MSBuild /p:PlatformToolset=ClangCL /p:Platform=Win32 /p:Configuration=Release /t:Rebuild ./native.sln
mkdir ..\binmath\Release\windows\i386
move /y Release\*math*.dll ..\binmath\Release\windows\i386
mkdir ..\bin\Release\windows\i386
move /y Release\*.dll ..\bin\Release\windows\i386
mkdir ..\binDemo\Release\windows\i386
move /y EXAMPLES\Release\*.exe ..\binDemo\Release\windows\i386

cd ..

mkdir buildamd64
cd buildamd64
cmake -G "Visual Studio 16 2019" -A x64 -DVERSION_SUFFIX=%1 ../
MSBuild /p:PlatformToolset=ClangCL /p:Platform=x64 /p:Configuration=Release /t:Rebuild ./native.sln
mkdir ..\binmath\Release\windows\amd64
move /y Release\*math*.dll ..\binmath\Release\windows\amd64
mkdir ..\bin\Release\windows\amd64
move /y Release\*.dll ..\bin\Release\windows\amd64
mkdir ..\binDemo\Release\windows\amd64
move /y EXAMPLES\Release\*.exe ..\binDemo\Release\windows\amd64
cd ..
