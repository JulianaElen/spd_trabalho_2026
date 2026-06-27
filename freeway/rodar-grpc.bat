@echo off
setlocal enabledelayedexpansion

set MVN="C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd"
set PROJ=%~dp0

echo Compilando...
%MVN% -f "%PROJ%pom.xml" install -pl freeway-common,freeway-grpc -am -q

set M2=%USERPROFILE%\.m2\repository
set G=io\grpc
set GV=1.63.0
set PV=3.25.3

set CP=%PROJ%freeway-common\target\freeway-common-1.0.jar
set CP=%CP%;%PROJ%freeway-grpc\target\freeway-grpc-1.0.jar
set CP=%CP%;%M2%\%G%\grpc-netty-shaded\%GV%\grpc-netty-shaded-%GV%.jar
set CP=%CP%;%M2%\%G%\grpc-protobuf\%GV%\grpc-protobuf-%GV%.jar
set CP=%CP%;%M2%\%G%\grpc-stub\%GV%\grpc-stub-%GV%.jar
set CP=%CP%;%M2%\com\google\protobuf\protobuf-java\%PV%\protobuf-java-%PV%.jar
set CP=%CP%;%M2%\%G%\grpc-api\%GV%\grpc-api-%GV%.jar
set CP=%CP%;%M2%\%G%\grpc-core\%GV%\grpc-core-%GV%.jar
set CP=%CP%;%M2%\%G%\grpc-protobuf-lite\%GV%\grpc-protobuf-lite-%GV%.jar
set CP=%CP%;%M2%\com\google\guava\guava\32.1.3-android\guava-32.1.3-android.jar
set CP=%CP%;%M2%\javax\annotation\javax.annotation-api\1.3.2\javax.annotation-api-1.3.2.jar

echo.
echo Escolha:
echo   1 - Iniciar SERVIDOR (porta 5001)
echo   2 - Iniciar CLIENTE (conecta em localhost)
set /p op="Opcao: "

if "%op%"=="1" (
    echo Iniciando servidor gRPC...
    java -cp "%CP%" freeway.grpc.Server
)
if "%op%"=="2" (
    set /p host="Host do servidor [localhost]: "
    if "!host!"=="" set host=localhost
    echo Iniciando cliente gRPC...
    java -cp "%CP%" freeway.grpc.Client !host!
)
pause
