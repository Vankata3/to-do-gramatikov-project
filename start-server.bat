@echo off
echo Building Todo Java Server...
call mvn clean package -q

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting Todo Java Server...
echo Open http://localhost:8080 in your browser
echo.
java -jar target/todo-java-1.0.0.jar

pause
