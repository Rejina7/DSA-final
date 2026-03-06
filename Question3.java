import java.util.HashMap;
import java.util.Map;

public class Question3 {

    // Returns maximum number of points on the same line - O(n²) time, O(n) space
    public int maxPoints(int[][] customerLocations) {
        int n = customerLocations.length;
        
        if (n <= 2) {
            return n;
        }
        
        int maxPoints = 1;
        
        for (int i = 0; i < n; i++) {
            Map<String, Integer> slopeCount = new HashMap<>();
            int duplicate = 0;
            int currentMax = 0;
            
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                
                int dx = customerLocations[j][0] - customerLocations[i][0];
                int dy = customerLocations[j][1] - customerLocations[i][1];
                
                if (dx == 0 && dy == 0) {
                    duplicate++;
                    continue;
                }
                
                int g = gcd(Math.abs(dx), Math.abs(dy));
                dx /= g;
                dy /= g;
                
                // Normalize slope direction
                if (dx < 0 || (dx == 0 && dy < 0)) {
                    dx = -dx;
                    dy = -dy;
                }
                
                String slope = dx + "," + dy;
                slopeCount.put(slope, slopeCount.getOrDefault(slope, 0) + 1);
                currentMax = Math.max(currentMax, slopeCount.get(slope));
            }
            
            maxPoints = Math.max(maxPoints, currentMax + duplicate + 1);
        }
        
        return maxPoints;
    }
    
    private int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }
    
    public static void main(String[] args) {
        Question3 solution = new Question3();
        
        int[][] customerLocations1 = {{1, 1}, {2, 2}, {3, 3}};
        int result1 = solution.maxPoints(customerLocations1);
        System.out.println("Example 1:");
        System.out.println("Input: customer_locations = [[1,1],[2,2],[3,3]]");
        System.out.println("Output: " + result1);
        System.out.println("Expected: 3");
        System.out.println("Explanation: All three customer homes lie on the same diagonal line (y = x)");
        System.out.println();
        
        int[][] customerLocations2 = {{1, 1}, {3, 2}, {5, 3}, {4, 1}, {2, 3}, {1, 4}};
        int result2 = solution.maxPoints(customerLocations2);
        System.out.println("Example 2:");
        System.out.println("Input: customer_locations = [[1,1],[3,2],[5,3],[4,1],[2,3],[1,4]]");
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println("Explanation: Points [1,4], [2,3], [3,2], [4,1] lie on the line y = -x + 5");
        System.out.println();
        
        System.out.println("Additional Test Cases:");
        System.out.println("--------------------------------------------------");
        
        int[][] test3 = {{0, 0}};
        System.out.println("Single point [[0,0]]: " + solution.maxPoints(test3) + " (Expected: 1)");
        
        int[][] test4 = {{0, 0}, {5, 5}};
        System.out.println("Two points [[0,0],[5,5]]: " + solution.maxPoints(test4) + " (Expected: 2)");
        
        int[][] test5 = {{1, 1}, {1, 2}, {1, 3}, {2, 2}};
        System.out.println("Vertical line [[1,1],[1,2],[1,3],[2,2]]: " + solution.maxPoints(test5) + " (Expected: 3)");
        
        int[][] test6 = {{1, 1}, {2, 1}, {3, 1}, {2, 2}};
        System.out.println("Horizontal line [[1,1],[2,1],[3,1],[2,2]]: " + solution.maxPoints(test6) + " (Expected: 3)");
        
        int[][] test7 = {{1, 1}, {1, 1}, {2, 2}, {3, 3}};
        System.out.println("With duplicates [[1,1],[1,1],[2,2],[3,3]]: " + solution.maxPoints(test7) + " (Expected: 4)");
    }
}

