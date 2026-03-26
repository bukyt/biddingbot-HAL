import java.util.*;

public class FinalArena {
    static final int TOTAL_ROUNDS = 100000;
    static final int INITIAL_EBUCKS = 10_000_000;
    static final double SPEND_FLOOR = INITIAL_EBUCKS * 0.3;

    enum Category { MUSIC, SPORTS, KIDS, DIY, VIDEO_GAMES, ASMR, BEAUTY, COOKING, FINANCE }
    static final double[] ORIG_VALS = {11, 21, 8, 32, 20, 37, 41, 22, 37, 21};
    static double[] SHUFFLED_VALUES = new double[10];
    static final long[] THRESHOLDS = {0, 100, 1000, 5000, 25000, 100000, 500000, 2000000, 8000000, 25000000};

    interface Competitor {
        String getName();
        Category getCategory();
        int[] getBid(String videoData, int currentEb);
        void handleResult(boolean won, int paid, int points, int highestOtherBid);
    }

    // ==========================================
    // 1. HAL-9000 (MARGINAL UTILITY)
    // ==========================================
    static class HAL9000_Improved implements Competitor {
        private double totalPoints = 0;
        private int totalSpent = 0;
        private int rounds = 0;
        private double marketCeiling = 65.0;
        private final Category myCat = Category.VIDEO_GAMES;

        public String getName() { return "HAL-9000 (Marginal-Utility)"; }
        public Category getCategory() { return myCat; }

        @Override
        public int[] getBid(String data, int eb) {
            this.rounds++;
            if (eb <= 0) return new int[]{1, 0};

            // Fast Parsing
            long views = 0;
            int vStart = data.indexOf('=') + 1;
            int vEnd = data.indexOf(',', vStart);
            if (vEnd == -1) vEnd = data.length();
            for (int i = vStart; i < vEnd; i++) {
                char c = data.charAt(i);
                if (c >= '0' && c <= '9') views = views * 10 + (c - '0');
            }

            double base = 11;
            for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
                if (views >= THRESHOLDS[i]) {
                    base = ORIG_VALS[i];
                    break;
                }
            }

            boolean isMyCat = data.contains(myCat.name());
            double match = isMyCat ? 1.0 : 0.161;
            double expectedVal = base * match * 1.8;

            // Current Efficiency Score
            double currentScore = totalPoints / Math.max(totalSpent, SPEND_FLOOR);

            int bidValue;
            if (totalSpent < SPEND_FLOOR) {
                // PHASE 1: BULLY (Fast 30% spend)
                if (isMyCat || base > 25) {
                    bidValue = (int)marketCeiling + 2;
                } else {
                    bidValue = 12;
                }
            } else {
                // PHASE 2: MARGINAL GAIN (Winning = Overpaying)
                // We demand a 1.5x efficiency jump over our current average
                int breakEvenBid = (currentScore > 0.05) ? (int)(expectedVal / (currentScore * 1.5)) : (int)expectedVal;

                // Hard caps to ensure denominator protection
                int selectiveCap = isMyCat ? 55 : 25;
                bidValue = Math.min(breakEvenBid, selectiveCap);

                if (expectedVal < 5) bidValue = 1;
            }

            return new int[]{1, Math.min(eb, Math.max(1, bidValue))};
        }

