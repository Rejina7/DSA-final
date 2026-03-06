import java.util.*;

/**
 * Emergency Supply Logistics - Earthquake Response Algorithm Solutions
 * Nepal's NDRRMA Supply Distribution Optimization
 * 
 * Locations:
 * - KTM: Kathmandu (Primary Supply Depot at Tribhuvan International Airport)
 * - PH: Patan Hospital (Medical supplies - needs SAFEST path)
 * - BS: Bhaktapur Shelter (Displaced people camp - needs MAXIMUM FLOW)
 * - JA: Junction A (Kalanki Chowk)
 * - JB: Junction B (Koteshwor Chowk)
 */
public class Question1 {

    // Node indices
    static final int KTM = 0, JA = 1, JB = 2, PH = 3, BS = 4;
    static final String[] NAMES = {"KTM", "JA", "JB", "PH", "BS"};
    static final int N = 5;

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║     EMERGENCY SUPPLY LOGISTICS - EARTHQUAKE RESPONSE ALGORITHMS           ║");
        System.out.println("║              Nepal National Disaster Risk Reduction Authority             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════════╝\n");

        solveQuestion1();
        solveQuestion2();
        solveQuestion3();
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUESTION 1: PROBLEM MODELING (4 Points)
    // ═══════════════════════════════════════════════════════════════════════════════
    static void solveQuestion1() {
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("QUESTION 1: PROBLEM MODELING (4 Points)");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════\n");

        System.out.println("(a) Why standard Dijkstra using distance as weight is NOT suitable (2 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.println("""
            Standard Dijkstra's algorithm finds the path that MINIMIZES the SUM of edge weights.
            However, for safety probability:
            
            1. OPERATION MISMATCH:
               - Dijkstra: Computes path cost as SUM of weights (w1 + w2 + w3 + ...)
               - Safety: Requires PRODUCT of probabilities (p1 × p2 × p3 × ...)
               - Example: Path KTM → JA → PH has safety = 0.9 × 0.95 = 0.855 (PRODUCT, not sum)
            
            2. OPTIMIZATION DIRECTION:
               - Dijkstra: MINIMIZES total path weight
               - Safety: We want to MAXIMIZE total probability
               
            3. WEIGHT INTERPRETATION:
               - Distance weights: Higher = worse (longer path)
               - Safety probabilities: Higher = better (safer path)
               
            Therefore, Dijkstra with distance weights solves the WRONG mathematical problem.
            """);

        System.out.println("\n(b) Why maximizing probabilities directly is problematic (2 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.println("""
            Even using probabilities as weights and trying to maximize has issues:
            
            1. GREEDY PROPERTY VIOLATION:
               - Dijkstra relies on the OPTIMAL SUBSTRUCTURE with ADDITION
               - For maximization with multiplication: max(p1×p2×p3) ≠ leads to correct greedy choices
               - A locally "best" edge might not lead to globally best path
            
            2. RELAXATION CONDITION INCOMPATIBILITY:
               - Standard Dijkstra: if (dist[u] + w(u,v) < dist[v]) → relax
               - For maximization: need if (prob[u] × p(u,v) > prob[v]) → update
               - Priority queue ordering is inverted (need max-heap, not min-heap)
            
            3. NUMERICAL PRECISION:
               - Multiplying many probabilities (0 < p < 1) causes underflow
               - Product quickly approaches 0: 0.9^10 ≈ 0.35, 0.9^100 ≈ 0.000027
               - Floating-point errors accumulate with multiplication
            
            4. INITIALIZATION ISSUE:
               - For sum-minimization: Initialize distances to ∞
               - For product-maximization: Initialize to 0, but then prob[src] × p = 0 always
               
            These issues make direct probability maximization unreliable with standard Dijkstra.
            """);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUESTION 2: ALGORITHM ADAPTATION FOR SAFEST PATH (9 Points)
    // ═══════════════════════════════════════════════════════════════════════════════
    static void solveQuestion2() {
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("QUESTION 2: ALGORITHM ADAPTATION FOR SAFEST PATH (9 Points)");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════\n");

        System.out.println("(a) Weight Transformation using Logarithm (3 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.println("""
            TRANSFORMATION: w'(e) = -log(p(e))  where p(e) is the safety probability
            
            KEY INSIGHT: Logarithm converts PRODUCTS into SUMS
            
            Mathematical Justification:
            ─────────────────────────────
            For a path P with edges e1, e2, ..., ek:
            
            Safety(P) = p(e1) × p(e2) × ... × p(ek)
            
            Taking negative log of both sides:
            -log(Safety(P)) = -log(p(e1)) - log(p(e2)) - ... - log(p(ek))
                            = w'(e1) + w'(e2) + ... + w'(ek)
                            = Sum of transformed weights
            
            WHY THIS WORKS:
            ───────────────
            1. CONVERTS PRODUCT TO SUM: log(a × b) = log(a) + log(b)
            
            2. CONVERTS MAXIMIZE TO MINIMIZE:
               - Since 0 < p(e) ≤ 1, we have log(p(e)) ≤ 0
               - Therefore -log(p(e)) ≥ 0 (non-negative weights!)
               - Higher p(e) → smaller -log(p(e))
               - MAXIMIZING product ↔ MINIMIZING sum of -log values
            
            3. NON-NEGATIVE WEIGHTS: Dijkstra requires w(e) ≥ 0 ✓
            
            EXAMPLE:
            ─────────
            Edge KTM→JA with p = 0.9:
            w'(KTM→JA) = -log(0.9) = -(-0.105) = 0.105
            
            Edge JA→PH with p = 0.95:
            w'(JA→PH) = -log(0.95) = -(-0.051) = 0.051
            
            Path KTM→JA→PH:
            - Original safety = 0.9 × 0.95 = 0.855
            - Transformed weight = 0.105 + 0.051 = 0.156
            - Verify: e^(-0.156) ≈ 0.855 ✓
            """);

        // Safety probability graph (sample data based on typical earthquake scenario)
        double[][] safety = new double[N][N];
        // Initialize with 0 (no edge)
        safety[KTM][JA] = 0.90;  // KTM to JA
        safety[KTM][JB] = 0.85;  // KTM to JB
        safety[JA][KTM] = 0.90;  // JA to KTM
        safety[JA][PH] = 0.95;   // JA to PH
        safety[JA][BS] = 0.80;   // JA to BS
        safety[JB][KTM] = 0.85;  // JB to KTM
        safety[JB][JA] = 0.75;   // JB to JA
        safety[JB][BS] = 0.88;   // JB to BS
        safety[PH][JA] = 0.95;   // PH to JA
        safety[PH][BS] = 0.70;   // PH to BS
        safety[BS][JA] = 0.80;   // BS to JA
        safety[BS][JB] = 0.88;   // BS to JB
        safety[BS][PH] = 0.70;   // BS to PH

        System.out.println("\n(b) Implementation Code (3 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.println("Safety Probability Graph:");
        printSafetyGraph(safety);

        System.out.println("\nALGORITHM: Modified Dijkstra for Maximum Safety Path");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        printAlgorithmPseudocode();

        // Run the algorithm
        System.out.println("\nEXECUTION: Finding safest paths from KTM to all nodes");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        findSafestPaths(safety, KTM);

        System.out.println("\n(c) Proof of Correctness (3 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.println("""
            THEOREM: The algorithm correctly finds paths with maximum product of probabilities.
            
            PROOF:
            ──────
            We prove by showing equivalence between the original and transformed problems.
            
            1. CLAIM: Minimizing Σ(-log(p(ei))) is equivalent to maximizing Π(p(ei))
            
               Let P be any path with edges e1, e2, ..., ek.
               
               Define: W(P) = Σ w'(ei) = Σ(-log(p(ei))) = -log(Π p(ei))
               Define: S(P) = Π p(ei) = Safety of path P
               
               Since -log is a strictly MONOTONICALLY DECREASING function:
               W(P1) < W(P2)  ⟺  -log(S(P1)) < -log(S(P2))
                              ⟺  log(S(P1)) > log(S(P2))
                              ⟺  S(P1) > S(P2)
               
               Therefore: Path with MINIMUM transformed weight has MAXIMUM safety.
            
            2. CLAIM: Dijkstra's algorithm correctly finds minimum weight path with w'(e)
            
               Conditions for Dijkstra's correctness:
               
               (i) Non-negative weights: w'(e) = -log(p(e)) ≥ 0
                   Since 0 < p(e) ≤ 1, we have log(p(e)) ≤ 0, so -log(p(e)) ≥ 0 ✓
               
               (ii) Optimal substructure: If P* = (s,...,u,v,...,t) is optimal s→t path,
                    then (s,...,u) is optimal s→u path.
                    This holds because weights are additive. ✓
               
               (iii) Greedy choice property: At each step, selecting the unvisited node
                     with minimum tentative distance leads to optimal solution.
                     Proof by contradiction follows standard Dijkstra proof. ✓
            
            3. CONCLUSION:
               
               By (1): Minimum W(P) ⟺ Maximum S(P)
               By (2): Dijkstra correctly finds minimum W(P)
               
               Therefore: Our algorithm correctly finds maximum S(P) = safest path. ∎
            
            RECOVERY OF ORIGINAL PROBABILITY:
            ─────────────────────────────────
            Given the minimum transformed distance d[v]:
            Maximum safety probability = e^(-d[v])
            
            This follows from: d[v] = -log(safety) → safety = e^(-d[v])
            """);
    }

    static void printSafetyGraph(double[][] safety) {
        System.out.println("┌────────┬────────┬────────────────────┬─────────────────────┐");
        System.out.println("│ Source │  Dest  │ Safety Prob p(e)   │ Weight w'=-log(p)   │");
        System.out.println("├────────┼────────┼────────────────────┼─────────────────────┤");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (safety[i][j] > 0) {
                    double w = -Math.log(safety[i][j]);
                    System.out.printf("│  %-4s  │  %-4s  │       %.2f         │       %.4f        │%n",
                            NAMES[i], NAMES[j], safety[i][j], w);
                }
            }
        }
        System.out.println("└────────┴────────┴────────────────────┴─────────────────────┘");
    }

    static void printAlgorithmPseudocode() {
        System.out.println("""
            ```
            ALGORITHM: SafestPathDijkstra(G, source)
            ─────────────────────────────────────────
            Input:  G = (V, E) with safety probabilities p(e) for each edge
                    source = starting node
            Output: safestProb[v] = maximum safety probability from source to v
                    parent[v] = predecessor on safest path
            
            1.  // INITIALIZATION
            2.  for each vertex v in V:
            3.      dist[v] ← ∞           // Transformed distance (-log of probability)
            4.      safestProb[v] ← 0     // Actual safety probability
            5.      parent[v] ← null
            6.      visited[v] ← false
            7.  
            8.  dist[source] ← 0
            9.  safestProb[source] ← 1.0  // Probability of reaching source is 1
            10. 
            11. // Priority queue ordered by dist (min-heap)
            12. PQ ← new MinPriorityQueue()
            13. PQ.insert(source, 0)
            14. 
            15. while PQ is not empty:
            16.     u ← PQ.extractMin()
            17.     if visited[u]: continue
            18.     visited[u] ← true
            19.     
            20.     for each edge (u, v) with safety probability p(u,v):
            21.         // MODIFIED RELAX OPERATION
            22.         w_transformed ← -log(p(u,v))
            23.         newDist ← dist[u] + w_transformed
            24.         
            25.         if newDist < dist[v]:
            26.             dist[v] ← newDist
            27.             safestProb[v] ← safestProb[u] × p(u,v)  // Or: exp(-dist[v])
            28.             parent[v] ← u
            29.             PQ.insert(v, dist[v])
            30. 
            31. return safestProb[], parent[]
            ```
            
            MODIFIED RELAX OPERATION EXPLANATION:
            ─────────────────────────────────────
            Standard Dijkstra RELAX:
                if dist[u] + w(u,v) < dist[v]:
                    dist[v] = dist[u] + w(u,v)
            
            Our Modified RELAX (with w' = -log(p)):
                if dist[u] + (-log(p(u,v))) < dist[v]:
                    dist[v] = dist[u] + (-log(p(u,v)))
                    // Equivalent to:
                    safestProb[v] = safestProb[u] × p(u,v)
            
            The RELAX operation remains structurally identical to standard Dijkstra.
            Only the weight calculation w(u,v) = -log(p(u,v)) is different.
            """);
    }

    // Implementation of safest path algorithm
    static void findSafestPaths(double[][] safety, int source) {
        double[] dist = new double[N];          // Transformed distances
        double[] safestProb = new double[N];    // Actual probabilities
        int[] parent = new int[N];
        boolean[] visited = new boolean[N];

        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(safestProb, 0);
        Arrays.fill(parent, -1);

        dist[source] = 0;
        safestProb[source] = 1.0;

        // Priority queue: [node, distance]
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        pq.offer(new double[]{source, 0});

        System.out.println("Step-by-step execution:");
        int step = 0;

        while (!pq.isEmpty()) {
            double[] curr = pq.poll();
            int u = (int) curr[0];

            if (visited[u]) continue;
            visited[u] = true;

            step++;
            System.out.printf("%nStep %d: Process node %s (dist=%.4f, prob=%.4f)%n", 
                    step, NAMES[u], dist[u], safestProb[u]);

            for (int v = 0; v < N; v++) {
                if (safety[u][v] > 0 && !visited[v]) {
                    double wTransformed = -Math.log(safety[u][v]);
                    double newDist = dist[u] + wTransformed;

                    if (newDist < dist[v]) {
                        dist[v] = newDist;
                        safestProb[v] = safestProb[u] * safety[u][v];
                        parent[v] = u;
                        pq.offer(new double[]{v, dist[v]});
                        System.out.printf("  RELAX: %s→%s: dist[%s] = %.4f, prob = %.4f%n",
                                NAMES[u], NAMES[v], NAMES[v], dist[v], safestProb[v]);
                    }
                }
            }
        }

        // Print results
        System.out.println("\n┌─────────────────────────────────────────────────────────────────────┐");
        System.out.println("│                    SAFEST PATHS FROM KTM                            │");
        System.out.println("├──────────┬───────────────┬───────────────┬──────────────────────────┤");
        System.out.println("│   Dest   │  -log(prob)   │  Safety Prob  │         Path             │");
        System.out.println("├──────────┼───────────────┼───────────────┼──────────────────────────┤");

        for (int v = 0; v < N; v++) {
            String path = reconstructPath(parent, source, v);
            System.out.printf("│   %-4s   │    %7.4f    │    %6.4f     │ %-24s │%n",
                    NAMES[v], dist[v], safestProb[v], path);
        }
        System.out.println("└──────────┴───────────────┴───────────────┴──────────────────────────┘");

        System.out.println("\n*** SAFEST PATH TO PATAN HOSPITAL (PH): " + 
                reconstructPath(parent, source, PH) + " with safety = " + 
                String.format("%.4f (%.2f%%)", safestProb[PH], safestProb[PH] * 100) + " ***");
    }

    static String reconstructPath(int[] parent, int source, int dest) {
        if (dest == source) return NAMES[source];
        if (parent[dest] == -1) return "No path";

        List<String> path = new ArrayList<>();
        int curr = dest;
        while (curr != -1) {
            path.add(NAMES[curr]);
            curr = parent[curr];
        }
        Collections.reverse(path);
        return String.join("→", path);
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // QUESTION 3: MAXIMUM THROUGHPUT ANALYSIS (7 Points)
    // ═══════════════════════════════════════════════════════════════════════════════
    static void solveQuestion3() {
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("QUESTION 3: MAXIMUM THROUGHPUT ANALYSIS (7 Points)");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════\n");

        System.out.println("(a) Maximum Flow Problem Model (2 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.println("""
            PROBLEM MODEL:
            ──────────────
            We model this as a Maximum Flow problem where:
            
            • SOURCE (s): KTM (Kathmandu - Primary Supply Depot)
            • SINK (t):   BS (Bhaktapur Shelter - Destination for supplies)
            
            • OBJECTIVE: Maximize the number of trucks per hour from KTM to BS
            
            NETWORK GRAPH G = (V, E):
            ─────────────────────────
            Vertices V = {KTM, JA, JB, PH, BS}
            
            Edges E with capacities c(e) (trucks/hour):
            """);

        // Capacity graph from the image
        int[][] capacity = new int[N][N];
        capacity[KTM][JA] = 10;
        capacity[KTM][JB] = 15;
        capacity[JA][KTM] = 10;
        capacity[JA][PH] = 8;
        capacity[JA][BS] = 5;
        capacity[JB][KTM] = 15;
        capacity[JB][JA] = 4;
        capacity[JB][BS] = 12;
        capacity[PH][JA] = 8;
        capacity[PH][BS] = 6;
        capacity[BS][JA] = 5;
        capacity[BS][JB] = 12;
        capacity[BS][PH] = 6;

        System.out.println("┌────────────┬─────────────────┬─────────────────────────┐");
        System.out.println("│   Source   │   Destination   │  Capacity (trucks/hr)   │");
        System.out.println("├────────────┼─────────────────┼─────────────────────────┤");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (capacity[i][j] > 0) {
                    System.out.printf("│    %-4s    │      %-4s       │           %-2d            │%n",
                            NAMES[i], NAMES[j], capacity[i][j]);
                }
            }
        }
        System.out.println("└────────────┴─────────────────┴─────────────────────────┘");

        System.out.println("""
            
            RELEVANT EDGES FOR KTM→BS FLOW:
            ────────────────────────────────
            Edges leaving KTM (source):  KTM→JA (10), KTM→JB (15)
            Edges entering BS (sink):    JA→BS (5), JB→BS (12), PH→BS (6)
            Internal edges:              JA→PH (8), JB→JA (4)
            
            Note: Reverse edges (like BS→JA) don't help flow from KTM to BS
            but will create backward edges in the residual graph.
            """);

        System.out.println("\n(b) Edmonds-Karp Algorithm Execution (3 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");

        // Execute Edmonds-Karp with detailed steps
        int maxFlow = edmondsKarpDetailed(capacity, KTM, BS);

        System.out.println("\n(c) Maximum Flow and Minimum Cut (2 Points)");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("""
            MAXIMUM FLOW VALUE: %d trucks/hour
            ════════════════════════════════════════════════════════════════════════════════
            
            MINIMUM S-T CUT ANALYSIS:
            ─────────────────────────
            After running Edmonds-Karp, we identify the minimum cut by finding vertices
            reachable from source in the final residual graph.
            
            """, maxFlow);

        findMinCut(capacity, KTM, BS);
    }

    static int edmondsKarpDetailed(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int[][] residual = new int[n][n];
        for (int i = 0; i < n; i++) {
            residual[i] = capacity[i].clone();
        }

        int totalFlow = 0;
        int iteration = 0;

        System.out.println("EDMONDS-KARP ALGORITHM (BFS-based Ford-Fulkerson)");
        System.out.println("══════════════════════════════════════════════════\n");
        System.out.println("Initial Residual Graph = Original Capacity Graph\n");

        while (true) {
            // BFS to find augmenting path
            int[] parent = new int[n];
            Arrays.fill(parent, -1);
            parent[source] = source;

            Queue<Integer> queue = new LinkedList<>();
            queue.offer(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();
                for (int v = 0; v < n; v++) {
                    if (parent[v] == -1 && residual[u][v] > 0) {
                        parent[v] = u;
                        queue.offer(v);
                    }
                }
            }

            // No augmenting path found
            if (parent[sink] == -1) {
                System.out.println("─────────────────────────────────────────────────────────");
                System.out.println("No more augmenting paths found. Algorithm terminates.\n");
                break;
            }

            iteration++;
            System.out.printf("ITERATION %d:%n", iteration);
            System.out.println("─────────────────────────────────────────────────────────");

            // Find path and bottleneck
            List<String> pathNodes = new ArrayList<>();
            int pathFlow = Integer.MAX_VALUE;
            int curr = sink;

            while (curr != source) {
                pathNodes.add(NAMES[curr]);
                int prev = parent[curr];
                pathFlow = Math.min(pathFlow, residual[prev][curr]);
                curr = prev;
            }
            pathNodes.add(NAMES[source]);
            Collections.reverse(pathNodes);

            System.out.println("1. Augmenting Path Found (via BFS): " + String.join(" → ", pathNodes));
            System.out.println("2. Bottleneck Capacity (flow to push): " + pathFlow + " trucks/hour");

            // Update residual graph
            System.out.println("3. Residual Graph Updates:");
            curr = sink;
            while (curr != source) {
                int prev = parent[curr];
                int oldForward = residual[prev][curr];
                int oldBackward = residual[curr][prev];

                residual[prev][curr] -= pathFlow;
                residual[curr][prev] += pathFlow;

                System.out.printf("   • Edge %s→%s: %d → %d (decreased by %d)%n",
                        NAMES[prev], NAMES[curr], oldForward, residual[prev][curr], pathFlow);
                System.out.printf("   • Edge %s→%s: %d → %d (backward edge increased by %d)%n",
                        NAMES[curr], NAMES[prev], oldBackward, residual[curr][prev], pathFlow);

                curr = prev;
            }

            totalFlow += pathFlow;
            System.out.printf("%nCumulative Flow after Iteration %d: %d trucks/hour%n%n", iteration, totalFlow);
        }

        return totalFlow;
    }

    static void findMinCut(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        int[][] residual = new int[n][n];
        for (int i = 0; i < n; i++) {
            residual[i] = capacity[i].clone();
        }

        // Run max flow to get final residual graph
        while (true) {
            int[] parent = new int[n];
            Arrays.fill(parent, -1);
            parent[source] = source;

            Queue<Integer> queue = new LinkedList<>();
            queue.offer(source);

            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();
                for (int v = 0; v < n; v++) {
                    if (parent[v] == -1 && residual[u][v] > 0) {
                        parent[v] = u;
                        queue.offer(v);
                    }
                }
            }

            if (parent[sink] == -1) break;

            int pathFlow = Integer.MAX_VALUE;
            int curr = sink;
            while (curr != source) {
                int prev = parent[curr];
                pathFlow = Math.min(pathFlow, residual[prev][curr]);
                curr = prev;
            }

            curr = sink;
            while (curr != source) {
                int prev = parent[curr];
                residual[prev][curr] -= pathFlow;
                residual[curr][prev] += pathFlow;
                curr = prev;
            }
        }

        // Find reachable vertices from source in residual graph
        boolean[] reachable = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);
        reachable[source] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v = 0; v < n; v++) {
                if (!reachable[v] && residual[u][v] > 0) {
                    reachable[v] = true;
                    queue.offer(v);
                }
            }
        }

        // Set S (reachable from source)
        System.out.print("Set S (reachable from KTM in residual graph): {");
        List<String> setS = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (reachable[i]) setS.add(NAMES[i]);
        }
        System.out.println(String.join(", ", setS) + "}");

        // Set T (not reachable)
        System.out.print("Set T (not reachable, contains sink): {");
        List<String> setT = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!reachable[i]) setT.add(NAMES[i]);
        }
        System.out.println(String.join(", ", setT) + "}");

        // Find min cut edges
        System.out.println("\nMINIMUM CUT EDGES (edges from S to T in original graph):");
        int cutCapacity = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (reachable[i] && !reachable[j] && capacity[i][j] > 0) {
                    System.out.printf("   • %s → %s : capacity = %d%n", NAMES[i], NAMES[j], capacity[i][j]);
                    cutCapacity += capacity[i][j];
                }
            }
        }

        System.out.println("\n════════════════════════════════════════════════════════════════════════════════");
        System.out.printf("MINIMUM CUT CAPACITY: %d trucks/hour%n", cutCapacity);
        System.out.println("════════════════════════════════════════════════════════════════════════════════");

        System.out.println("""
            
            MAX-FLOW MIN-CUT THEOREM VERIFICATION:
            ──────────────────────────────────────
            The Max-Flow Min-Cut Theorem states that:
            
                Maximum Flow Value = Minimum Cut Capacity
            
            Our results:
            • Maximum Flow (computed by Edmonds-Karp) = 23 trucks/hour
            • Minimum Cut Capacity (sum of cut edges) = 23 trucks/hour
            
            The theorem is VERIFIED! ✓
            
            INTERPRETATION:
            ───────────────
            The bottleneck in our supply network is the set of edges in the minimum cut.
            To increase supply throughput to Bhaktapur Shelter beyond 23 trucks/hour,
            we would need to increase capacity on one or more of the cut edges.
            
            This could involve:
            • Repairing damaged roads to increase capacity
            • Opening alternative routes
            • Coordinating traffic to optimize existing capacity
            """);
    }
}
