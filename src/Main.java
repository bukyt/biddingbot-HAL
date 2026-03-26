import java.io.*;
import java.util.*;

public class Main {
    static final long[] THRESHOLDS = {0, 100, 1000, 5000, 25000, 100000, 500000, 2000000, 8000000, 25000000};
    static final double[] ORIG_VALS = {11, 21, 8, 32, 20, 37, 41, 22, 37, 21};
    static final double SPEND_FLOOR = 3_000_000.0;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in), 32768);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out), 32768));

        int initialEBucks = (args.length > 0) ? Integer.parseInt(args[0]) : 10_000_000;
        String myCategoryStr = "Video Games";

        writer.println(myCategoryStr);
        writer.flush();

        HAL9000 apexLogic = new HAL9000(myCategoryStr, initialEBucks);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) continue;
            char firstChar = line.charAt(0);

            if (firstChar == 'v') {
                writer.print("1 ");
                writer.println(apexLogic.calculateBid(line));
                writer.flush();
            } else if (firstChar == 'W') {
                apexLogic.handleResult(true, parseFastInt(line, 2));
            } else if (firstChar == 'L') {
                apexLogic.handleResult(false, 0);
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
        private int rounds = 0;
        private double marketCeiling = 35.0; // Start at a reasonable floor
        private int lastBid = 0;
        private final String myCat;

        HAL9000(String category, int startEb) {
            this.myCat = category;
            this.currentEb = startEb;
        }

        public int calculateBid(String data) {
            this.rounds++;
            if (currentEb <= 0) return 0;

            // Fast View Parsing
            long views = 0;
            int vStart = data.indexOf('=') + 1;
            int vEnd = data.indexOf(',', vStart);
            if (vEnd == -1) vEnd = data.length();
            for (int i = vStart; i < vEnd; i++) {
                char c = data.charAt(i);
                if (c >= '0' && c <= '9') views = views * 10 + (c - '0');
            }

            // Threshold Lookup
            double base = ORIG_VALS[0];
            for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
                if (views >= THRESHOLDS[i]) {
                    base = ORIG_VALS[i];
                    break;
                }
            }

            boolean isMyCat = data.contains(myCat);
            double match = isMyCat ? 1.0 : 0.161;
            double estValue = base * match * 1.9; // Aggressive value estimate

            int bid;
            if (totalSpent < SPEND_FLOOR) {
                // AGGRESSIVE EARLY PHASE
                if (isMyCat || estValue > 25) {
                    bid = (int)marketCeiling + 2;
                } else {
                    bid = 10; // Low-ball the trash
                }
            } else {
                // STABLE EFFICIENT PHASE
                bid = (int)(estValue * 0.92);
            }

            this.lastBid = Math.min(currentEb, bid);
            return Math.max(1, this.lastBid);
        }

        public void handleResult(boolean won, int paid) {
            // LEARNING RATE DECAY: High at round 1, Low at round 100k
            // Starts at 12.0 and drops to 0.5
            double learningRate = Math.max(0.5, 12.0 * (1.0 - (double)rounds / 80000.0));

            if (won) {
                this.currentEb -= paid;
                this.totalSpent += paid;
                // If we win, slowly lower the ceiling to stay efficient
                if (totalSpent < SPEND_FLOOR) {
                    marketCeiling -= (learningRate * 0.1);
                }
            } else {
                // If we lose, jump the ceiling aggressively in the beginning
                if (totalSpent < SPEND_FLOOR) {
                    marketCeiling += learningRate;
                }
            }

            // Safety cap for the ceiling
            if (marketCeiling > 200) marketCeiling = 200;
            if (marketCeiling < 10) marketCeiling = 10;
        }
    }
}