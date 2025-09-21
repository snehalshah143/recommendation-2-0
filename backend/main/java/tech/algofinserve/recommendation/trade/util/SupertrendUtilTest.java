package tech.algofinserve.recommendation.trade.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SupertrendUtil
 */
class SupertrendUtilTest {

    @Test
    void testCalculateATR_ValidData() {
        // Given
        List<Map<String, Object>> ohlcData = createSampleOHLCData();

        // When
        Double atr = SupertrendUtil.calculateATR(ohlcData, 14);

        // Then
        assertNotNull(atr);
        assertTrue(atr > 0);
    }

    @Test
    void testCalculateATR_InsufficientData() {
        // Given
        List<Map<String, Object>> ohlcData = createSampleOHLCData().subList(0, 5); // Only 5 candles

        // When
        Double atr = SupertrendUtil.calculateATR(ohlcData, 14);

        // Then
        assertNull(atr);
    }

    @Test
    void testCalculateSupertrend_ValidData() {
        // Given
        List<Map<String, Object>> ohlcData = createSampleOHLCData();

        // When
        Double supertrend = SupertrendUtil.calculateSupertrend(ohlcData, 11, 20);

        // Then
        assertNotNull(supertrend);
        assertTrue(supertrend > 0);
    }

    @Test
    void testCalculateSupertrend_InsufficientData() {
        // Given
        List<Map<String, Object>> ohlcData = createSampleOHLCData().subList(0, 5); // Only 5 candles

        // When
        Double supertrend = SupertrendUtil.calculateSupertrend(ohlcData, 11, 20);

        // Then
        assertNull(supertrend);
    }

    @Test
    void testCalculateSupertrend_NullData() {
        // When
        Double supertrend = SupertrendUtil.calculateSupertrend(null, 11, 20);

        // Then
        assertNull(supertrend);
    }

    private List<Map<String, Object>> createSampleOHLCData() {
        return List.of(
                createOHLCCandle(100.0, 105.0, 98.0, 102.0),
                createOHLCCandle(102.0, 108.0, 101.0, 106.0),
                createOHLCCandle(106.0, 110.0, 104.0, 108.0),
                createOHLCCandle(108.0, 112.0, 106.0, 110.0),
                createOHLCCandle(110.0, 115.0, 109.0, 113.0),
                createOHLCCandle(113.0, 118.0, 112.0, 116.0),
                createOHLCCandle(116.0, 120.0, 115.0, 118.0),
                createOHLCCandle(118.0, 122.0, 117.0, 120.0),
                createOHLCCandle(120.0, 125.0, 119.0, 123.0),
                createOHLCCandle(123.0, 128.0, 122.0, 126.0),
                createOHLCCandle(126.0, 130.0, 125.0, 128.0),
                createOHLCCandle(128.0, 132.0, 127.0, 130.0),
                createOHLCCandle(130.0, 135.0, 129.0, 133.0),
                createOHLCCandle(133.0, 138.0, 132.0, 136.0),
                createOHLCCandle(136.0, 140.0, 135.0, 138.0),
                createOHLCCandle(138.0, 142.0, 137.0, 140.0),
                createOHLCCandle(140.0, 145.0, 139.0, 143.0),
                createOHLCCandle(143.0, 148.0, 142.0, 146.0),
                createOHLCCandle(146.0, 150.0, 145.0, 148.0),
                createOHLCCandle(148.0, 152.0, 147.0, 150.0)
        );
    }

    private Map<String, Object> createOHLCCandle(double open, double high, double low, double close) {
        Map<String, Object> candle = new HashMap<>();
        candle.put("open", open);
        candle.put("high", high);
        candle.put("low", low);
        candle.put("close", close);
        return candle;
    }
}
