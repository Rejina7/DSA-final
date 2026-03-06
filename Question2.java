public class Question2 {

    private int maxSum;

    // Returns maximum path sum in binary tree
    public int maxPathSum(TreeNode root) {
        maxSum = Integer.MIN_VALUE;
        dfs(root);
        return maxSum;
    }

    // Returns max sum path starting from this node going down
    private int dfs(TreeNode node) {
        if (node == null) {
            return 0;
        }

        int leftMax = Math.max(0, dfs(node.left));   // Ignore negative paths
        int rightMax = Math.max(0, dfs(node.right));

        // Path through this node as the peak
        maxSum = Math.max(maxSum, node.val + leftMax + rightMax);

        // Return max path going down one side only
        return node.val + Math.max(leftMax, rightMax);
    }

    public static void main(String[] args) {
        Question2 solution = new Question2();

        // Example 1: [1, 2, 3]
        TreeNode root1 = new TreeNode(1);
        root1.left = new TreeNode(2);
        root1.right = new TreeNode(3);
        System.out.println("Example 1:");
        System.out.println("Input: root = [1, 2, 3]");
        System.out.println("Output: " + solution.maxPathSum(root1));
        System.out.println("Expected: 6 (path: 2 -> 1 -> 3)");
        System.out.println();

        // Example 2: [-10, 9, 20, null, null, 15, 7]
        TreeNode root2 = new TreeNode(-10);
        root2.left = new TreeNode(9);
        root2.right = new TreeNode(20);
        root2.right.left = new TreeNode(15);
        root2.right.right = new TreeNode(7);
        System.out.println("Example 2:");
        System.out.println("Input: root = [-10, 9, 20, null, null, 15, 7]");
        System.out.println("Output: " + solution.maxPathSum(root2));
        System.out.println("Expected: 42 (path: 15 -> 20 -> 7)");
    }
}

class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val) {
        this.val = val;
    }
}
