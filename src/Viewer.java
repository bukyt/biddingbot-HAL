import java.util.LinkedList;

public class Viewer {
    boolean subscribed;
    AgeBracket age;
    LinkedList<VideoCategory> interests;

    public Viewer(boolean subscribed, AgeBracket age, LinkedList<VideoCategory> interests) {
        this.subscribed = subscribed;
        this.age = age;
        this.interests = interests;
    }

    public float getAgeValue() {
        switch (age) {
            case AGE_13_17: return 1;
            case AGE_18_24: return 1.5F;
            case AGE_25_34: return 2;
            case AGE_35_44: return 1.5F;
            case AGE_45_54: return 1.25F;
            case AGE_55_PLUS: return 1.0F;
            default: return 0;
        }
    }

    // ----------- PARSER -----------
    public static Viewer parse(String input) {
        /**
         * input example:
         * "video.viewCount=12345,video.category=Kids,video.commentCount=987,viewer.subscribed=Y,viewer.age=18-24,viewer.gender=F,viewer.interests=Video Games;Music"
        */
        boolean subscribed = false;
        AgeBracket age = null;
        LinkedList<VideoCategory> interests = new LinkedList<>();

        String[] fields = input.split(",");
        for (String field : fields) {
            field = field.trim();
            if (field.startsWith("viewer.subscribed=")) {
                String val = field.split("=")[1].trim();
                subscribed = val.equalsIgnoreCase("Y");
            } else if (field.startsWith("viewer.age=")) {
                String val = field.split("=")[1].trim();
                switch (val) {
                    case "13-17": age = AgeBracket.AGE_13_17; break;
                    case "18-24": age = AgeBracket.AGE_18_24; break;
                    case "25-34": age = AgeBracket.AGE_25_34; break;
                    case "35-44": age = AgeBracket.AGE_35_44; break;
                    case "45-54": age = AgeBracket.AGE_45_54; break;
                    case "55+":   age = AgeBracket.AGE_55_PLUS; break;
                    default: throw new IllegalArgumentException("Unknown age bracket: " + val);
                }
            } else if (field.startsWith("viewer.interests=")) {
                String val = field.split("=")[1].trim();
                String[] cats = val.split(";");
                for (String c : cats) {
                    interests.add(VideoCategory.valueOf(c.toUpperCase().replace(" ", "_")));
                }
            }
        }

        if (age == null) {
            throw new IllegalArgumentException("Age missing in input: " + input);
        }

        return new Viewer(subscribed, age, interests);
    }
}