        @Override
        public void handleResult(boolean won, int paid, int points, int h) {
            double lr = Math.max(0.2, 15.0 * (1.0 - (double)rounds / 90000.0));
            if (won) {
                this.totalSpent += paid;
                this.totalPoints += points;
                // Win-Shaving: If we won, we likely overpaid. Pull ceiling down.
                if (totalSpent < SPEND_FLOOR) marketCeiling -= (lr * 0.25);
            } else {
                // Only push the ceiling up if we are lagging behind the 30% floor pace
                double targetPace = rounds * (SPEND_FLOOR / TOTAL_ROUNDS);
                if (totalSpent < targetPace) {
                    marketCeiling = Math.max(marketCeiling + lr, h + 1);
                }
            }
            if (marketCeiling > 250) marketCeiling = 250;
            if (marketCeiling < 15) marketCeiling = 15;
        }
    }

    static class VampireBot implements Competitor {
        private int marketCeiling = 60;
        public String getName() { return "The-Vampire"; }
        public Category getCategory() { return Category.COOKING; }
        public int[] getBid(String data, int eb) {
            if (eb <= 0) return new int[]{1, 0};
            return new int[]{1, Math.min(eb, marketCeiling - 1)};
        }
        public void handleResult(boolean w, int p, int pts, int h) { marketCeiling = Math.max(marketCeiling, h); }
    }

    static class AdaptiveBully implements Competitor {
        private int bidBase = 60;
        public String getName() { return "Adaptive-Bully"; }
        public Category getCategory() { return Category.DIY; }
        public int[] getBid(String data, int eb) {
            return new int[]{1, Math.min(eb, bidBase)};
        }
        public void handleResult(boolean won, int paid, int pts, int h) {
            if (!won && h < 150) bidBase += 1;
            if (won && bidBase > 50) bidBase -= 1;
        }
    }

    static class StaticCompetitor implements Competitor {
        String name; Category cat; int bid;
        StaticCompetitor(String n, Category c, int b) { name=n; cat=c; bid=b; }
        public String getName() { return name; }
        public Category getCategory() { return cat; }
        public int[] getBid(String d, int eb) { return new int[]{1, Math.min(eb, bid)}; }
        public void handleResult(boolean w, int p, int pts, int h) {}
    }

    static class Dumb0Bot implements Competitor {
        private Random rnd = new Random();
        public String getName() { return "Dumb0"; }
        public Category getCategory() { return Category.KIDS; }
        public int[] getBid(String d, int eb) { return new int[]{1, Math.min(eb, rnd.nextInt(55)+1)}; }
        public void handleResult(boolean w, int p, int pts, int h) {}
    }

    public static void main(String[] args) {
        Random marketRng = new Random();
        for (int i = 0; i < ORIG_VALS.length; i++) {
            SHUFFLED_VALUES[i] = ORIG_VALS[i] * (0.8 + marketRng.nextDouble() * 0.4);
        }

        List<Competitor> bots = new ArrayList<>();
        bots.add(new HAL9000_Improved());
        bots.add(new VampireBot());
        bots.add(new AdaptiveBully());
        bots.add(new StaticCompetitor("Steady-45", Category.SPORTS, 45));
        bots.add(new Dumb0Bot());

        Map<Competitor, Double> totalPoints = new HashMap<>();
        Map<Competitor, Integer> totalSpent = new HashMap<>();
        Map<Competitor, Integer> currentEB = new HashMap<>();

        for(Competitor b : bots) {
            totalPoints.put(b, 0.0); totalSpent.put(b, 0); currentEB.put(b, INITIAL_EBUCKS);
        }

        for (int r = 0; r < TOTAL_ROUNDS; r++) {
            Category vidCat = Category.values()[marketRng.nextInt(Category.values().length)];
            long views = (long)Math.pow(10, marketRng.nextDouble() * 8);
            String data = "v=" + views + ",cat=" + vidCat.name();

            int highestBid = -1; int secondHighest = 0; Competitor winner = null;

            for (Competitor b : bots) {
                int eb = currentEB.get(b);
                if (eb <= 0) continue;
                int[] bids = b.getBid(data, eb);
                if (bids[1] > highestBid) {
                    secondHighest = highestBid; highestBid = bids[1]; winner = b;
                } else if (bids[1] > secondHighest) {
                    secondHighest = bids[1];
                }
            }

            if (winner != null) {
                double base = 11;
                for(int i=0; i<THRESHOLDS.length; i++) if(views >= THRESHOLDS[i]) base = SHUFFLED_VALUES[i];
                double match = (winner.getCategory() == vidCat) ? 1.0 : 0.161;
                double pts = Math.ceil(base * match * (1.1 + marketRng.nextDouble() * 1.4));

                totalPoints.put(winner, totalPoints.get(winner) + pts);
                totalSpent.put(winner, totalSpent.get(winner) + highestBid);
                currentEB.put(winner, currentEB.get(winner) - highestBid);

                for(Competitor b : bots) {
                    b.handleResult(b == winner, highestBid, (int)pts, (b == winner) ? secondHighest : highestBid);
                }
            }
        }

        bots.sort((a, b) -> {
            double scoreA = totalPoints.get(a) / Math.max(totalSpent.get(a), SPEND_FLOOR);
            double scoreB = totalPoints.get(b) / Math.max(totalSpent.get(b), SPEND_FLOOR);
            return Double.compare(scoreB, scoreA);
        });

        System.out.println("RANK | BOT NAME               | POINTS    | SPENT     | ROI SCORE");
        System.out.println("------------------------------------------------------------------");
        for (int i = 0; i < bots.size(); i++) {
            Competitor b = bots.get(i);
            double score = totalPoints.get(b) / Math.max(totalSpent.get(b), SPEND_FLOOR);
            System.out.printf("%4d | %-22s | %9.0f | %9d | %8.6f\n",
                    i+1, b.getName(), totalPoints.get(b), totalSpent.get(b), score);
        }
    }
}