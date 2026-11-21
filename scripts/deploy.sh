#!/usr/bin/env bash
# ------------------------------------------------------------
# deploy.sh - equivalent of deploy.bat (Linux / macOS)
# Usage: scripts/deploy.sh [local|prod|down]
# ------------------------------------------------------------

# Move to the repository root (parent of the scripts folder)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.." || exit 1

MODE="${1:-local}"
MODE="$(echo "$MODE" | tr '[:upper:]' '[:lower:]')"

echo "=== Modalita selezionata: $MODE ==="

# Docker deve essere in esecuzione
if ! docker info >/dev/null 2>&1; then
    echo "[ERRORE] Docker non e' in esecuzione. Avvia Docker e riprova."
    exit 1
fi

case "$MODE" in
    local|prod|down) ;;
    *)
        echo "[ERRORE] Parametro non valido: \"$MODE\""
        echo "Uso corretto: deploy.sh [local|prod|down]"
        exit 1
        ;;
esac

run_local() {
    echo
    echo "[LOCAL] Avvio dell'infrastruttura di sviluppo (PostgreSQL e PgAdmin)..."
    if ! docker compose up -d; then
        echo "[ERRORE] Errore durante l'avvio di Docker Compose!"
        exit 1
    fi

    echo
    echo "=== Infrastruttura LOCALE avviata con successo! ==="
    echo "- Database pronto su: localhost:5432"
    echo "- PgAdmin disponibile su: http://localhost:5050"
    echo "Ora puoi avviare l'applicazione Spring Boot da IntelliJ."
}

run_prod() {
    echo
    echo "[PROD] Build dell'immagine e avvio dei container..."
    echo "       (la compilazione Gradle avviene dentro il Dockerfile)"
    if ! docker compose -f docker-compose.prod.yaml up -d --build; then
        echo "[ERRORE] Errore durante il deploy in produzione con Docker Compose!"
        exit 1
    fi

    echo
    echo "=== Completato! Ambiente di PRODUZIONE avviato ==="
    echo "- Applicazione su: http://localhost:8080"
    echo "- Stop:            scripts/deploy.sh down"
}

run_down() {
    echo
    echo "[DOWN] Arresto dei container prod e local ed eliminazione dei volumi..."
    docker compose -f docker-compose.prod.yaml down -v
    docker compose down -v
    echo
    echo "Container fermati e volumi eliminati."
}

case "$MODE" in
    local|prod)
        if [[ ! -f ".env" ]]; then
            echo "[ERRORE] File .env mancante. Copia .env.example in .env e compila i valori."
            exit 1
        fi
        if [[ "$MODE" == "local" ]]; then
            run_local
        else
            run_prod
        fi
        ;;
    down)
        run_down
        ;;
esac