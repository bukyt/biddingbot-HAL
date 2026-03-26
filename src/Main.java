import java.io.*;
import java.util.*;

public class Main {
    static final long[] THRESHOLDS = {0, 100, 1000, 5000, 25000, 100000, 500000, 2000000, 8000000, 25000000};
    static final double[] ORIG_VALS = {11, 21, 8, 32, 20, 37, 41, 22, 37, 21};

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in), 32768);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out), 32768));

        int initialEBucks = (args.length > 0) ? Integer.parseInt(args[0]) : 10_000_000;
        double dynamicSpendFloor = initialEBucks * 0.30;
        String myCategoryStr = "Video Games";

        writer.println(myCategoryStr);
        writer.flush();

        HAL9000 bot = new HAL9000(myCategoryStr, initialEBucks, dynamicSpendFloor);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) continue;
            char firstChar = line.charAt(0);

            if (firstChar == 'v') {
                writer.print("1 ");
                writer.println(bot.calculateBid(line));
                writer.flush();
            } else if (firstChar == 'W') {
                // NEW: Capture BOTH paid and points: "W [paid] [points]"
                int firstSpace = line.indexOf(' ');
                int secondSpace = line.indexOf(' ', firstSpace + 1);
                int paid = parseFastInt(line, firstSpace + 1);
                int points = (secondSpace != -1) ? parseFastInt(line, secondSpace + 1) : (int)(paid * 0.5);
                bot.handleResult(true, paid, points);
            } else if (firstChar == 'L') {
                bot.handleResult(false, parseFastInt(line, 2), 0);
            }
        }
    }

    private static int parseFastInt(String s, int offset) {
        int res = 0;
        for (int i = offset; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') break;
            res = res * 10 + (c - '0');
        }
        return res;
    }

    static class HAL9000 {
        private int currentEb;
        private int totalSpent = 0;
        private double totalValue = 0;
        private int rounds = 0;
        private double marketCeiling = 65.0;
        private final String myCat;
        private final double spendFloor;

        HAL9000(String category, int startEb, double spendFloor) {
            this.myCat = category;
            this.currentEb = startEb;
            this.spendFloor = spendFloor;
        }

        public int calculateBid(String data) {
            this.rounds++;
            if (currentEb <= 0) return 1;

            long views = 0;
            int vStart = data.indexOf('=') + 1;
            int vEnd = data.indexOf(',', vStart);
            if (vEnd == -1) vEnd = data.length();
            for (int i = vStart; i < vEnd; i++) {
                char c = data.charAt(i);
                if (c >= '0' && c <= '9') views = views * 10 + (c - '0');
            }

            double base = ORIG_VALS[0];
            for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
                if (views >= THRESHOLDS[i]) {
                    base = ORIG_VALS[i];
                    break;
                }
            }

            boolean isMyCat = data.contains(myCat) || data.contains("VIDEO_GAMES");
            double match = isMyCat ? 1.0 : 0.161;
            double expectedVal = base * match * 1.8;

            double currentScore = totalValue / Math.max(totalSpent, spendFloor);

            int bid;
            if (totalSpent < spendFloor) {
                // PHASE 1: BULLY
                // If we match cat or it's high base, use the ceiling
                if (isMyCat || base > 25) {
                    bid = (int)marketCeiling + 2;
                } else {
                    bid = 12;
                }
            } else {
                // PHASE 2: "WINNING IS OVERPAYING" MARGIN
                // Break-even at current score is (Val / currentScore).
                // We want to force score growth, so we require 1.5x efficiency.
                int breakEvenBid = (currentScore > 0.05) ? (int)(expectedVal / (currentScore * 1.5)) : (int)expectedVal;

                // Hard caps to ensure we don't accidentally dump 10M
                int selectiveCap = isMyCat ? 55 : 25;
                bid = Math.min(breakEvenBid, selectiveCap);

                if (expectedVal < 5) bid = 1;
            }

            return Math.min(currentEb, Math.max(1, bid));
        }

        public void handleResult(boolean won, int paid, int points) {
            double lr = Math.max(0.2, 15.0 * (1.0 - (double)rounds / 90000.0));

            if (won) {
                this.currentEb -= paid;
                this.totalSpent += paid;
                this.totalValue += points;

                // PHILOSOPHY: If I won, I overpaid. Drop the ceiling immediately.
                if (totalSpent < spendFloor) {
                    marketCeiling -= (lr * 0.25); // Faster downward pressure
                }
            } else {
                // Only raise ceiling if we aren't spending enough to hit the 30% floor
                if (totalSpent < (rounds * (spendFloor / 100000.0))) {
                    marketCeiling += lr;
                }
            }

            if (marketCeiling > 250) marketCeiling = 250;
            if (marketCeiling < 15) marketCeiling = 15;
        }
    }
}