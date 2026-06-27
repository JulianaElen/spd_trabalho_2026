@echo off
setlocal

set MVN="C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd"
set PROJ=%~dp0

echo Compilando...
%MVN% -f "%PROJ%pom.xml" install -pl freeway-common,freeway-sockets -am -q

set CP=%PROJ%freeway-common\target\freeway-common-1.0.jar;%PROJ%freeway-sockets\target\freeway-sockets-1.0.jar

echo.
echo Escolha:
echo   1 - Iniciar SERVIDOR
echo   2 - Iniciar CLIENTE (conecta em localhost)
set /p op="Opcao: "

if "%op%"=="1" (
    echo Iniciando servidor na porta 5000...
    java -cp "%CP%" freeway.sockets.Server
)
if "%op%"=="2" (
    set /p host="Host do servidor [localhost]: "
    if "!host!"=="" set host=localhost
    echo Iniciando cliente...
    java -cp "%CP%" freeway.sockets.Client !host!
)
pause
