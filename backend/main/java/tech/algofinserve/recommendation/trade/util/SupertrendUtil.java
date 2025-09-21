package tech.algofinserve.recommendation.trade.util;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Utility class for calculating Supertrend and ATR indicators
 */
@Slf4j
public class SupertrendUtil {

    /**
     * Calculate Supertrend indicator
     * @param ohlcData List of OHLC data maps with keys: open, high, low, close
     * @param atrPeriod ATR period (typically 11)
     * @param multiplier ATR multiplier (typically 20)
     * @return Latest Supertrend value
     */
    public static Double calculateSupertrend(List<Map<String, Object>> ohlcData, int atrPeriod, int multiplier) {
        if (ohlcData == null || ohlcData.size() < atrPeriod) {
            log.warn("Insufficient data for Supertrend calculation. Required: {}, Available: {}", 
                    atrPeriod, ohlcData != null ? ohlcData.size() : 0);
            return null;
        }

        try {
            // Calculate ATR
            Double atr = calculateATR(ohlcData, atrPeriod);
            if (atr == null) {
                return null;
            }

            // Calculate Supertrend
            double[] supertrend = new double[ohlcData.size()];
            boolean[] trend = new boolean[ohlcData.size()]; // true = uptrend, false = downtrend

            // Initialize first value
            Map<String, Object> firstCandle = ohlcData.get(0);
            double high = getDoubleValue(firstCandle, "high");
            double low = getDoubleValue(firstCandle, "low");
            double close = getDoubleValue(firstCandle, "close");

            supertrend[0] = (high + low) / 2;
            trend[0] = close > supertrend[0];

            // Calculate Supertrend for remaining candles
            for (int i = 1; i < ohlcData.size(); i++) {
                Map<String, Object> candle = ohlcData.get(i);
                double currentHigh = getDoubleValue(candle, "high");
                double currentLow = getDoubleValue(candle, "low");
                double currentClose = getDoubleValue(candle, "close");

                // Basic upper and lower bands
                double basicUpper = (currentHigh + currentLow) / 2 + (multiplier * atr);
                double basicLower = (currentHigh + currentLow) / 2 - (multiplier * atr);

                // Final upper and lower bands
                double finalUpper = basicUpper;
                double finalLower = basicLower;

                if (i > 0) {
                    Map<String, Object> prevCandle = ohlcData.get(i - 1);
                    double prevHigh = getDoubleValue(prevCandle, "high");
                    double prevLow = getDoubleValue(prevCandle, "low");

                // Adjust final bands based on previous values
                if (basicUpper < supertrend[i - 1] || currentClose > supertrend[i - 1]) {
                    finalUpper = supertrend[i - 1];
                }

                if (basicLower > supertrend[i - 1] || currentClose < supertrend[i - 1]) {
                    finalLower = supertrend[i - 1];
                }
                }

                // Determine trend and Supertrend value
                if (trend[i - 1] && currentClose <= finalLower) {
                    trend[i] = false;
                    supertrend[i] = finalLower;
                } else if (!trend[i - 1] && currentClose >= finalUpper) {
                    trend[i] = true;
                    supertrend[i] = finalUpper;
                } else {
                    trend[i] = trend[i - 1];
                    supertrend[i] = trend[i] ? finalLower : finalUpper;
                }
            }

            // Return the latest Supertrend value
            return supertrend[supertrend.length - 1];

        } catch (Exception e) {
            log.error("Error calculating Supertrend: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Calculate ATR (Average True Range)
     * @param ohlcData List of OHLC data maps
     * @param period ATR period
     * @return ATR value
     */
    public static Double calculateATR(List<Map<String, Object>> ohlcData, int period) {
        if (ohlcData == null || ohlcData.size() < period + 1) {
            log.warn("Insufficient data for ATR calculation. Required: {}, Available: {}", 
                    period + 1, ohlcData != null ? ohlcData.size() : 0);
            return null;
        }

        try {
            double[] trueRanges = new double[ohlcData.size() - 1];

            // Calculate True Range for each candle
            for (int i = 1; i < ohlcData.size(); i++) {
                Map<String, Object> currentCandle = ohlcData.get(i);
                Map<String, Object> prevCandle = ohlcData.get(i - 1);

                double currentHigh = getDoubleValue(currentCandle, "high");
                double currentLow = getDoubleValue(currentCandle, "low");
                double prevClose = getDoubleValue(prevCandle, "close");

                // True Range = max(high - low, |high - prevClose|, |low - prevClose|)
                double tr1 = currentHigh - currentLow;
                double tr2 = Math.abs(currentHigh - prevClose);
                double tr3 = Math.abs(currentLow - prevClose);

                trueRanges[i - 1] = Math.max(tr1, Math.max(tr2, tr3));
            }

            // Calculate ATR as simple moving average of True Ranges
            double sum = 0;
            for (int i = 0; i < period; i++) {
                sum += trueRanges[i];
            }

            return sum / period;

        } catch (Exception e) {
            log.error("Error calculating ATR: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Helper method to safely extract double value from map
     */
    private static double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        throw new IllegalArgumentException("Invalid value for key " + key + ": " + value);
    }
}
