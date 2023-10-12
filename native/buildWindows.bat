mkdir buildi386													|| goto :error
cd buildi386													|| goto :error
cmake -A Win32 -T ClangCL -DVERSION_SUFFIX=%1 ../				|| goto :error
cmake --build . --config Release								|| goto :error
cmake --install . --config Release --prefix ./installation		|| goto :error
mkdir ..\binmath\Release\windows\i386							|| goto :error
move /y Release\*math*.dll ..\binmath\Release\windows\i386		|| goto :error
mkdir ..\bin\Release\windows\i386								|| goto :error
move /y Release\*.dll ..\bin\Release\windows\i386				|| goto :error
mkdir ..\binDemo\Release\windows\i386							|| goto :error
move /y Release\*.exe ..\binDemo\Release\windows\i386			|| goto :error

cd ..															|| goto :error

mkdir buildamd64												|| goto :error
cd buildamd64													|| goto :error
cmake -A x64 -T ClangCL -DVERSION_SUFFIX=%1 ../					|| goto :error
cmake --build . --config Release								|| goto :error
cmake --install . --config Release --prefix ./installation		|| goto :error
mkdir ..\binmath\Release\windows\amd64							|| goto :error
move /y Release\*math*.dll ..\binmath\Release\windows\amd64		|| goto :error
mkdir ..\bin\Release\windows\amd64								|| goto :error
move /y Release\*.dll ..\bin\Release\windows\amd64				|| goto :error
mkdir ..\binDemo\Release\windows\amd64							|| goto :error
move /y Release\*.exe ..\binDemo\Release\windows\amd64			|| goto :error
cd ..															|| goto :error

goto :EOF

:error
echo Failed with error #%errorlevel%.
exit /b %errorlevel%
