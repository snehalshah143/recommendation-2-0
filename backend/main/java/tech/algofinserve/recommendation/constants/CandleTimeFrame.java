package tech.algofinserve.recommendation.constants;

public enum CandleTimeFrame {
  ONE_MINUTE("1M"),
  THREE_MINUTE("3M"),
  FIVE_MINUTE("5M"),
  TEN_MINUTE("10M"),
  FIFTEEN_MINUTE("15M"),
  THIRTY_MINUTE("30M"),
  ONE_HOUR("H"),
  SEVENTY_FIVE_MINUTE("75M"),
  MONTH("M"),
  WEEK("W"),
  ONE_DAY("D");

  private final String value;

  private CandleTimeFrame(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public CandleTimeFrame fromValue(String value) {
    CandleTimeFrame[] candleTimeFrames = values();

    for (int i = 0; i < candleTimeFrames.length; ++i) {
      CandleTimeFrame c = candleTimeFrames[i];
      if (c.value.equals(value)) {
        return c;
      }
    }

    throw new IllegalArgumentException(value);
  }
}





