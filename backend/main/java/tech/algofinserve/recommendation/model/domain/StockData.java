package tech.algofinserve.recommendation.model.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class StockData implements Serializable {

    public StockData() {}

    private Long id;
    private String symbolId;
    private LocalDateTime timestamp;

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;
    private Long candleNum;
    private Long openInterest;
    private TechnicalData technicalData;

    public TechnicalData getTechnicalData() {
        return technicalData;
    }

    public void setTechnicalData(TechnicalData technicalData) {
        this.technicalData = technicalData;
    }

    public Long getCandleNum() {
        return candleNum;
    }

    public void setCandleNum(Long candleNum) {
        this.candleNum = candleNum;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockData stockData = (StockData) o;
        return symbolId.equals(stockData.symbolId)
            && timestamp.equals(stockData.timestamp)
            && Objects.equals(open, stockData.open)
            && Objects.equals(high, stockData.high)
            && Objects.equals(low, stockData.low)
            && Objects.equals(close, stockData.close)
            && Objects.equals(volume, stockData.volume)
            && Objects.equals(candleNum, stockData.candleNum)
            && Objects.equals(openInterest, stockData.openInterest)
            && Objects.equals(technicalData, stockData.technicalData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            symbolId,
            timestamp,
            open,
            high,
            low,
            close,
            volume,
            candleNum,
            openInterest,
            technicalData);
    }
}
