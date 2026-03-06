import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class Question6 extends JFrame {

    // Tourist Spot class
    static class TouristSpot {
        String name;
        double latitude;
        double longitude;
        int entryFee;
        String openTime;
        String closeTime;
        List<String> tags;

        TouristSpot(String name, double lat, double lon, int fee, String open, String close, String... tags) {
            this.name = name;
            this.latitude = lat;
            this.longitude = lon;
            this.entryFee = fee;
            this.openTime = open;
            this.closeTime = close;
            this.tags = Arrays.asList(tags);
        }

        int getOpenHour() {
            return Integer.parseInt(openTime.split(":")[0]);
        }

        int getCloseHour() {
            return Integer.parseInt(closeTime.split(":")[0]);
        }

        boolean hasTag(String tag) {
            return tags.stream().anyMatch(t -> t.equalsIgnoreCase(tag));
        }

        boolean matchesAnyTag(List<String> interests) {
            return interests.stream().anyMatch(this::hasTag);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Itinerary Result
    static class ItineraryResult {
        List<TouristSpot> spots;
        List<String> explanations;
        int totalCost;
        double totalTime;
        double totalDistance;

        ItineraryResult() {
            spots = new ArrayList<>();
            explanations = new ArrayList<>();
            totalCost = 0;
            totalTime = 0;
            totalDistance = 0;
        }
    }

    // Data
    private List<TouristSpot> allSpots;
    private ItineraryResult currentResult;
    private ItineraryResult bruteForceResult;

    // GUI Components
    private JSpinner timeSpinner;
    private JSpinner budgetSpinner;
    private JCheckBox cultureCheck, natureCheck, adventureCheck, religiousCheck, heritageCheck, relaxationCheck;
    private JTextArea resultArea;
    private JTextArea comparisonArea;
    private MapPanel mapPanel;

    public Question6() {
        super("Tourist Spot Optimizer - Nepal Itinerary Planner");
        initializeData();
        createGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    // Task 2: Load Tourist Spot Dataset
    private void initializeData() {
        allSpots = new ArrayList<>();
        allSpots.add(new TouristSpot("Pashupatinath Temple", 27.7104, 85.3488, 100, "06:00", "18:00", "culture", "religious"));
        allSpots.add(new TouristSpot("Swayambhunath Stupa", 27.7149, 85.2906, 200, "07:00", "17:00", "culture", "heritage"));
        allSpots.add(new TouristSpot("Garden of Dreams", 27.7125, 85.3170, 150, "09:00", "21:00", "nature", "relaxation"));
        allSpots.add(new TouristSpot("Chandragiri Hills", 27.6616, 85.2458, 700, "09:00", "17:00", "nature", "adventure"));
        allSpots.add(new TouristSpot("Kathmandu Durbar Square", 27.7048, 85.3076, 100, "10:00", "17:00", "culture", "heritage"));
        allSpots.add(new TouristSpot("Boudhanath Stupa", 27.7215, 85.3620, 250, "06:00", "20:00", "culture", "religious"));
        allSpots.add(new TouristSpot("Thamel District", 27.7152, 85.3123, 0, "00:00", "23:59", "shopping", "relaxation"));
        allSpots.add(new TouristSpot("Nagarkot Viewpoint", 27.7172, 85.5200, 50, "05:00", "19:00", "nature", "adventure"));
    }

    // Task 1: GUI Design
    private void createGUI() {
        setLayout(new BorderLayout(10, 10));

        // Left Panel - Input
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.WEST);

        // Center Panel - Results and Map
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        // Map Panel
        mapPanel = new MapPanel();
        mapPanel.setPreferredSize(new Dimension(500, 400));
        mapPanel.setBorder(BorderFactory.createTitledBorder("Route Map"));
        centerPanel.add(mapPanel, BorderLayout.CENTER);

        // Results Area
        resultArea = new JTextArea(12, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Suggested Itinerary"));
        centerPanel.add(resultScroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Right Panel - Comparison
        JPanel rightPanel = new JPanel(new BorderLayout());
        comparisonArea = new JTextArea(30, 30);
        comparisonArea.setEditable(false);
        comparisonArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane compScroll = new JScrollPane(comparisonArea);
        compScroll.setBorder(BorderFactory.createTitledBorder("Heuristic vs Brute-Force Comparison"));
        rightPanel.add(compScroll, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("User Preferences"));
        panel.setPreferredSize(new Dimension(250, 0));

        // Time Available
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Available Time (hours):"));
        timeSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 24, 1));
        timePanel.add(timeSpinner);
        panel.add(timePanel);

        // Budget
        JPanel budgetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        budgetPanel.add(new JLabel("Max Budget (NPR):"));
        budgetSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 10000, 100));
        budgetPanel.add(budgetSpinner);
        panel.add(budgetPanel);

        // Interest Tags
        JPanel tagsPanel = new JPanel();
        tagsPanel.setLayout(new BoxLayout(tagsPanel, BoxLayout.Y_AXIS));
        tagsPanel.setBorder(BorderFactory.createTitledBorder("Interest Tags"));

        cultureCheck = new JCheckBox("Culture", true);
        natureCheck = new JCheckBox("Nature", true);
        adventureCheck = new JCheckBox("Adventure");
        religiousCheck = new JCheckBox("Religious");
        heritageCheck = new JCheckBox("Heritage");
        relaxationCheck = new JCheckBox("Relaxation");

        tagsPanel.add(cultureCheck);
        tagsPanel.add(natureCheck);
        tagsPanel.add(adventureCheck);
        tagsPanel.add(religiousCheck);
        tagsPanel.add(heritageCheck);
        tagsPanel.add(relaxationCheck);
        panel.add(tagsPanel);

        panel.add(Box.createVerticalStrut(20));

        // Buttons
        JButton optimizeBtn = new JButton("Generate Itinerary (Greedy)");
        optimizeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        optimizeBtn.addActionListener(e -> runOptimization());
        panel.add(optimizeBtn);

        panel.add(Box.createVerticalStrut(10));

        JButton compareBtn = new JButton("Compare with Brute-Force");
        compareBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        compareBtn.addActionListener(e -> runComparison());
        panel.add(compareBtn);

        panel.add(Box.createVerticalStrut(10));

        JButton clearBtn = new JButton("Clear Results");
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.addActionListener(e -> clearResults());
        panel.add(clearBtn);

        // Dataset info
        panel.add(Box.createVerticalStrut(20));
        JTextArea dataInfo = new JTextArea();
        dataInfo.setEditable(false);
        dataInfo.setLineWrap(true);
        dataInfo.setWrapStyleWord(true);
        dataInfo.setText("Dataset: " + allSpots.size() + " tourist spots in Kathmandu Valley, Nepal");
        dataInfo.setBackground(panel.getBackground());
        panel.add(dataInfo);

        return panel;
    }

    private List<String> getSelectedTags() {
        List<String> tags = new ArrayList<>();
        if (cultureCheck.isSelected()) tags.add("culture");
        if (natureCheck.isSelected()) tags.add("nature");
        if (adventureCheck.isSelected()) tags.add("adventure");
        if (religiousCheck.isSelected()) tags.add("religious");
        if (heritageCheck.isSelected()) tags.add("heritage");
        if (relaxationCheck.isSelected()) tags.add("relaxation");
        return tags;
    }

    // Task 3: Greedy Heuristic Optimization
    private void runOptimization() {
        int maxTime = (int) timeSpinner.getValue();
        int maxBudget = (int) budgetSpinner.getValue();
        List<String> interests = getSelectedTags();

        if (interests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one interest tag!");
            return;
        }

        currentResult = greedyOptimization(allSpots, maxTime, maxBudget, interests, 9); // Start at 9 AM
        displayResult(currentResult);
        mapPanel.setRoute(currentResult.spots);
    }

    private ItineraryResult greedyOptimization(List<TouristSpot> spots, int maxHours, int maxBudget, 
                                                List<String> interests, int startHour) {
        ItineraryResult result = new ItineraryResult();
        Set<TouristSpot> visited = new HashSet<>();
        TouristSpot current = null;
        int currentHour = startHour;
        int remainingBudget = maxBudget;
        double remainingTime = maxHours;

        while (remainingTime > 0.5 && remainingBudget > 0) {
            TouristSpot best = null;
            double bestScore = -1;
            String bestReason = "";

            for (TouristSpot spot : spots) {
                if (visited.contains(spot)) continue;
                if (spot.entryFee > remainingBudget) continue;

                // Check if spot is open
                if (currentHour < spot.getOpenHour() || currentHour >= spot.getCloseHour()) continue;

                // Calculate travel time
                double travelTime = (current == null) ? 0.5 : calculateTravelTime(current, spot);
                double visitTime = 1.0; // Assume 1 hour per spot
                if (travelTime + visitTime > remainingTime) continue;

                // Calculate score (heuristic)
                double score = 0;

                // Interest match bonus
                int matchCount = 0;
                for (String interest : interests) {
                    if (spot.hasTag(interest)) matchCount++;
                }
                score += matchCount * 100;

                // Cost efficiency (lower fee = higher score)
                score += (1000 - spot.entryFee) * 0.1;

                // Distance penalty
                if (current != null) {
                    score -= travelTime * 50;
                }

                // Prefer spots closing soon
                int hoursUntilClose = spot.getCloseHour() - currentHour;
                if (hoursUntilClose <= 2) score += 50;

                if (score > bestScore) {
                    bestScore = score;
                    best = spot;
                    bestReason = String.format("Interest match: %d tags, Fee: Rs.%d, Travel: %.1fh", 
                                               matchCount, spot.entryFee, travelTime);
                }
            }

            if (best == null) break;

            // Add to itinerary
            double travelTime = (current == null) ? 0.5 : calculateTravelTime(current, best);
            visited.add(best);
            result.spots.add(best);
            result.explanations.add(bestReason);
            result.totalCost += best.entryFee;
            result.totalTime += travelTime + 1.0;
            if (current != null) {
                result.totalDistance += calculateDistance(current, best);
            }

            remainingBudget -= best.entryFee;
            remainingTime -= (travelTime + 1.0);
            currentHour += (int)(travelTime + 1.0);
            current = best;
        }

        return result;
    }

    // Task 4: Display Results
    private void displayResult(ItineraryResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════════\n");
        sb.append("           SUGGESTED ITINERARY (GREEDY ALGORITHM)\n");
        sb.append("═══════════════════════════════════════════════════════\n\n");

        if (result.spots.isEmpty()) {
            sb.append("No valid itinerary found with given constraints.\n");
            sb.append("Try increasing budget/time or selecting more interests.\n");
        } else {
            int hour = 9;
            for (int i = 0; i < result.spots.size(); i++) {
                TouristSpot spot = result.spots.get(i);
                sb.append(String.format("Stop %d: %s\n", i + 1, spot.name));
                sb.append(String.format("   Time: %02d:00 - %02d:00\n", hour, hour + 1));
                sb.append(String.format("   Cost: Rs. %d | Tags: %s\n", spot.entryFee, spot.tags));
                sb.append(String.format("   Decision: %s\n", result.explanations.get(i)));
                sb.append("\n");
                hour += 2; // 1hr visit + ~1hr travel
            }

            sb.append("───────────────────────────────────────────────────────\n");
            sb.append(String.format("TOTAL SPOTS: %d\n", result.spots.size()));
            sb.append(String.format("TOTAL COST: Rs. %d\n", result.totalCost));
            sb.append(String.format("TOTAL TIME: %.1f hours\n", result.totalTime));
            sb.append(String.format("TOTAL DISTANCE: %.2f km (approx)\n", result.totalDistance));
        }

        resultArea.setText(sb.toString());
    }

    // Task 5: Brute-Force Comparison
    private void runComparison() {
        if (currentResult == null || currentResult.spots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please generate an itinerary first!");
            return;
        }

        int maxTime = (int) timeSpinner.getValue();
        int maxBudget = (int) budgetSpinner.getValue();
        List<String> interests = getSelectedTags();

        // Use smaller dataset for brute-force (first 5 spots)
        List<TouristSpot> smallSet = allSpots.subList(0, Math.min(5, allSpots.size()));

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("    BRUTE-FORCE vs GREEDY COMPARISON\n");
        sb.append("═══════════════════════════════════════════════\n\n");
        sb.append("Dataset: " + smallSet.size() + " spots (limited for brute-force)\n\n");

        // Run greedy on small set
        long greedyStart = System.nanoTime();
        ItineraryResult greedyResult = greedyOptimization(smallSet, maxTime, maxBudget, interests, 9);
        long greedyTime = System.nanoTime() - greedyStart;

        // Run brute-force
        long bfStart = System.nanoTime();
        bruteForceResult = bruteForceOptimization(smallSet, maxTime, maxBudget, interests);
        long bfTime = System.nanoTime() - bfStart;

        // Display Greedy Results
        sb.append("─── GREEDY ALGORITHM ───\n");
        sb.append(String.format("Spots visited: %d\n", greedyResult.spots.size()));
        for (TouristSpot s : greedyResult.spots) {
            sb.append("  • " + s.name + "\n");
        }
        sb.append(String.format("Total Cost: Rs. %d\n", greedyResult.totalCost));
        sb.append(String.format("Total Time: %.1f hrs\n", greedyResult.totalTime));
        sb.append(String.format("Execution: %.3f ms\n\n", greedyTime / 1_000_000.0));

        // Display Brute-Force Results
        sb.append("─── BRUTE-FORCE (OPTIMAL) ───\n");
        sb.append(String.format("Spots visited: %d\n", bruteForceResult.spots.size()));
        for (TouristSpot s : bruteForceResult.spots) {
            sb.append("  • " + s.name + "\n");
        }
        sb.append(String.format("Total Cost: Rs. %d\n", bruteForceResult.totalCost));
        sb.append(String.format("Total Time: %.1f hrs\n", bruteForceResult.totalTime));
        sb.append(String.format("Execution: %.3f ms\n\n", bfTime / 1_000_000.0));

        // Analysis
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("          ANALYSIS\n");
        sb.append("═══════════════════════════════════════════════\n\n");

        int spotDiff = bruteForceResult.spots.size() - greedyResult.spots.size();
        int costDiff = greedyResult.totalCost - bruteForceResult.totalCost;
        double speedup = (double) bfTime / greedyTime;

        sb.append(String.format("Spot difference: %d\n", spotDiff));
        sb.append(String.format("Cost difference: Rs. %d\n", costDiff));
        sb.append(String.format("Speed improvement: %.1fx faster\n\n", speedup));

        sb.append("─── TRADE-OFF DISCUSSION ───\n");
        sb.append("• ACCURACY:\n");
        if (greedyResult.spots.size() == bruteForceResult.spots.size()) {
            sb.append("  Greedy found OPTIMAL solution!\n");
        } else {
            double accuracy = (greedyResult.spots.size() * 100.0) / bruteForceResult.spots.size();
            sb.append(String.format("  Greedy achieved %.1f%% of optimal\n", accuracy));
        }

        sb.append("\n• PERFORMANCE:\n");
        sb.append(String.format("  Brute-force: O(n!) = O(%d!) permutations\n", smallSet.size()));
        sb.append("  Greedy: O(n²) - much faster\n");
        sb.append(String.format("  Greedy was %.1fx faster\n", speedup));

        sb.append("\n• SCALABILITY:\n");
        sb.append("  With " + allSpots.size() + " spots:\n");
        sb.append("  - Brute-force: " + factorial(allSpots.size()) + " permutations (infeasible)\n");
        sb.append("  - Greedy: ~" + (allSpots.size() * allSpots.size()) + " operations (fast)\n");

        sb.append("\n• CONCLUSION:\n");
        sb.append("  Greedy provides good solutions in\n");
        sb.append("  polynomial time. For real-world\n");
        sb.append("  applications with many spots,\n");
        sb.append("  greedy is the practical choice.\n");

        comparisonArea.setText(sb.toString());
    }

    private ItineraryResult bruteForceOptimization(List<TouristSpot> spots, int maxHours, 
                                                    int maxBudget, List<String> interests) {
        ItineraryResult best = new ItineraryResult();
        int bestScore = 0;

        // Generate all permutations
        List<List<TouristSpot>> permutations = new ArrayList<>();
        generatePermutations(spots, 0, permutations);

        for (List<TouristSpot> perm : permutations) {
            // Try all subset sizes
            for (int size = 1; size <= perm.size(); size++) {
                ItineraryResult result = evaluatePath(perm.subList(0, size), maxHours, maxBudget, interests);
                int score = calculatePathScore(result, interests);
                if (score > bestScore) {
                    bestScore = score;
                    best = result;
                }
            }
        }

        return best;
    }

    private void generatePermutations(List<TouristSpot> spots, int start, List<List<TouristSpot>> result) {
        if (start == spots.size()) {
            result.add(new ArrayList<>(spots));
            return;
        }
        for (int i = start; i < spots.size(); i++) {
            Collections.swap(spots, start, i);
            generatePermutations(spots, start + 1, result);
            Collections.swap(spots, start, i);
        }
    }

    private ItineraryResult evaluatePath(List<TouristSpot> path, int maxHours, int maxBudget, List<String> interests) {
        ItineraryResult result = new ItineraryResult();
        int currentHour = 9;
        int budget = maxBudget;
        double time = maxHours;
        TouristSpot prev = null;

        for (TouristSpot spot : path) {
            double travelTime = (prev == null) ? 0.5 : calculateTravelTime(prev, spot);
            double visitTime = 1.0;

            if (travelTime + visitTime > time) break;
            if (spot.entryFee > budget) break;
            if (currentHour < spot.getOpenHour() || currentHour >= spot.getCloseHour()) continue;
            if (!spot.matchesAnyTag(interests)) continue;

            result.spots.add(spot);
            result.totalCost += spot.entryFee;
            result.totalTime += travelTime + visitTime;
            if (prev != null) {
                result.totalDistance += calculateDistance(prev, spot);
            }

            budget -= spot.entryFee;
            time -= (travelTime + visitTime);
            currentHour += (int)(travelTime + visitTime);
            prev = spot;
        }

        return result;
    }

    private int calculatePathScore(ItineraryResult result, List<String> interests) {
        int score = result.spots.size() * 1000;
        for (TouristSpot spot : result.spots) {
            for (String interest : interests) {
                if (spot.hasTag(interest)) score += 100;
            }
        }
        score -= result.totalCost / 10;
        return score;
    }

    private double calculateDistance(TouristSpot a, TouristSpot b) {
        double latDiff = a.latitude - b.latitude;
        double lonDiff = a.longitude - b.longitude;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111; // Approx km
    }

    private double calculateTravelTime(TouristSpot a, TouristSpot b) {
        double distance = calculateDistance(a, b);
        return distance / 20.0; // Assume 20 km/h average speed in Kathmandu
    }

    private long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    private void clearResults() {
        resultArea.setText("");
        comparisonArea.setText("");
        mapPanel.setRoute(new ArrayList<>());
        currentResult = null;
        bruteForceResult = null;
    }

    // Map Panel for visualization
    class MapPanel extends JPanel {
        private List<TouristSpot> route = new ArrayList<>();
        private final Color[] COLORS = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN};

        void setRoute(List<TouristSpot> route) {
            this.route = route;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 40;
            int h = getHeight() - 60;

            // Find bounds
            double minLat = 27.65, maxLat = 27.73;
            double minLon = 85.24, maxLon = 85.52;

            // Draw all spots
            g2.setColor(Color.LIGHT_GRAY);
            for (TouristSpot spot : allSpots) {
                int x = (int) ((spot.longitude - minLon) / (maxLon - minLon) * w) + 20;
                int y = (int) ((maxLat - spot.latitude) / (maxLat - minLat) * h) + 30;
                g2.fillOval(x - 5, y - 5, 10, 10);
            }

            if (route.isEmpty()) {
                g2.setColor(Color.GRAY);
                g2.drawString("Generate itinerary to see route", w/2 - 80, h/2);
                return;
            }

            // Draw route lines
            g2.setColor(new Color(0, 100, 200));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < route.size() - 1; i++) {
                TouristSpot a = route.get(i);
                TouristSpot b = route.get(i + 1);
                int x1 = (int) ((a.longitude - minLon) / (maxLon - minLon) * w) + 20;
                int y1 = (int) ((maxLat - a.latitude) / (maxLat - minLat) * h) + 30;
                int x2 = (int) ((b.longitude - minLon) / (maxLon - minLon) * w) + 20;
                int y2 = (int) ((maxLat - b.latitude) / (maxLat - minLat) * h) + 30;
                g2.drawLine(x1, y1, x2, y2);

                // Arrow
                drawArrow(g2, x1, y1, x2, y2);
            }

            // Draw route spots with numbers
            for (int i = 0; i < route.size(); i++) {
                TouristSpot spot = route.get(i);
                int x = (int) ((spot.longitude - minLon) / (maxLon - minLon) * w) + 20;
                int y = (int) ((maxLat - spot.latitude) / (maxLat - minLat) * h) + 30;

                g2.setColor(COLORS[i % COLORS.length]);
                g2.fillOval(x - 12, y - 12, 24, 24);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.drawString(String.valueOf(i + 1), x - 4, y + 5);

                // Label
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.PLAIN, 10));
                String name = spot.name.length() > 15 ? spot.name.substring(0, 15) + "..." : spot.name;
                g2.drawString(name, x + 15, y + 5);
            }

            // Legend
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("Route: " + route.size() + " stops", 10, 20);
        }

        private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int midX = (x1 + x2) / 2;
            int midY = (y1 + y2) / 2;
            int arrowSize = 8;

            int[] xPoints = {midX, midX - arrowSize, midX - arrowSize};
            int[] yPoints = {0, -arrowSize/2, arrowSize/2};

            AffineTransform old = g2.getTransform();
            g2.translate(midX, midY);
            g2.rotate(angle);
            g2.translate(-midX, -midY);
            g2.fillPolygon(xPoints, yPoints, 3);
            g2.setTransform(old);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new Question6().setVisible(true);
        });
    }
}
