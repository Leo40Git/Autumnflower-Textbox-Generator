@echo off
echo This script requires Oxipng to be on your PATH.
echo Download it here: https://github.com/shssoichiro/oxipng/releases/latest
pause

setlocal
pushd scratch
call :processFolder
popd
pushd scratch_ex
call :processFolder
popd
exit /b 0

:processFolder
echo Processing folder %CD%
oxipng --strip safe *.png
for /D %%d in (*) do (
    pushd %%d
    call :processFolder
    popd
)
endlocal
