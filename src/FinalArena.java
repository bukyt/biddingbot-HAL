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
    // 1. HAL-9000 (IMPROVED) - Fixed for Arena
    // ==========================================
    static class HAL9000_Improved implements Competitor {
        private int totalSpent = 0;
        private int rounds = 0;
        private double marketCeiling = 65.0; // Start higher to compete immediately
        private final Category myCat = Category.VIDEO_GAMES;

        public String getName() { return "HAL-9000 (Improved)"; }
        public Category getCategory() { return myCat; }

        @Override
        public int[] getBid(String data, int eb) {
            this.rounds++;
            if (eb <= 0) return new int[]{1, 0};

            // Fast parsing
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
            double estValue = base * match * 1.9;

            int bidValue;
            if (totalSpent < SPEND_FLOOR) {
                // AGGRESSION: Bid on anything with decent potential or your own cat
                // Lowered 25 -> 10 so it actually bids the ceiling on most videos
                if (isMyCat || estValue > 10) {
                    bidValue = (int)marketCeiling + 1;
                } else {
                    bidValue = 15; // Increased low-ball to catch cheap outliers
                }
            } else {
                bidValue = (int)(estValue * 0.95);
            }

            return new int[]{1, Math.min(eb, Math.max(1, bidValue))};
        }

        @Override
        public void handleResult(boolean won, int paid, int points, int h) {
            // High initial learning rate for fast adaptation
            double lr = Math.max(0.5, 15.0 * (1.0 - (double)rounds / 70000.0));
            if (won) {
                this.totalSpent += paid;
                if (totalSpent < SPEND_FLOOR) marketCeiling -= (lr * 0.1);
            } else {
                if (totalSpent < SPEND_FLOOR) {
                    // h is the actual highest bid from the winner
                    marketCeiling = Math.max(marketCeiling + lr, h + 1);
                }
            }
            if (marketCeiling > 250) marketCeiling = 250;
        }
    }

    // ==========================================
    // 2. HAL-9000 (ORIGINAL)
    // ==========================================
    static class HAL9000 implements Competitor {
        private int totalSpent = 0;
        private int marketCeiling = 60;
        private final Category myCat = Category.VIDEO_GAMES;
        private final TreeMap<Long, Double> brain = new TreeMap<>();

        HAL9000() { for(int i=0; i<10; i++) brain.put(THRESHOLDS[i], ORIG_VALS[i]); }
        public String getName() { return "HAL-9000 (Original)"; }
        public Category getCategory() { return myCat; }

        public int[] getBid(String data, int eb) {
            if (eb <= 0) return new int[]{1, 0};
            long v = Long.parseLong(data.split("v=")[1].split(",")[0]);
            double base = brain.floorEntry(v).getValue();
            boolean isMyCat = data.contains(myCat.name());
            double estValue = base * (isMyCat ? 1.0 : 0.161) * 1.8;

            int bidValue;
            if (totalSpent < SPEND_FLOOR) {
                bidValue = (isMyCat || estValue > 35) ? marketCeiling + 1 : 12;
            } else {
                bidValue = (int)(estValue * 0.85);
            }
            return new int[]{1, Math.min(eb, bidValue)};
        }

        public void handleResult(boolean won, int paid, int points, int h) {
            if (!won) marketCeiling = Math.max(marketCeiling, h);
            if (won) { totalSpent += paid; }
        }
    }

    // Rest of your bots (Vampire, Bully, etc.) go here...
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
        bots.add(new HAL9000_Improved()); // NOW WORKS
        bots.add(new HAL9000());
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