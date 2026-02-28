# Tradebot - Automated Trading System

A production-grade, extensible automated trading system built with Java Spring Boot.

## 🏗️ Architecture

The system follows **Hexagonal (Ports and Adapters) Architecture** to ensure high decoupling and testability.

- **Domain Layer**: Pure business logic and models (`News`, `TradeSignal`, `Order`, etc.).
- **Application Layer**: Services and Ports (Interfaces) for the core trading logic.
- **Infrastructure Layer**: Implementation of ports (Adapters) like `CryptoNewsScraper`, `OllamaStrategy`, and `CoinDCXBroker`.
- **API Layer**: REST Controllers exposing functionality via Swagger.

## 🚀 Getting Started

### Prerequisites
- Java 23
- Maven

### Running the Application
```bash
./mvnw spring-boot:run
```

### API Documentation (Swagger)
Once running, access Swagger UI at:
`http://localhost:8080/swagger-ui.html`

## 🧩 Key Modules

### 1. News Scraper
Fetches and normalizes news from various sources.
- **Interface**: `NewsSource`
- **Default**: `CryptoNewsScraper` (Simulated RSS/API)

### 2. Trading Strategy Engine
Generates BUY/SELL signals based on news and portfolio context.
- **Interface**: `TradingStrategy`
- **Default**: `OllamaLLMStrategy` (Simulated LLM-based analysis)

### 3. Broker Execution Layer
Handles order placement and portfolio management.
- **Interface**: `Broker`
- **Default**: `CoinDCXBroker` (Simulated integration)

## 🔁 Execution Flow (Pipeline)

1. **Fetch**: Gather latest news from the selected source.
2. **Context**: Retrieve current portfolio and market data from the broker.
3. **Analyze**: Pass context to the strategy engine.
4. **Decide**: Strategy generates trade signals.
5. **Execute**: Broker places orders based on signals.

## 🛠️ Testing via API

Use the following endpoints in Swagger:

- `GET /news`: Fetch latest news.
- `GET /broker/portfolio`: View current holdings.
- `POST /strategy/run`: Test a strategy with custom context.
- `POST /trade/execute`: Trigger the full end-to-end pipeline.

## 🔧 Extensibility

To add a new component:
1. Implement the corresponding interface (`NewsSource`, `TradingStrategy`, or `Broker`).
2. Annotate with `@Component`.
3. The system will automatically register it and make it available via API parameters.
