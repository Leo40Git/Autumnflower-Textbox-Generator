@echo off

REM This script requires Oxipng to be on your PATH.
REM Download it here: https://github.com/shssoichiro/oxipng/releases/latest

setlocal
REM Modify this variable to pass options to Oxipng
set OXIPNG_OPTIONS=-a -s

pushd %~dp0
call :optimize_dir scratch
call :optimize_dir scratch_ex
popd

endlocal
exit /b 0

:optimize_dir dirname
pushd %1
echo === Optimizing %cd% and subdirectories...
oxipng %OXIPNG_OPTIONS% -r *
echo === Done with  %cd% and subdirectories
popd
