package tech.algofinserve.recommendation.constants;

public enum ExchSeg {
  NSE("NSE"),
  BSE("BSE"),
  NFO("NFO");

  private final String value;

  private ExchSeg(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public ExchSeg fromValue(String value) {
    ExchSeg[] exchSegs = values();

    for (int i = 0; i < exchSegs.length; ++i) {
      ExchSeg c = exchSegs[i];
      if (c.value.equals(value)) {
        return c;
      }
    }

    throw new IllegalArgumentException(value);
  }
}





