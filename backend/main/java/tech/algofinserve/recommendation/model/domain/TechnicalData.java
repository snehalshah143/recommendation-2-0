package tech.algofinserve.recommendation.model.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class TechnicalData extends TechnicalDataBase {

    public TechnicalData() {
        super();
    }
}

abstract class TechnicalDataBase implements Serializable {

    public TechnicalDataBase() {}
    
    private String tId;
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

    public String getTId() {
        return tId;
    }

    public void setTId(String tId) {
        this.tId = tId;
    }

    private Double ema8;
    private Double ema13;
    private Double ema21;
    private Double ema34;
    private Double ema55;
    private Double ema89;
    private Double ema200;
    private Double rsi3;
    private Double rsi14;
    private Double rsi21;
    private Double hull21;
    private Double hull55;
    private Double hull89;
    private Double vwap;
    private Double vwma21;
    private Double rsi14ema9;
    private Double rsi14ema21;
    private Double ichimokuCloudTop;
    private Double ichimokuCloudBottom;
    private Double ichimokuCloudBaseLine;
    private Double ichimokuCloudConversionLine;
    private Double macd22107;
    private Double macdSignal22107;
    private Double macdHist22107;
    private Double macd26129;
    private Double macdSignal26129;
    private Double macdHist26129;
    private Double macd1153;
    private Double macdSignal1153;
    private Double macdHist1153;
    private Double atr;
    private Double diPositive14;
    private Double dinegative14;
    private Double adx14;
    private Double supertrend103;
    private Double supertrend112;
    private Double bbUpper202;
    private Double bbLower202;
    private Double bbMiddle202;

    // Getters and Setters
    public Double getEma8() { return ema8; }
    public void setEma8(Double ema8) { this.ema8 = ema8; }

    public Double getEma13() { return ema13; }
    public void setEma13(Double ema13) { this.ema13 = ema13; }

    public Double getEma21() { return ema21; }
    public void setEma21(Double ema21) { this.ema21 = ema21; }

    public Double getEma34() { return ema34; }
    public void setEma34(Double ema34) { this.ema34 = ema34; }

    public Double getEma55() { return ema55; }
    public void setEma55(Double ema55) { this.ema55 = ema55; }

    public Double getEma89() { return ema89; }
    public void setEma89(Double ema89) { this.ema89 = ema89; }

    public Double getEma200() { return ema200; }
    public void setEma200(Double ema200) { this.ema200 = ema200; }

    public Double getRsi3() { return rsi3; }
    public void setRsi3(Double rsi3) { this.rsi3 = rsi3; }

    public Double getRsi14() { return rsi14; }
    public void setRsi14(Double rsi14) { this.rsi14 = rsi14; }

    public Double getRsi21() { return rsi21; }
    public void setRsi21(Double rsi21) { this.rsi21 = rsi21; }

    public Double getHull21() { return hull21; }
    public void setHull21(Double hull21) { this.hull21 = hull21; }

    public Double getHull55() { return hull55; }
    public void setHull55(Double hull55) { this.hull55 = hull55; }

    public Double getHull89() { return hull89; }
    public void setHull89(Double hull89) { this.hull89 = hull89; }

    public Double getVwap() { return vwap; }
    public void setVwap(Double vwap) { this.vwap = vwap; }

    public Double getVwma21() { return vwma21; }
    public void setVwma21(Double vwma21) { this.vwma21 = vwma21; }

    public Double getRsi14ema9() { return rsi14ema9; }
    public void setRsi14ema9(Double rsi14ema9) { this.rsi14ema9 = rsi14ema9; }

    public Double getRsi14ema21() { return rsi14ema21; }
    public void setRsi14ema21(Double rsi14ema21) { this.rsi14ema21 = rsi14ema21; }

    public Double getIchimokuCloudTop() { return ichimokuCloudTop; }
    public void setIchimokuCloudTop(Double ichimokuCloudTop) { this.ichimokuCloudTop = ichimokuCloudTop; }

    public Double getIchimokuCloudBottom() { return ichimokuCloudBottom; }
    public void setIchimokuCloudBottom(Double ichimokuCloudBottom) { this.ichimokuCloudBottom = ichimokuCloudBottom; }

    public Double getIchimokuCloudBaseLine() { return ichimokuCloudBaseLine; }
    public void setIchimokuCloudBaseLine(Double ichimokuCloudBaseLine) { this.ichimokuCloudBaseLine = ichimokuCloudBaseLine; }

    public Double getIchimokuCloudConversionLine() { return ichimokuCloudConversionLine; }
    public void setIchimokuCloudConversionLine(Double ichimokuCloudConversionLine) { this.ichimokuCloudConversionLine = ichimokuCloudConversionLine; }

    public Double getMacd22107() { return macd22107; }
    public void setMacd22107(Double macd22107) { this.macd22107 = macd22107; }

    public Double getMacdSignal22107() { return macdSignal22107; }
    public void setMacdSignal22107(Double macdSignal22107) { this.macdSignal22107 = macdSignal22107; }

    public Double getMacdHist22107() { return macdHist22107; }
    public void setMacdHist22107(Double macdHist22107) { this.macdHist22107 = macdHist22107; }

    public Double getMacd26129() { return macd26129; }
    public void setMacd26129(Double macd26129) { this.macd26129 = macd26129; }

