package tech.algofinserve.recommendation.constants;

public enum RecommendationValidity {

    INTRADAY("1 Day"),
    MOMENTUM("3-5 Days"),
    POSITIONAL("1 week to 1 Month"),
    SHORT_TERM("3 Months"),
    MEDIUM_TERM("6 Months"),
    LONG_TERM("2 Year");

    private final  String value;
    private RecommendationValidity(String value){
        this.value=value;

    }
    public String value(){return this.value;}

    public RecommendationValidity fromValue(String value){
        RecommendationValidity[] recommendationTimeframes=values();

        for(int i = 0; i < recommendationTimeframes.length; ++i) {
            RecommendationValidity c = recommendationTimeframes[i];
            if (c.value.equals(value)) {
                return c;
            }
        }

        throw new IllegalArgumentException(value);
    }


}
