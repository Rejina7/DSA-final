public class Question5 {

    // Returns maximum profit with at most max_trades transactions
    public int maxProfit(int maxTrades, int[] dailyPrices) {
        int n = dailyPrices.length;
        if (n == 0 || maxTrades == 0) {
            return 0;
        }

        // If max_trades >= n/2, we can capture all profits
        if (maxTrades >= n / 2) {
            int profit = 0;
            for (int i = 1; i < n; i++) {
                if (dailyPrices[i] > dailyPrices[i - 1]) {
                    profit += dailyPrices[i] - dailyPrices[i - 1];
                }
            }
            return profit;
        }

        // dp[t][d] = max profit using at most t transactions up to day d
        int[][] dp = new int[maxTrades + 1][n];

        for (int t = 1; t <= maxTrades; t++) {
            int maxDiff = -dailyPrices[0];
            for (int d = 1; d < n; d++) {
                dp[t][d] = Math.max(dp[t][d - 1], dailyPrices[d] + maxDiff);
                maxDiff = Math.max(maxDiff, dp[t - 1][d] - dailyPrices[d]);
            }
        }

        return dp[maxTrades][n - 1];
    }

    public static void main(String[] args) {
        Question5 solution = new Question5();

        // Example 1
        int[] prices1 = {2000, 4000, 1000};
        int maxTrades1 = 2;
        System.out.println("Example 1:");
        System.out.println("Input: max_trades = " + maxTrades1 + ", daily_prices = [2000, 4000, 1000]");
        System.out.println("Output: " + solution.maxProfit(maxTrades1, prices1));
        System.out.println("Expected: 2000 (Buy at 2000, sell at 4000)");
        System.out.println();

        // Example 2
        int[] prices2 = {3000, 2000, 6000, 5000, 1000, 3000};
        int maxTrades2 = 2;
        System.out.println("Example 2:");
        System.out.println("Input: max_trades = " + maxTrades2 + ", daily_prices = [3000, 2000, 6000, 5000, 1000, 3000]");
        System.out.println("Output: " + solution.maxProfit(maxTrades2, prices2));
        System.out.println("Expected: 6000 (Buy at 2000, sell at 6000; Buy at 1000, sell at 3000)");
        System.out.println();

        // Example 3: Single transaction
        int[] prices3 = {1000, 2000, 3000, 4000, 5000};
        int maxTrades3 = 1;
        System.out.println("Example 3:");
        System.out.println("Input: max_trades = " + maxTrades3 + ", daily_prices = [1000, 2000, 3000, 4000, 5000]");
        System.out.println("Output: " + solution.maxProfit(maxTrades3, prices3));
        System.out.println("Expected: 4000 (Buy at 1000, sell at 5000)");
        System.out.println();

        // Example 4: No profit possible
        int[] prices4 = {5000, 4000, 3000, 2000, 1000};
        int maxTrades4 = 2;
        System.out.println("Example 4:");
        System.out.println("Input: max_trades = " + maxTrades4 + ", daily_prices = [5000, 4000, 3000, 2000, 1000]");
        System.out.println("Output: " + solution.maxProfit(maxTrades4, prices4));
        System.out.println("Expected: 0 (Prices only decrease)");
    }
}

