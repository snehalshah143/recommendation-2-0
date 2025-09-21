-- Create trade_recommendations table for storing targets and stoploss calculations
CREATE TABLE trade_recommendations (
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
    metadata TEXT -- JSON string for additional data
);

-- Note: Unique constraint for active recommendations will be handled at application level

-- Create indexes for performance
CREATE INDEX idx_trade_recommendations_symbol ON trade_recommendations (symbol);
CREATE INDEX idx_trade_recommendations_status ON trade_recommendations (status);
CREATE INDEX idx_trade_recommendations_created_at ON trade_recommendations (created_at);

-- Note: SQLite doesn't support partial indexes with WHERE clause
-- The unique constraint will be handled at application level