    public Double getMacdSignal26129() { return macdSignal26129; }
    public void setMacdSignal26129(Double macdSignal26129) { this.macdSignal26129 = macdSignal26129; }

    public Double getMacdHist26129() { return macdHist26129; }
    public void setMacdHist26129(Double macdHist26129) { this.macdHist26129 = macdHist26129; }

    public Double getMacd1153() { return macd1153; }
    public void setMacd1153(Double macd1153) { this.macd1153 = macd1153; }

    public Double getMacdSignal1153() { return macdSignal1153; }
    public void setMacdSignal1153(Double macdSignal1153) { this.macdSignal1153 = macdSignal1153; }

    public Double getMacdHist1153() { return macdHist1153; }
    public void setMacdHist1153(Double macdHist1153) { this.macdHist1153 = macdHist1153; }

    public Double getAtr() { return atr; }
    public void setAtr(Double atr) { this.atr = atr; }

    public Double getDiPositive14() { return diPositive14; }
    public void setDiPositive14(Double diPositive14) { this.diPositive14 = diPositive14; }

    public Double getDinegative14() { return dinegative14; }
    public void setDinegative14(Double dinegative14) { this.dinegative14 = dinegative14; }

    public Double getAdx14() { return adx14; }
    public void setAdx14(Double adx14) { this.adx14 = adx14; }

    public Double getSupertrend103() { return supertrend103; }
    public void setSupertrend103(Double supertrend103) { this.supertrend103 = supertrend103; }

    public Double getSupertrend112() { return supertrend112; }
    public void setSupertrend112(Double supertrend112) { this.supertrend112 = supertrend112; }

    public Double getBbUpper202() { return bbUpper202; }
    public void setBbUpper202(Double bbUpper202) { this.bbUpper202 = bbUpper202; }

    public Double getBbLower202() { return bbLower202; }
    public void setBbLower202(Double bbLower202) { this.bbLower202 = bbLower202; }

    public Double getBbMiddle202() { return bbMiddle202; }
    public void setBbMiddle202(Double bbMiddle202) { this.bbMiddle202 = bbMiddle202; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechnicalDataBase that = (TechnicalDataBase) o;
        return symbolId.equals(that.symbolId)
            && timestamp.equals(that.timestamp)
            && Objects.equals(ema8, that.ema8)
            && Objects.equals(ema13, that.ema13)
            && Objects.equals(ema21, that.ema21)
            && Objects.equals(ema34, that.ema34)
            && Objects.equals(ema55, that.ema55)
            && Objects.equals(ema89, that.ema89)
            && Objects.equals(ema200, that.ema200)
            && Objects.equals(rsi3, that.rsi3)
            && Objects.equals(rsi14, that.rsi14)
            && Objects.equals(rsi21, that.rsi21)
            && Objects.equals(hull21, that.hull21)
            && Objects.equals(hull55, that.hull55)
            && Objects.equals(hull89, that.hull89)
            && Objects.equals(vwap, that.vwap)
            && Objects.equals(vwma21, that.vwma21)
            && Objects.equals(rsi14ema9, that.rsi14ema9)
            && Objects.equals(rsi14ema21, that.rsi14ema21)
            && Objects.equals(ichimokuCloudTop, that.ichimokuCloudTop)
            && Objects.equals(ichimokuCloudBottom, that.ichimokuCloudBottom)
            && Objects.equals(ichimokuCloudBaseLine, that.ichimokuCloudBaseLine)
            && Objects.equals(ichimokuCloudConversionLine, that.ichimokuCloudConversionLine)
            && Objects.equals(macd22107, that.macd22107)
            && Objects.equals(macdSignal22107, that.macdSignal22107)
            && Objects.equals(macdHist22107, that.macdHist22107)
            && Objects.equals(macd26129, that.macd26129)
            && Objects.equals(macdSignal26129, that.macdSignal26129)
            && Objects.equals(macdHist26129, that.macdHist26129)
            && Objects.equals(macd1153, that.macd1153)
            && Objects.equals(macdSignal1153, that.macdSignal1153)
            && Objects.equals(macdHist1153, that.macdHist1153)
            && Objects.equals(atr, that.atr)
            && Objects.equals(diPositive14, that.diPositive14)
            && Objects.equals(dinegative14, that.dinegative14)
            && Objects.equals(adx14, that.adx14)
            && Objects.equals(supertrend103, that.supertrend103)
            && Objects.equals(supertrend112, that.supertrend112)
            && Objects.equals(bbUpper202, that.bbUpper202)
            && Objects.equals(bbLower202, that.bbLower202)
            && Objects.equals(bbMiddle202, that.bbMiddle202);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            symbolId,
            timestamp,
            ema8,
            ema13,
            ema21,
            ema34,
            ema55,
            ema89,
            ema200,
            rsi3,
            rsi14,
            rsi21,
            hull21,
            hull55,
            hull89,
            vwap,
            vwma21,
            rsi14ema9,
            rsi14ema21,
            ichimokuCloudTop,
            ichimokuCloudBottom,
            ichimokuCloudBaseLine,
            ichimokuCloudConversionLine,
            macd22107,
            macdSignal22107,
            macdHist22107,
            macd26129,
            macdSignal26129,
            macdHist26129,
            macd1153,
            macdSignal1153,
            macdHist1153,
            atr,
            diPositive14,
            dinegative14,
            adx14,
            supertrend103,
            supertrend112,
            bbUpper202,
            bbLower202,
            bbMiddle202);
    }
}




