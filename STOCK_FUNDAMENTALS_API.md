# Stock Fundamentals API

## Overview
This API provides stock fundamental data by integrating with Angel One SmartAPI for live market prices and Yahoo Finance for fundamental metrics. The implementation follows the patterns and architecture from the marketdata project for consistency and maintainability.

## Endpoint
```
GET /api/stocks/fundamentals/{symbol}
```

## Parameters
- `symbol` (path parameter): Stock symbol (e.g., RELIANCE, TCS, HDFC)

## Response Format
```json
{
  "symbol": "RELIANCE",
  "peRatio": 24.5,
  "roe": 18.2,
  "roc": 15.8,
  "bookValue": 1250.0,
  "marketCap": 250000000000.0,
  "sales": 15200.0,
  "ltp": 2500.0
}
```

## Features
- **Live Market Price**: Fetched from Angel One SmartAPI
- **Fundamental Data**: P/E ratio and Market Cap from Yahoo Finance API
- **Mock Data Fallback**: Returns mock values if external APIs fail
- **Error Handling**: Graceful degradation with mock data

## Configuration
The application uses the same Angel One API credentials as the marketdata project:
```properties
# Angel One Configuration (from marketdata project)
angel.api.key=gX3qXZXn
angel.client.id=S133712
angel.password=1805
angel.totp=RUVZEH5OVV6MDYEWWAQ3EUSGCM

# Additional Angel One Keys (from marketdata project)
angel.market.api.key=OtbLUg4X
angel.market.secret.key=11841221-7bf1-4e99-8005-a25ca002a454
angel.historical.api.key=gX3qXZXn
angel.historical.secret.key=44064590-d952-44b4-b64b-175fcd1095cb
```

**Note**: These are the actual credentials from the marketdata project, so the API should work immediately without additional configuration.

## Example Usage
```bash
curl http://localhost:8082/api/stocks/fundamentals/RELIANCE
```

## Dependencies Added
- Angel One SmartAPI Java SDK
- Spring WebFlux for HTTP client
- Jackson for JSON processing
- OkHttp for HTTP requests

## Architecture
- **Controller**: `StockController` - REST endpoint
- **Service**: `StockService` - Business logic and API integration
- **Angel Integration**: `AngelMarketDataService` - Angel One SmartAPI integration (following marketdata patterns)
- **MetaData Service**: `MetaDataService` - Symbol to token mapping (following marketdata patterns)
- **Infrastructure**: `AngelBrokerConnector`, `AngelApiKey` - Angel One integration
- **Models**: `Ticker` - Stock ticker model (copied from marketdata)
- **Constants**: `ExchSeg`, `InstrumentType`, `CandleTimeFrame` - Market constants (copied from marketdata)
- **DTO**: `StockFundamentalsDto` - Response model
- **Config**: `WebClientConfig` - HTTP client configuration

## Code Reuse from MarketData Project
- **Constants**: ExchSeg, InstrumentType, CandleTimeFrame enums
- **Models**: Ticker domain model
- **Patterns**: AngelMarketDataService interface and implementation pattern
- **Architecture**: MetaDataService for symbol lookup
- **Integration**: Angel One SmartAPI integration following marketdata patterns
- **AngelBrokerConnector**: Advanced session management with TOTP, session expiry handling, and token management

## Advanced Features from MarketData AngelBrokerConnector
- **Singleton Pattern**: Efficient session management
- **TOTP Integration**: Automatic Google Authenticator TOTP generation
- **Session Expiry Handling**: Proper session expiry hooks and recovery
- **Token Management**: Access token and refresh token handling
- **Error Recovery**: Automatic session regeneration on failures
- **Production Ready**: Robust error handling and session management
