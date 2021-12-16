@echo off

REM This script requires Oxipng to be on your PATH.
REM Download it here: https://github.com/shssoichiro/oxipng/releases/latest

setlocal
set OXIPNG_OPTIONS=-a -s
pushd scratch
echo === Optimizing %cd% and subdirectories...
oxipng %OXIPNG_OPTIONS% -r *
popd
pushd scratch_ex
echo === Optimizing %cd% and subdirectories...
oxipng %OXIPNG_OPTIONS% -r *
popd
endlocal
