@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

IF EXIST out.txt DEL /F out.txt
javac *.java>>out.txt
SET /p output=<out.txt
FOR /f "tokens=* delims= " %%a IN ("!output!") DO SET output=%%a

IF NOT '!output!'=='' GOTO Error

START java Peer 1001
START java Peer 1002 localhost 1001
START java Peer 1003 localhost 1001
GOTO Done

:Error
echo Compiler Error:
echo !output!
GOTO :eof

:Done
echo Done
