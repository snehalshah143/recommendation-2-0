package tech.algofinserve.recommendation.constants;

public enum InstrumentType {
  EQ("EQ"),
  INDEX("INDEX"),
  FUTIDX("FUTIDX"),
  FUTSTK("FUTSTK"),
  OPTSTK("OPTSTK"),
  OPTIDX("OPTIDX");

  private final String value;

  private InstrumentType(String value) {
    this.value = value;
  }

  public String value() {
    return this.value;
  }

  public InstrumentType fromValue(String value) {
    InstrumentType[] instrumentTypes = values();

    for (int i = 0; i < instrumentTypes.length; ++i) {
      InstrumentType c = instrumentTypes[i];
      if (c.value.equals(value)) {
        return c;
      }
    }

    throw new IllegalArgumentException(value);
  }
}
