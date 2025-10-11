package tech.algofinserve.recommendation.constants;

public enum ScannerName {
//BASE
    Snehal_BUY_Monthly_MULTIBAGGERBASE("Snehal_BUY_Monthly_MULTIBAGGERBASE");


    private final  String value;
    private ScannerName(String value){
        this.value=value;

    }
    public String value(){return this.value;}

    public ScannerName fromValue(String value){
        ScannerName[] scannerNames=values();

        for(int i = 0; i < scannerNames.length; ++i) {
            ScannerName c = scannerNames[i];
            if (c.value.equals(value)) {
                return c;
            }
        }

        throw new IllegalArgumentException(value);
    }


}
