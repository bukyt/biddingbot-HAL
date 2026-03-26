public enum AgeBracket {
    AGE_13_17("13-17"),
    AGE_18_24("18-24"),
    AGE_25_34("25-34"),
    AGE_35_44("35-44"),
    AGE_45_54("45-54"),
    AGE_55_PLUS("55+");

    private final String text;

    AgeBracket(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}