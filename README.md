# spending-aggregator-service

A Spring Boot microservice that consumes transaction events from RabbitMQ, categorizes them, and aggregates them into spending summaries queryable via a REST API.

---

## How It Works

### 1. Event Consumption
The service listens on the `fintrack.transactions` RabbitMQ queue (bound to the `fintrack.transactions` topic exchange with routing key `transaction.TRANSACTION`). Messages are deserialized as `TransactionIngestedEvent` objects using a JSON message converter.

The listener runs with manual acknowledgement and a concurrency of 4–10 threads. On success, the message is ACK'd. On failure, it is NACK'd (not re-queued).

### 2. Categorization
Each transaction is categorized by `CategoryResolver` using the merchant name and description. The text is normalized (noise prefixes like `POS PURCHASE`, `CARD PURCHASE`, etc. are stripped) and then matched against regex rules defined in `application.yaml`.

Categories (in match order):

| Category | Examples |
|---|---|
| `INCOME` | Salary, payroll, bonus, refund, cashback |
| `TRANSFER` | EFT, instant payment, Zapper, SnapScan, wallet |
| `UTILITIES` | Eskom, Vodacom, MTN, municipality, fibre providers |
| `HEALTHCARE` | Dis-Chem, Clicks, Netcare, doctor, pharmacy |
| `GROCERIES` | Checkers, Pick n Pay, Woolworths, SPAR |
| `DINING` | KFC, Nando's, Steers, restaurants, cafes |
| `TRANSPORT` | Uber, Bolt, petrol stations, e-toll, airlines |
| `SUBSCRIPTIONS` | Netflix, Spotify, DStv, Adobe, Apple, Google |
| `SHOPPING` | Takealot, Mr Price, Zara, Nike, online shops |
| `ENTERTAINMENT` | Cinema, gym, concerts, events |
| `INVESTMENT` | Easy Equities, Allan Gray, unit trusts, crypto |
| `LOAN_PAYMENT` | Home loan, bond, vehicle finance, personal loan |
| `OTHER` | Catch-all for anything unmatched |

### 3. Aggregation
Once categorized, `SpendingSummaryUpdater` upserts a `spending_summary` row keyed on `(source_id, period, currency, category)`. It accumulates `total_amount` and `transaction_count`, and updates `last_updated_at`. The period is derived from the transaction date in `YYYY-MM` format.

### 4. REST API
The service exposes a read-only API on port `8083`, protected by an API key passed in the request header.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/transaction/summary` | Returns aggregated spending for the previous calendar month |
| `GET` | `/transaction/summary/{period}` | Returns aggregated spending for a given period (e.g. `2025-04`) |

Response shape:
```json
[
  {
    "id": "uuid",
    "period": "2025-04",
    "category": "GROCERIES",
    "currency": "ZAR",
    "totalAmount": 3200.50
  }
]
```

Results are ordered by `totalAmount` descending.

---

## Database

PostgreSQL with Flyway migrations. Schema is validated on startup (`ddl-auto: validate`).

**Table:** `spending_summary`

| Column | Type | Description |
|---|---|---|
| `id` | UUID | Primary key |
| `source_id` | VARCHAR | Identifier of the transaction source |
| `period` | VARCHAR(7) | Month in `YYYY-MM` format |
| `category` | VARCHAR(50) | Spending category |
| `currency` | VARCHAR(10) | Currency code |
| `total_amount` | NUMERIC(19,4) | Aggregated spend |
| `transaction_count` | INT | Number of transactions |
| `last_updated_at` | TIMESTAMPTZ | Last update timestamp |

---

## Environment Variables

| Variable | Description |
|---|---|
| `AGGREGATOR_DB_URL` | JDBC connection URL (e.g. `jdbc:postgresql://host:5432/dbname`) |
| `AGGREGATOR_DB_USERNAME` | Database username |
| `AGGREGATOR_DB_PASSWORD` | Database password |
| `RABBITMQ_HOST` | RabbitMQ broker hostname |
| `RABBITMQ_USERNAME` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `API_KEY` | API key required in request headers |

---

## Tech Stack

- Java 21 (virtual threads enabled)
- Spring Boot 3.2
- Spring AMQP (RabbitMQ)
- Spring Data JPA + PostgreSQL
- Flyway
- Micrometer (metrics: `transactions.categorized`, `transactions.consumed`)