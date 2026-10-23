@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_HOME=%DIRNAME%
set APP_BASE_NAME=%~n0
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if not "%JAVA_HOME%" == "" (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

if exist "%JAVA_EXE%" goto init

echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
exit /b 1

:init
if not "%JAVA_HOME%" == "" (
    set JAVA_HOME=%JAVA_HOME:"=%
)
if exist "%JAVA_HOME%\bin\java.exe" goto okJava

echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
exit /b 1

:okJava
set DEFAULT_JVM_OPTS=-Xmx64m -Xms256m
if "%GRADLE_OPTS%" == "" set GRADLE_OPTS=%DEFAULT_JVM_OPTS%
if "%JAVA_OPTS%" == "" set JAVA_OPTS=%DEFAULT_JVM_OPTS%

"%JAVA_EXE%" %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
