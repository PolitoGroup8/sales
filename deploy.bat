@echo off
setlocal

set "MODE=%~1"
if "%MODE%"=="" set "MODE=local"

echo === Modalita selezionata: %MODE% ===

REM Docker deve essere in esecuzione
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERRORE] Docker non e' in esecuzione. Avvia Docker Desktop e riprova.
    goto ERROR
)

if /i "%MODE%"=="local" goto CHECK_ENV
if /i "%MODE%"=="prod"  goto CHECK_ENV
if /i "%MODE%"=="down"  goto RUN_DOWN
if /i "%MODE%"=="logs"  goto RUN_LOGS
if /i "%MODE%"=="reset" goto RUN_RESET

echo [ERRORE] Parametro non valido: "%MODE%"
echo Uso corretto: run.bat [local^|prod^|down^|logs^|reset]
goto ERROR


:CHECK_ENV
if not exist ".env" (
    echo [ERRORE] File .env mancante. Copia .env.example in .env e compila i valori.
    goto ERROR
)
if /i "%MODE%"=="local" goto RUN_LOCAL
goto RUN_PROD


:RUN_LOCAL
echo.
echo [LOCAL] Avvio dell'infrastruttura di sviluppo (PostgreSQL e PgAdmin)...
docker compose up -d
if %errorlevel% neq 0 (
    echo [ERRORE] Errore durante l'avvio di Docker Compose!
    goto ERROR
)

echo.
echo === Infrastruttura LOCALE avviata con successo! ===
echo - Database pronto su: localhost:5432
echo - PgAdmin disponibile su: http://localhost:5050
echo Ora puoi avviare l'applicazione Spring Boot da IntelliJ.
goto END


:RUN_PROD
echo.
echo [PROD] Build dell'immagine e avvio dei container...
echo        (la compilazione Gradle avviene dentro il Dockerfile)
docker compose -f docker-compose.prod.yml up -d --build
if %errorlevel% neq 0 (
    echo [ERRORE] Errore durante il deploy in produzione con Docker Compose!
    goto ERROR
)

echo.
echo === Completato! Ambiente di PRODUZIONE avviato ===
echo - Applicazione su: http://localhost:8080
echo - Log:             run.bat logs
echo - Stop:            run.bat down
goto END


:RUN_DOWN
echo.
echo [DOWN] Arresto dei container prod e local...
docker compose -f docker-compose.prod.yml down
docker compose down
echo.
echo Container fermati. I volumi con i dati sono stati mantenuti.
goto END


:RUN_LOGS
docker compose -f docker-compose.prod.yml logs -f sales
goto END


:RUN_RESET
echo.
echo [RESET] Elimino container e volumi di sviluppo (i dati andranno persi)...
set /p CONFIRM="Sei sicuro? (s/n): "
if /i not "%CONFIRM%"=="s" (
    echo Operazione annullata.
    goto END
)
docker compose down -v
if %errorlevel% neq 0 (
    echo [ERRORE] Errore durante il reset!
    goto ERROR
)
echo.
echo Reset completato. Rilancia "run.bat local" per ripartire con un DB pulito.
goto END


:ERROR
pause
exit /b 1

:END
endlocal