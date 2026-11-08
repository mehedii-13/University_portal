<# : batch portion
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV Vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV Vars
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the input commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to pause at the end of this script
@REM MAVEN_OPTS - parameters to pass to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
if "%JAVA_HOME%"=="" (
    if exist "%USERPROFILE%\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2" (
        set "JAVA_HOME=%USERPROFILE%\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
    )
)
@REM set pc to monitor at runtime
@REM set command line args to env variable
set MAVEN_CMD_LINE_ARGS=%*

@REM set %~d0 = drive letter only
set MAVEN_PROJECTBASEDIR=%~dp0
:strip
if "%MAVEN_PROJECTBASEDIR:~-1%"=="" goto done
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%& goto strip
:done

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

FOR /F "usebackq tokens=1,2 delims==" %%A IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO (
    IF "%%A"=="wrapperUrl" SET DOWNLOAD_URL=%%B
)

@REM Check wrapper jar or fallback to installed maven
if exist %WRAPPER_JAR% goto run

@REM Fallback 1: Look for pre-installed Maven in .m2 wrapper dists
for /f "delims=" %%M in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\mvn.cmd" 2^>nul') do (
    call "%%M" %MAVEN_CMD_LINE_ARGS%
    exit /b %ERRORLEVEL%
)

@REM Fallback 2: Look for mvn in PATH
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    call mvn %MAVEN_CMD_LINE_ARGS%
    exit /b %ERRORLEVEL%
)

echo Couldn't find %WRAPPER_JAR%, attempting to download...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')}" 2>nul

if exist %WRAPPER_JAR% goto run

:run
@REM Provide a default value for JAVA_HOME if not set
if "%JAVA_HOME%"=="" (
    for /f "delims=" %%i in ('where java 2^>nul') do (
        set "JAVA_EXE=%%i"
        goto found_java
    )
) else (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

:found_java
if not exist "%JAVA_EXE%" (
    echo Error: JAVA_HOME is not defined correctly or java cannot be found in PATH. >&2
    exit /b 1
)

"%JAVA_EXE%" %MAVEN_OPTS% -jar %WRAPPER_JAR% %MAVEN_CMD_LINE_ARGS%

