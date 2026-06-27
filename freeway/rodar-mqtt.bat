@echo off
setlocal enabledelayedexpansion

set MVN="C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd"
set PROJ=%~dp0

echo Compilando...
%MVN% -f "%PROJ%pom.xml" install -pl freeway-common,freeway-mqtt -am -q

set M2=%USERPROFILE%\.m2\repository
set CP=%PROJ%freeway-common\target\freeway-common-1.0.jar;%PROJ%freeway-mqtt\target\freeway-mqtt-1.0.jar;%M2%\org\eclipse\paho\org.eclipse.paho.client.mqttv3\1.2.5\org.eclipse.paho.client.mqttv3-1.2.5.jar;%M2%\com\google\code\gson\gson\2.10.1\gson-2.10.1.jar

echo.
echo MQTT usa broker publico: broker.hivemq.com:1883 (requer internet)
echo.
echo Escolha:
echo   1 - Iniciar SERVIDOR
echo   2 - Iniciar CLIENTE
set /p op="Opcao: "

if "%op%"=="1" (
    echo Iniciando servidor MQTT...
    java -cp "%CP%" freeway.mqtt.Server
)
if "%op%"=="2" (
    echo Iniciando cliente MQTT...
    java -cp "%CP%" freeway.mqtt.Client
)
pause
