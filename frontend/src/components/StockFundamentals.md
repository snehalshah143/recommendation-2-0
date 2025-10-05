# StockFundamentals Component

A React component that displays stock fundamental data by calling the `/api/stocks/fundamentals/{symbol}` endpoint.

## Features

- **Real-time Data**: Fetches live market price and fundamental data from Angel One SmartAPI and Yahoo Finance
- **Error Handling**: Shows `--` for null values and handles API errors gracefully
- **Loading State**: Displays loading spinner while fetching data
- **Consistent Styling**: Uses existing card layout and styling patterns
- **Responsive**: Works on all screen sizes

## Usage

```jsx
import StockFundamentals from './components/StockFundamentals';

// Basic usage
<StockFundamentals symbol="RELIANCE" />

// With custom base URL
<StockFundamentals symbol="TCS" baseUrl="http://localhost:8082" />
```

## Props

| Prop | Type | Required | Default | Description |
|------|------|----------|---------|-------------|
| `symbol` | string | Yes | - | Stock symbol (e.g., 'RELIANCE', 'TCS') |
| `baseUrl` | string | No | `''` | API base URL for backend calls |

## Data Displayed

- **P/E Ratio**: Price-to-Earnings ratio
- **ROE**: Return on Equity (as percentage)
- **ROC**: Return on Capital (as percentage)
- **Book Value**: Book value per share (in ₹)
- **Market Cap**: Market capitalization (formatted)
- **Sales (Qtr)**: Quarterly sales (formatted)
- **LTP**: Last Traded Price (live from Angel One)

## Error Handling

- Shows `--` for null/undefined values
- Displays error message if API call fails
- Falls back to mock data structure to prevent UI breaking
- Shows loading spinner during API calls

## Styling

Uses the same card layout as existing components:
- Card with header and content
- Consistent spacing and typography
- Responsive grid layout
- Loading and error states

## API Integration

Calls the backend endpoint: `GET /api/stocks/fundamentals/{symbol}`

Expected response format:
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




