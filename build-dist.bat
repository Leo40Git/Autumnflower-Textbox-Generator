@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Eclipse Foundation\jdk-17.0.0.35-hotspot\"
gradlew --no-daemon jpackageImage
endlocal
