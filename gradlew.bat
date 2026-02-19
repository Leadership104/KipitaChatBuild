@ECHO OFF
SETLOCAL

REM Lightweight text-only Gradle launcher for environments where committing
REM binary wrapper artifacts is not allowed.
where gradle >NUL 2>NUL
IF %ERRORLEVEL% EQU 0 (
  gradle %*
  EXIT /B %ERRORLEVEL%
)

echo ERROR: 'gradle' command not found in PATH. 1>&2
echo Install Gradle 9.2.1 (as set in gradle/wrapper/gradle-wrapper.properties) and retry. 1>&2
EXIT /B 1
