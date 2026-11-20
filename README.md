# Sales Service --- Database Design

## Avvio ed esecuzione del progetto

Per eseguire il progetto è necessario avere **Docker** attivo e configurare il file `.env` con le credenziali richieste (prendendo spunto da `.env.example`). Tutti i comandi vanno eseguiti tramite lo script **`deploy.bat`**, situato nella **root del progetto**.

### 1. Avvio in locale (Sviluppo)

1. Apri il terminale nella root del progetto (si consiglia l'uso di **IntelliJ IDEA**) ed esegui lo script `deploy.bat` (presente nella root) per avviare l'infrastruttura locale:

   ```bash
   deploy.bat local
   ```

   Questo comando avvia i container **PostgreSQL** e **pgAdmin** utilizzando le variabili definite nel file `.env`.

2. Una volta terminato lo script, avvia l'applicazione **Spring Boot** direttamente da IntelliJ.

3. All'avvio, **Flyway** applica automaticamente lo schema del database e carica una batch iniziale di dati di test.

#### Configurazione di pgAdmin

1. Apri il browser all'indirizzo di pgAdmin (`http://localhost:5050`) ed effettua l'accesso con email e password impostate nel `.env`.

2. Aggiungi un nuovo server (*Add New Server*):

    * **General -> Name**: assegna un nome a piacere (es. `Sales DB`).

    * **Connection -> Host name/address**: inserisci `sales-db` (o `sales-<nome del container postgresql>`).

    * **Connection -> Username / Password**: inserisci le credenziali del database definite nel `.env`.

---

### 2. Avvio in produzione

Dalla root del progetto, lancia lo script `deploy.bat` con l'opzione `prod`:

```bash
deploy.bat prod
```

In questa modalità:

* I dati di test locali (`dev-data`) **non** vengono caricati.

* **pgAdmin** non viene scaricato né avviato.

---

### 3. Arresto dei servizi

Per fermare i container e ripulire l'ambiente (inclusi i volumi locali), esegui dalla root del progetto:

```bash
deploy.bat down
```
## 1. Schema relazionale

Il diagramma mostra le relazioni interne al database Sales. Gli
identificativi `catalog_*` e `payments_*` sono riferimenti a sistemi
esterni e, intenzionalmente, non hanno foreign key.

```mermaid
erDiagram
    sales_sessions ||--o{ session_seats : contains
    holds ||--|{ hold_items : contains
    session_seats ||--o{ hold_items : reserved_by
    holds ||--o| orders : creates
    orders ||--|{ order_items : contains
    session_seats ||--o{ order_items : referenced_by

    sales_sessions {
        UUID id PK
        UUID catalog_event_id
        UUID catalog_session_id UK
        UUID catalog_venue_id
        TIMESTAMPTZ catalog_session_start_at
        TIMESTAMPTZ catalog_sales_start_at
        TIMESTAMPTZ catalog_sales_end_at
        INTEGER catalog_max_tickets_per_order
        BIGINT snapshot_version
        TIMESTAMPTZ snapshot_created_at
        TIMESTAMPTZ snapshot_updated_at
    }

    session_seats {
        UUID id PK
        UUID sales_session_id FK
        UUID catalog_seat_id
        VARCHAR status "FREE, HELD, SOLD"
        NUMERIC catalog_price
        CHAR catalog_currency "EUR, USD, JPY, GBP"
    }

    holds {
        UUID id PK
        UUID customer_id
        VARCHAR status "ACTIVE, ORDER_CREATED, EXPIRED"
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    hold_items {
        UUID id PK
        UUID hold_id FK
        UUID seat_id FK
    }

    orders {
        UUID id PK
        UUID hold_id FK, UK
        UUID customer_id
        VARCHAR customer_email
        VARCHAR customer_first_name
        VARCHAR customer_last_name
        NUMERIC total_amount
        CHAR currency "EUR, USD, JPY, GBP"
        VARCHAR status "PENDING_PAYMENT, PAID, PAYMENT_FAILED, EXPIRED, CANCELLED"
        TIMESTAMPTZ payment_expires_at
        UUID payments_payment_id
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    order_items {
        UUID id PK
        UUID order_id FK
        UUID seat_id FK, UK
        NUMERIC catalog_unit_price
        CHAR currency "EUR, USD, JPY, GBP"
    }

    incoming_messages {
        UUID message_id PK
        VARCHAR message_type
        JSONB payload
        TIMESTAMPTZ processed_at
    }

    outgoing_messages {
        UUID id PK
        VARCHAR message_type
        JSONB payload
        TIMESTAMPTZ created_at
        TIMESTAMPTZ published_at
    }
```
## 2. Tabelle

### 2.1 `sales_sessions`

Rappresenta lo **snapshot locale di una sessione Catalog** necessario a
Sales per gestire la vendita senza interrogare Catalog durante il
checkout.

| Colonna | Significato |
|---|---|
| `id` | Identificativo UUID interno a Sales |
| `catalog_event_id` | ID dell'evento in Catalog |
| `catalog_session_id` | ID della sessione in Catalog |
| `catalog_venue_id` | ID della venue in Catalog |
| `catalog_session_start_at` | Data e ora di inizio della sessione |
| `catalog_sales_start_at` | Apertura delle vendite |
| `catalog_sales_end_at` | Chiusura delle vendite |
| `catalog_max_tickets_per_order` | Numero massimo di biglietti per ordine |
| `snapshot_version` | Versione dello snapshot applicata da Sales |
| `snapshot_created_at` | Creazione dello snapshot |
| `snapshot_updated_at` | Ultimo aggiornamento dello snapshot |

### 2.2 `session_seats`

Contiene una riga per ogni posto vendibile associato a una sessione.
Sales conserva **solo le informazioni commerciali**, non la geometria
della pianta o i dati grafici del posto.

| Colonna | Significato |
|---|---|
| `id` | Identificativo UUID interno della combinazione sessione/posto |
| `sales_session_id` | FK a `sales_sessions.id` |
| `catalog_seat_id` | Identificativo del posto fisico in Catalog |
| `status` | Disponibilità commerciale: `FREE`, `HELD`, `SOLD` |
| `catalog_currency` |    `EUR`, `USD`, `JPY`, `GBP` |
| `catalog_price` | Prezzo ricevuto da Catalog nello snapshot |

Il frontend può unire la seat map restituita da Catalog con
disponibilità e prezzi restituiti da Sales usando `catalog_seat_id`.

### 2.3 `holds`

Rappresenta il blocco temporaneo dei posti selezionati da un cliente.

| Colonna | Significato | Stati concordati |
|---|---|---|
| `id` | Identificativo UUID | - |
| `customer_id` | Cliente proprietario dell'HOLD | - |
| `status` | Stato dell'HOLD | `ACTIVE`: HOLD attivo.<br>`ORDER_CREATED`: è stato creato l'ordine a partire dall'HOLD.<br>`EXPIRED`: HOLD scaduto. |
| `expires_at` | Scadenza del blocco | - |
| `created_at` | Creazione | - |
| `updated_at` | Ultima modifica | - |


Se il cliente chiude il browser, il backend non tenta di rilevarlo:
l'HOLD rimane attivo fino alla scadenza. Un processo backend si occupa
di scadere gli HOLD.

### 2.4 `hold_items`

Associa i posti a un HOLD.

| Colonna | Significato |
|---|---|
| `id` | Identificativo UUID |
| `hold_id` | FK a `holds.id` |
| `seat_id` | FK a `session_seats.id` |

La creazione di un HOLD deve verificare e aggiornare atomicamente i
posti: solo i posti con stato `FREE` possono passare a `HELD`. Il
controllo deve essere sicuro in presenza di richieste concorrenti.

### 2.5 `orders`

Rappresenta l'ordine commerciale creato a partire da un HOLD.

| Colonna | Significato | Stati concordati |
|---|---|---|
| `id` | Identificativo UUID | - |
| `hold_id` | FK a `holds.id`, univoca | - |
| `customer_id` | Identificativo del cliente | - |
| `customer_email` | Snapshot dell'email al momento dell'ordine | - |
| `customer_first_name` | Snapshot del nome | - |
| `customer_last_name` | Snapshot del cognome | - |
| `total_amount` | Importo totale dell'ordine | - |
| `currency` | Codice valuta ISO 4217, ad esempio `EUR` | - |
| `status` | Stato dell'ordine | `PENDING_PAYMENT`<br>`PAID`<br>`PAYMENT_FAILED`<br>`EXPIRED`<br>`CANCELLED` |
| `payment_expires_at` | Scadenza del tempo concesso per il pagamento | - |
| `payments_payment_id` | ID del pagamento in Payments, nullable | - |
| `created_at` | Creazione | - |
| `updated_at` | Ultima modifica | - |

Al momento della creazione, `holds.expires_at` e
`orders.payment_expires_at` possono ricevere lo stesso timestamp. Dopo
la creazione dell'ordine, il processo di pagamento è governato dalla
scadenza dell'ordine.

### 2.6 `order_items`

Contiene una riga per ogni posto associato all'ordine.

| Colonna | Significato |
|---|---|
| `id` | Identificativo UUID |
| `order_id` | FK a `orders.id` |
| `seat_id` | FK a `session_seats.id`, univoca |
| `catalog_unit_price` | Prezzo unitario fotografato al momento dell'ordine |
| `currency` | Codice valuta ISO 4217 (`EUR`, `USD`, `JPY`, `GBP`) |

### 2.7 `incoming_messages`

Registra i messaggi ricevuti dagli altri microservizi e già elaborati da
Sales. Serve a garantire l'idempotenza: se lo stesso messaggio viene
consegnato più volte, Sales riconosce l'ID già registrato e non ripete
l'elaborazione.

| Colonna | Significato |
|---|---|
| `message_id` | ID originale del messaggio, PK |
| `message_type` | Tipo di messaggio ricevuto |
| `payload` | Contenuto originale del messaggio, JSONB |
| `processed_at` | Timestamp dell'elaborazione |

La registrazione del messaggio, l'aggiornamento dei dati di business e
l'eventuale inserimento in `outgoing_messages` devono avvenire nella
stessa transazione. Se la transazione fallisce, anche la registrazione
del messaggio viene annullata e il broker può riconsegnarlo.

### 2.8 `outgoing_messages`

Registra i messaggi generati da Sales che devono essere pubblicati sul
message broker. Implementa il lato Outbox del pattern.

| Colonna | Significato |
|---|---|
| `id` | Identificativo UUID del messaggio, PK |
| `message_type` | Tipo di messaggio da pubblicare |
| `payload` | Contenuto del messaggio, JSONB |
| `created_at` | Momento di creazione |
| `published_at` | Momento di pubblicazione confermata; nullable |