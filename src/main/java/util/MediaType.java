package util;

public enum MediaType {
    MOVIE("Movie"), EXTRAS("Extras"), TV("Series");

    private final String displayValue;

    private MediaType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}
