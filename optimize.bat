@echo off

REM This script requires Oxipng to be on your PATH.
REM Download it here: https://github.com/shssoichiro/oxipng/releases/latest

setlocal
REM Modify this variable to pass options to Oxipng
set OXIPNG_OPTIONS=-a -s

pushd %~dp0
call :optimize_dir src\main\resources
call :optimize_dir game_autumnflower
call :optimize_dir ext_autumnflower_addons
popd

endlocal
pause
exit /b 0
goto:eof

:optimize_dir dirname
pushd %1
echo === Optimizing %cd% and subdirectories...
oxipng %OXIPNG_OPTIONS% -r *
echo === Done with  %cd% and subdirectories
popd
goto:eof
