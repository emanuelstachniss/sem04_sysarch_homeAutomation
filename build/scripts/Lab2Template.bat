@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Lab2Template startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and LAB2_TEMPLATE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\Lab2Template-1.0-SNAPSHOT.jar;%APP_HOME%\lib\json-20250107.jar;%APP_HOME%\lib\akka-actor-testkit-typed_3-2.10.5.jar;%APP_HOME%\lib\akka-actor-typed_3-2.10.5.jar;%APP_HOME%\lib\akka-stream-alpakka-mqtt_3-9.0.1.jar;%APP_HOME%\lib\akka-grpc-runtime_3-2.5.5.jar;%APP_HOME%\lib\akka-stream_3-2.10.5.jar;%APP_HOME%\lib\akka-slf4j_3-2.10.5.jar;%APP_HOME%\lib\akka-testkit_3-2.10.5.jar;%APP_HOME%\lib\akka-http_3-10.7.1.jar;%APP_HOME%\lib\akka-pki_3-2.10.5.jar;%APP_HOME%\lib\akka-discovery_3-2.10.5.jar;%APP_HOME%\lib\akka-actor_3-2.10.5.jar;%APP_HOME%\lib\logback-classic-1.5.18.jar;%APP_HOME%\lib\akka-http2-support_3-10.5.3.jar;%APP_HOME%\lib\akka-grpc-runtime_2.13-2.5.5.jar;%APP_HOME%\lib\scalapb-runtime_3-0.11.17.jar;%APP_HOME%\lib\akka-http-core_3-10.7.1.jar;%APP_HOME%\lib\lenses_3-0.11.17.jar;%APP_HOME%\lib\scala-collection-compat_3-2.12.0.jar;%APP_HOME%\lib\akka-parsing_3-10.7.1.jar;%APP_HOME%\lib\scala3-library_3-3.3.4.jar;%APP_HOME%\lib\scalapb-runtime_2.13-0.11.17.jar;%APP_HOME%\lib\akka-stream_2.13-2.10.5.jar;%APP_HOME%\lib\akka-http_2.13-10.7.1.jar;%APP_HOME%\lib\akka-http-core_2.13-10.7.1.jar;%APP_HOME%\lib\akka-pki_2.13-2.10.5.jar;%APP_HOME%\lib\akka-discovery_2.13-2.10.5.jar;%APP_HOME%\lib\lenses_2.13-0.11.17.jar;%APP_HOME%\lib\scala-collection-compat_2.13-2.12.0.jar;%APP_HOME%\lib\akka-actor_2.13-2.10.5.jar;%APP_HOME%\lib\akka-parsing_2.13-10.7.1.jar;%APP_HOME%\lib\scala-library-2.13.15.jar;%APP_HOME%\lib\grpc-netty-shaded-1.72.0.jar;%APP_HOME%\lib\grpc-stub-1.71.0.jar;%APP_HOME%\lib\config-1.4.3.jar;%APP_HOME%\lib\slf4j-api-2.0.17.jar;%APP_HOME%\lib\akka-protobuf-v3_3-2.10.5.jar;%APP_HOME%\lib\reactive-streams-1.0.4.jar;%APP_HOME%\lib\org.eclipse.paho.client.mqttv3-1.2.5.jar;%APP_HOME%\lib\logback-core-1.5.18.jar;%APP_HOME%\lib\grpc-protobuf-1.71.0.jar;%APP_HOME%\lib\proto-google-common-protos-2.51.0.jar;%APP_HOME%\lib\protobuf-java-3.25.5.jar;%APP_HOME%\lib\grpc-util-1.72.0.jar;%APP_HOME%\lib\grpc-core-1.72.0.jar;%APP_HOME%\lib\grpc-protobuf-lite-1.71.0.jar;%APP_HOME%\lib\grpc-context-1.72.0.jar;%APP_HOME%\lib\grpc-api-1.72.0.jar;%APP_HOME%\lib\guava-33.3.1-jre.jar;%APP_HOME%\lib\gson-2.11.0.jar;%APP_HOME%\lib\error_prone_annotations-2.30.0.jar;%APP_HOME%\lib\perfmark-api-0.27.0.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.24.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\asn-one-0.6.0.jar;%APP_HOME%\lib\annotations-4.1.1.4.jar;%APP_HOME%\lib\failureaccess-1.0.2.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\checker-qual-3.43.0.jar;%APP_HOME%\lib\j2objc-annotations-3.0.0.jar;%APP_HOME%\lib\akka-protobuf-v3_2.13-2.10.5.jar


@rem Execute Lab2Template
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %LAB2_TEMPLATE_OPTS%  -classpath "%CLASSPATH%" at.fhv.sysarch.lab2.HomeAutomationSystem %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable LAB2_TEMPLATE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%LAB2_TEMPLATE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
