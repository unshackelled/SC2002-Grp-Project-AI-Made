@echo off
rem Compiles and runs this stage. Extra arguments are passed to the game, e.g.
rem   run.bat --echo < tests\victory.txt
cd /d "%~dp0"
if not exist out mkdir out
javac -d out -sourcepath src src\restaurantrush\Main.java || exit /b 1
java -cp out restaurantrush.Main %*
