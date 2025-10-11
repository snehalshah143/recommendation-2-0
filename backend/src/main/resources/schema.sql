-- Create alerts table if it doesn't exist
CREATE TABLE IF NOT EXISTS alerts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_code VARCHAR(255) NOT NULL,
    price VARCHAR(255),
    alert_date DATETIME NOT NULL,
    scan_name VARCHAR(255),
    buy_sell VARCHAR(10),
    since_days INTEGER DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Create trade_recommendations table if it doesn't exist
CREATE TABLE IF NOT EXISTS trade_recommendations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    symbol VARCHAR(20) NOT NULL,
    exchange VARCHAR(10) NOT NULL,
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('BUY', 'SELL')),
    trade_duration VARCHAR(20) NOT NULL CHECK (trade_duration IN ('INTRADAY', 'POSITIONAL', 'SHORTTERM', 'LONGTERM')),
    timeframe VARCHAR(10) NOT NULL,
    entry_price REAL NOT NULL,
    target1 REAL,
    target2 REAL,
    target3 REAL,
    stoploss1 REAL NOT NULL,
    stoploss2 REAL NOT NULL,
    hard_stoploss REAL NOT NULL,
    trailing_method VARCHAR(50),
    trailing_value REAL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'CLOSED')),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at DATETIME,
    close_reason VARCHAR(50) CHECK (close_reason IN ('STOPLOSS_HIT', 'OPPOSITE_SIGNAL', 'MANUAL')),
    rule_version VARCHAR(50) NOT NULL DEFAULT 'v1-supertrend-75low',
    metadata TEXT
);

-- Create indexes for alerts table
CREATE INDEX IF NOT EXISTS idx_alerts_stock_code ON alerts (stock_code);
CREATE INDEX IF NOT EXISTS idx_alerts_alert_date ON alerts (alert_date);
CREATE INDEX IF NOT EXISTS idx_alerts_buy_sell ON alerts (buy_sell);

-- Create indexes for trade_recommendations table
CREATE INDEX IF NOT EXISTS idx_trade_recommendations_symbol ON trade_recommendations (symbol);
CREATE INDEX IF NOT EXISTS idx_trade_recommendations_status ON trade_recommendations (status);
CREATE INDEX IF NOT EXISTS idx_trade_recommendations_created_at ON trade_recommendations (created_at);
