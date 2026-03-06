import java.util.*;

public class Question8 {

    // Returns all possible word break combinations - uses backtracking with memoization
    public List<String> wordBreak(String userQuery, List<String> marketingKeywordsDictionary) {
        Set<String> wordSet = new HashSet<>(marketingKeywordsDictionary);
        Map<Integer, List<String>> memo = new HashMap<>();
        return backtrack(userQuery, 0, wordSet, memo);
    }

    private List<String> backtrack(String s, int start, Set<String> wordSet, Map<Integer, List<String>> memo) {
        if (memo.containsKey(start)) {
            return memo.get(start);
        }

        List<String> result = new ArrayList<>();

        if (start == s.length()) {
            result.add("");
            return result;
        }

        for (int end = start + 1; end <= s.length(); end++) {
            String word = s.substring(start, end);
            if (wordSet.contains(word)) {
                List<String> suffixes = backtrack(s, end, wordSet, memo);
                for (String suffix : suffixes) {
                    if (suffix.isEmpty()) {
                        result.add(word);
                    } else {
                        result.add(word + " " + suffix);
                    }
                }
            }
        }

        memo.put(start, result);
        return result;
    }

    public static void main(String[] args) {
        Question8 solution = new Question8();

        // Example 1
        String userQuery1 = "nepaltrekkingguide";
        List<String> dict1 = Arrays.asList("nepal", "trekking", "guide", "nepaltrekking");
        System.out.println("Example 1:");
        System.out.println("Input: user_query = \"" + userQuery1 + "\"");
        System.out.println("Dictionary: " + dict1);
        System.out.println("Output: " + solution.wordBreak(userQuery1, dict1));
        System.out.println("Expected: [nepal trekking guide, nepaltrekking guide]");
        System.out.println();

        // Example 2
        String userQuery2 = "visitkathmandunepal";
        List<String> dict2 = Arrays.asList("visit", "kathmandu", "nepal", "visitkathmandu", "kathmandunepal");
        System.out.println("Example 2:");
        System.out.println("Input: user_query = \"" + userQuery2 + "\"");
        System.out.println("Dictionary: " + dict2);
        System.out.println("Output: " + solution.wordBreak(userQuery2, dict2));
        System.out.println("Expected: [visit kathmandu nepal, visitkathmandu nepal, visit kathmandunepal]");
        System.out.println();

        // Example 3
        String userQuery3 = "everesthikingtrail";
        List<String> dict3 = Arrays.asList("everest", "hiking", "trek");
        System.out.println("Example 3:");
        System.out.println("Input: user_query = \"" + userQuery3 + "\"");
        System.out.println("Dictionary: " + dict3);
        System.out.println("Output: " + solution.wordBreak(userQuery3, dict3));
        System.out.println("Expected: []");
    }
}
