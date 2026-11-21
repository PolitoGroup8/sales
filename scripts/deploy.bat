@echo off
setlocal

REM Move to the repository root (parent of the scripts folder)
pushd "%~dp0.."

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

echo [ERRORE] Parametro non valido: "%MODE%"
echo Uso corretto: deploy.bat [local^|prod^|down]
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
docker compose -f docker-compose.prod.yaml up -d --build
if %errorlevel% neq 0 (
    echo [ERRORE] Errore durante il deploy in produzione con Docker Compose!
    goto ERROR
)

echo.
echo === Completato! Ambiente di PRODUZIONE avviato ===
echo - Applicazione su: http://localhost:8080
echo - Stop:            scripts\deploy.bat down
goto END


:RUN_DOWN
echo.
echo [DOWN] Arresto dei container prod e local ed eliminazione dei volumi...
docker compose -f docker-compose.prod.yaml down -v
docker compose down -v
echo.
echo Container fermati e volumi eliminati.
goto END


:ERROR
popd
pause
exit /b 1

:END
popd
endlocal