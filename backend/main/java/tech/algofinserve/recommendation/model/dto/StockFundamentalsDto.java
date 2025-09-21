package tech.algofinserve.recommendation.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockFundamentalsDto {
    
    private String symbol;
    private Double peRatio;
    private Double roe;
    private Double roc;
    private Double bookValue;
    private Double marketCap;
    private Double sales;
    private Double ltp;
    
    public StockFundamentalsDto() {}
    
    public StockFundamentalsDto(String symbol, Double peRatio, Double roe, Double roc, 
                               Double bookValue, Double marketCap, Double sales, Double ltp) {
        this.symbol = symbol;
        this.peRatio = peRatio;
        this.roe = roe;
        this.roc = roc;
        this.bookValue = bookValue;
        this.marketCap = marketCap;
        this.sales = sales;
        this.ltp = ltp;
    }
    
    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public Double getPeRatio() {
        return peRatio;
    }
    
    public void setPeRatio(Double peRatio) {
        this.peRatio = peRatio;
    }
    
    public Double getRoe() {
        return roe;
    }
    
    public void setRoe(Double roe) {
        this.roe = roe;
    }
    
    public Double getRoc() {
        return roc;
    }
    
    public void setRoc(Double roc) {
        this.roc = roc;
    }
    
    public Double getBookValue() {
        return bookValue;
    }
    
    public void setBookValue(Double bookValue) {
        this.bookValue = bookValue;
    }
    
    public Double getMarketCap() {
        return marketCap;
    }
    
    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }
    
    public Double getSales() {
        return sales;
    }
    
    public void setSales(Double sales) {
        this.sales = sales;
    }
    
    public Double getLtp() {
        return ltp;
    }
    
    public void setLtp(Double ltp) {
        this.ltp = ltp;
    }
    
    @Override
    public String toString() {
        return "StockFundamentalsDto{" +
                "symbol='" + symbol + '\'' +
                ", peRatio=" + peRatio +
                ", roe=" + roe +
                ", roc=" + roc +
                ", bookValue=" + bookValue +
                ", marketCap=" + marketCap +
                ", sales=" + sales +
                ", ltp=" + ltp +
                '}';
    }
}
