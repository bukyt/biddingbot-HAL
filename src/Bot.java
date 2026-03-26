class Bot {
    private final VideoCategory myCategory;
    private Video video;
    private Viewer viewer;
    private final int initialEBucks;
    private int eBucks;

    private int exponentialBid = 5000;
    private int firstWinningBid = -1;

    public Bot(VideoCategory category, Video video, Viewer viewer, int eBucks) {
        this.myCategory = category != null ? category : VideoCategory.FINANCE;
        this.video = video;
        this.viewer = viewer;
        this.eBucks = eBucks;
        this.initialEBucks = eBucks;
    }

    // New method to update context every round
    public void setContext(Video video, Viewer viewer) {
        this.video = video;
        this.viewer = viewer;
    }

    public int[] bid() {
        if (eBucks <= 0) return new int[]{0, 0};

        double spentRatio = (double) (initialEBucks - eBucks) / initialEBucks;
        // Logic for getting value
        double videoValue = (video != null) ? video.getBaseValue() : 1.0;

        // --- PHASE 1: EXPONENTIAL BUFFER ATTACK ---
        int targetMaxBid;
        if (spentRatio < 0.35) {
            targetMaxBid = (int)(exponentialBid * (videoValue / 2.0) * videoValue);
        }
        // --- PHASE 2: SETTLE & RECOVER ---
        else if (spentRatio < 0.65) {
            targetMaxBid = (firstWinningBid > 0)
                    ? (int)(firstWinningBid * (videoValue / 5.0) * videoValue)
                    : 5000;
        }
        // --- PHASE 3: CONSERVATIVE ---
        else {
            targetMaxBid = Math.max(1, (int)(eBucks * 0.01));
        }

        if (video != null && video.getCategory() == myCategory) {
            targetMaxBid *= 1.5;
        }

        int maxBid = Math.min(eBucks, targetMaxBid);
        int startBid = (videoValue > 5.0) ? Math.min(maxBid, (int)(maxBid * 0.8)) : 1;

        return new int[]{startBid, Math.max(1, maxBid)};
    }

    // Updated to fix the parsing of "W {cost}"
    public void score(String input) {
        String[] parts = input.split(" ");
        String result = parts[0]; // "W" or "L"
        double spentRatio = (double) (initialEBucks - eBucks) / initialEBucks;

        if ("W".equals(result)) {
            int spent = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            eBucks -= spent;
            if (firstWinningBid == -1) firstWinningBid = spent;
            if (spentRatio < 0.35) exponentialBid = 5000;
        } else if ("L".equals(result)) {
            if (spentRatio < 0.35) {
                exponentialBid *= 3;
                int cap = (int)(initialEBucks * 0.15);
                if (exponentialBid > cap) exponentialBid = cap;
            }
        }
    }
}