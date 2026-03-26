public class Video {
    private VideoCategory category;
    private long viewCount;
    private long commentCount;

    public Video() {
        this.category = VideoCategory.FINANCE; // default safe category
    }

    // Constructor with safe category parsing
    public Video(String nCategory, long nViewCount, long nCommentCount) {
        try {
            if (nCategory != null && !nCategory.isBlank()) {
                category = VideoCategory.valueOf(nCategory.toUpperCase().replaceAll(" ", "_"));
            } else {
                System.err.println("Warning: Video category null or blank, defaulting to FINANCE");
                category = VideoCategory.FINANCE;
            }
        } catch (Exception ex) {
            System.err.println("Warning: Invalid category '" + nCategory + "', defaulting to FINANCE");
            category = VideoCategory.FINANCE;
        }
        viewCount = Math.max(0, nViewCount);
        commentCount = Math.max(0, nCommentCount);
    }

    public VideoCategory getCategory() {
        return category;
    }

    public long getViewCount() {
        return viewCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    // Base value calculation (always positive)
    public double getBaseValue() {
        double base = 1;
        double viewScore;

        if (viewCount < 10_000)
            viewScore = 0.8;
        else if (viewCount < 100_000)
            viewScore = 0.8 - 0.7 * Math.log(viewCount / 10_000.0) / Math.log(10);
        else
            viewScore = 0.1 + 0.9 * Math.log(viewCount / 100_000.0) / Math.log(97_000_000.0 / 100_000.0);

        base = base * (1 + (commentCount * 0.001) / Math.max(viewCount * 0.0001, 1)) * viewScore + 0.01;
        return Math.max(0.01, base);
    }

    // ---------- PARSER ----------
    public static Video parse(String input) {
        String[] fields = input.split(",");
        String categoryStr = null;
        long viewCount = 0;
        long commentCount = 0;

        for (String field : fields) {
            field = field.trim();
            if (field.startsWith("video.viewCount=")) {
                try { viewCount = Long.parseLong(field.split("=")[1]); } catch (Exception ignored) {}
            } else if (field.startsWith("video.category=")) {
                categoryStr = field.split("=")[1];
            } else if (field.startsWith("video.commentCount=")) {
                try { commentCount = Long.parseLong(field.split("=")[1]); } catch (Exception ignored) {}
            }
        }

        if (categoryStr == null || categoryStr.isBlank()) {
            System.err.println("Warning: Video category missing in input, defaulting to FINANCE");
            categoryStr = "FINANCE";
        }

        return new Video(categoryStr, viewCount, commentCount);
    }
}