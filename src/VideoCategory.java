public enum VideoCategory {
    //https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
    MUSIC("Music"),
    SPORTS("Sports"),
    KIDS("Kids"),
    DIY("DIY"),
    VIDEO_GAMES("Video Games"),
    ASMR("ASMR"),
    BEAUTY("Beauty"),
    COOKING("Cooking"),
    FINANCE("Finance")
    ;

    private final String text;

    /**
     * @param text
     */
    VideoCategory(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}
