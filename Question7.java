import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Question7 extends JFrame {

    // Cities in Nepal
    private static final String[] CITIES = {"Kathmandu", "Pokhara", "Biratnagar", "Nepalgunj", "Dhangadhi"};
    
    // API Configuration - Replace with your API key from OpenWeatherMap
    private static final String API_KEY = "YOUR_API_KEY_HERE"; // Get free key from openweathermap.org
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather?q=%s,NP&appid=%s&units=metric";
    
    // Use simulation mode if no API key
    private boolean simulationMode = API_KEY.equals("YOUR_API_KEY_HERE");

    // Weather data class
    static class WeatherData {
        String city;
        double temperature;
        double humidity;
        double pressure;
        String description;
        long fetchTime;
        String status;

        WeatherData(String city) {
            this.city = city;
            this.status = "Pending";
        }
    }

    // Thread-safe data storage
    private final Map<String, WeatherData> weatherDataMap = new ConcurrentHashMap<>();
    private final ReentrantLock guiLock = new ReentrantLock();

    // Timing results
    private long sequentialTime = 0;
    private long parallelTime = 0;

    // GUI Components
    private JTable weatherTable;
    private DefaultTableModel tableModel;
    private JButton fetchButton;
    private JButton sequentialButton;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private LatencyChartPanel chartPanel;
    private JTextArea logArea;

    public Question7() {
        super("Multi-threaded Weather Data Collector - Nepal");
        initializeData();
        createGUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initializeData() {
        for (String city : CITIES) {
            weatherDataMap.put(city, new WeatherData(city));
        }
    }

    // Task 1: Design GUI Application
    private void createGUI() {
        setLayout(new BorderLayout(10, 10));

        // Top Panel - Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        fetchButton = new JButton("Fetch Weather (Parallel)");
        fetchButton.setFont(new Font("Arial", Font.BOLD, 14));
        fetchButton.addActionListener(e -> fetchWeatherParallel());
        controlPanel.add(fetchButton);

        sequentialButton = new JButton("Fetch Weather (Sequential)");
        sequentialButton.addActionListener(e -> fetchWeatherSequential());
        controlPanel.add(sequentialButton);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearData());
        controlPanel.add(clearButton);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(150, 25));
        controlPanel.add(progressBar);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        controlPanel.add(statusLabel);

        add(controlPanel, BorderLayout.NORTH);

        // Center Panel - Table and Chart
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Weather Data Table
        String[] columns = {"City", "Temperature (°C)", "Humidity (%)", "Pressure (hPa)", "Description", "Fetch Time (ms)", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        weatherTable = new JTable(tableModel);
        weatherTable.setFont(new Font("Arial", Font.PLAIN, 12));
        weatherTable.setRowHeight(25);
        weatherTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Custom renderer for status column
        weatherTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if ("Success".equals(status)) {
                    c.setForeground(new Color(0, 128, 0));
                } else if ("Error".equals(status)) {
                    c.setForeground(Color.RED);
                } else if ("Fetching...".equals(status)) {
                    c.setForeground(Color.BLUE);
                } else {
                    c.setForeground(Color.GRAY);
                }
                return c;
            }
        });

        // Initialize table rows
        for (String city : CITIES) {
            tableModel.addRow(new Object[]{city, "-", "-", "-", "-", "-", "Pending"});
        }

        JScrollPane tableScroll = new JScrollPane(weatherTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Weather Data"));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        // Latency Chart Panel
        chartPanel = new LatencyChartPanel();
        chartPanel.setPreferredSize(new Dimension(300, 200));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Latency Comparison (ms)"));
        centerPanel.add(chartPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel - Log
        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Execution Log"));
        add(logScroll, BorderLayout.SOUTH);

        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Show simulation mode warning
        if (simulationMode) {
            log("⚠ SIMULATION MODE: No API key configured. Using simulated data.");
            log("  To use real API, get a free key from openweathermap.org");
            log("  and replace 'YOUR_API_KEY_HERE' in the code.\n");
        }
    }

    // Task 2 & 3: Fetch weather data using multithreading (parallel)
    private void fetchWeatherParallel() {
        fetchButton.setEnabled(false);
        sequentialButton.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Fetching (Parallel)...");
        log("\n═══ PARALLEL FETCH STARTED ═══");
        log("Using " + CITIES.length + " threads concurrently...\n");

        // Reset data
        for (int i = 0; i < CITIES.length; i++) {
            updateTableRow(i, "-", "-", "-", "-", "-", "Fetching...");
        }

        // Use SwingWorker to keep GUI responsive
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long startTime = System.currentTimeMillis();

                // Task 3: Create thread pool with 5 threads
                ExecutorService executor = Executors.newFixedThreadPool(CITIES.length);
                CountDownLatch latch = new CountDownLatch(CITIES.length);

                for (int i = 0; i < CITIES.length; i++) {
                    final int index = i;
                    final String city = CITIES[i];

                    executor.submit(() -> {
                        try {
                            log(String.format("Thread-%d: Fetching %s...", index + 1, city));
                            long fetchStart = System.currentTimeMillis();

                            WeatherData data = fetchWeatherFromAPI(city);
                            data.fetchTime = System.currentTimeMillis() - fetchStart;

                            // Task 4: Thread-safe GUI update using SwingUtilities
                            SwingUtilities.invokeLater(() -> {
                                guiLock.lock();
                                try {
                                    updateTableRow(index, 
                                        String.format("%.1f", data.temperature),
                                        String.format("%.0f", data.humidity),
                                        String.format("%.0f", data.pressure),
                                        data.description,
                                        String.valueOf(data.fetchTime),
                                        data.status);
                                    weatherDataMap.put(city, data);
                                } finally {
                                    guiLock.unlock();
                                }
                            });

                            log(String.format("Thread-%d: %s completed in %dms", index + 1, city, data.fetchTime));
                        } catch (Exception e) {
                            log(String.format("Thread-%d: Error fetching %s - %s", index + 1, city, e.getMessage()));
                        } finally {
                            latch.countDown();
                            int progress = (int) ((CITIES.length - latch.getCount()) * 100 / CITIES.length);
                            publish(progress);
                        }
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                executor.shutdown();
                parallelTime = System.currentTimeMillis() - startTime;
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                statusLabel.setText("Parallel fetch complete");
                fetchButton.setEnabled(true);
                sequentialButton.setEnabled(true);
                log(String.format("\n✓ PARALLEL FETCH COMPLETED in %d ms\n", parallelTime));
                chartPanel.updateData(sequentialTime, parallelTime);
            }
        };

        worker.execute();
    }

    // Fetch weather data sequentially (for comparison)
    private void fetchWeatherSequential() {
        fetchButton.setEnabled(false);
        sequentialButton.setEnabled(false);
        progressBar.setValue(0);
        statusLabel.setText("Fetching (Sequential)...");
        log("\n═══ SEQUENTIAL FETCH STARTED ═══");
        log("Fetching one city at a time...\n");

        // Reset data
        for (int i = 0; i < CITIES.length; i++) {
            updateTableRow(i, "-", "-", "-", "-", "-", "Pending");
        }

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < CITIES.length; i++) {
                    final int index = i;
                    final String city = CITIES[i];

                    // Update status
                    SwingUtilities.invokeLater(() -> 
                        updateTableRow(index, "-", "-", "-", "-", "-", "Fetching..."));

                    log(String.format("Fetching %s...", city));
                    long fetchStart = System.currentTimeMillis();

                    try {
                        WeatherData data = fetchWeatherFromAPI(city);
                        data.fetchTime = System.currentTimeMillis() - fetchStart;

                        final WeatherData finalData = data;
                        SwingUtilities.invokeLater(() -> {
                            updateTableRow(index,
                                String.format("%.1f", finalData.temperature),
                                String.format("%.0f", finalData.humidity),
                                String.format("%.0f", finalData.pressure),
                                finalData.description,
                                String.valueOf(finalData.fetchTime),
                                finalData.status);
                            weatherDataMap.put(city, finalData);
                        });

                        log(String.format("  %s completed in %dms", city, data.fetchTime));
                    } catch (Exception e) {
                        log(String.format("  Error: %s", e.getMessage()));
                    }

                    publish((i + 1) * 100 / CITIES.length);
                }

                sequentialTime = System.currentTimeMillis() - startTime;
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progressBar.setValue(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                progressBar.setValue(100);
                statusLabel.setText("Sequential fetch complete");
                fetchButton.setEnabled(true);
                sequentialButton.setEnabled(true);
                log(String.format("\n✓ SEQUENTIAL FETCH COMPLETED in %d ms\n", sequentialTime));
                chartPanel.updateData(sequentialTime, parallelTime);
            }
        };

        worker.execute();
    }

    // Task 2: Fetch weather from API
    private WeatherData fetchWeatherFromAPI(String city) {
        WeatherData data = new WeatherData(city);

        if (simulationMode) {
            // Simulate API call with random delay
            try {
                Thread.sleep(500 + new Random().nextInt(1000)); // 500-1500ms delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Generate realistic simulated data
            Random rand = new Random();
            data.temperature = 15 + rand.nextDouble() * 20; // 15-35°C
            data.humidity = 40 + rand.nextDouble() * 50;    // 40-90%
            data.pressure = 1000 + rand.nextDouble() * 30;  // 1000-1030 hPa
            String[] descriptions = {"Clear sky", "Few clouds", "Scattered clouds", "Light rain", "Sunny"};
            data.description = descriptions[rand.nextInt(descriptions.length)];
            data.status = "Success";
            return data;
        }

        // Real API call
        try {
            String urlStr = String.format(API_URL, URLEncoder.encode(city, "UTF-8"), API_KEY);
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON manually (simple parsing)
                String json = response.toString();
                data.temperature = parseJsonDouble(json, "\"temp\":");
                data.humidity = parseJsonDouble(json, "\"humidity\":");
                data.pressure = parseJsonDouble(json, "\"pressure\":");
                data.description = parseJsonString(json, "\"description\":\"");
                data.status = "Success";
            } else {
                data.status = "Error";
                data.description = "HTTP " + responseCode;
            }
        } catch (Exception e) {
            data.status = "Error";
            data.description = e.getMessage();
        }

        return data;
    }

    private double parseJsonDouble(String json, String key) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            return Double.parseDouble(json.substring(start, end));
        } catch (Exception e) {
            return 0;
        }
    }

    private String parseJsonString(String json, String key) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "N/A";
        }
    }

    // Task 4: Thread-safe GUI update
    private void updateTableRow(int row, String temp, String humidity, String pressure, 
                                 String desc, String time, String status) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setValueAt(temp, row, 1);
            tableModel.setValueAt(humidity, row, 2);
            tableModel.setValueAt(pressure, row, 3);
            tableModel.setValueAt(desc, row, 4);
            tableModel.setValueAt(time, row, 5);
            tableModel.setValueAt(status, row, 6);
        });
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void clearData() {
        for (int i = 0; i < CITIES.length; i++) {
            updateTableRow(i, "-", "-", "-", "-", "-", "Pending");
        }
        logArea.setText("");
        sequentialTime = 0;
        parallelTime = 0;
        chartPanel.updateData(0, 0);
        statusLabel.setText("Ready");
        progressBar.setValue(0);
    }

    // Task 5: Latency Chart Panel
    class LatencyChartPanel extends JPanel {
        private long seqTime = 0;
        private long parTime = 0;

        void updateData(long sequential, long parallel) {
            this.seqTime = sequential;
            this.parTime = parallel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int margin = 50;
            int barWidth = 60;
            int chartHeight = h - 2 * margin;

            // Background
            g2.setColor(Color.WHITE);
            g2.fillRect(margin, margin, w - 2 * margin, chartHeight);
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawRect(margin, margin, w - 2 * margin, chartHeight);

            if (seqTime == 0 && parTime == 0) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Arial", Font.ITALIC, 12));
                g2.drawString("Run both fetch methods", margin + 10, h / 2 - 10);
                g2.drawString("to see comparison", margin + 10, h / 2 + 10);
                return;
            }

            // Calculate scale
            long maxTime = Math.max(seqTime, parTime);
            if (maxTime == 0) maxTime = 1;

            // Draw bars
            int x1 = margin + 30;
            int x2 = margin + 120;

            // Sequential bar
            int seqHeight = (int) ((seqTime * chartHeight) / maxTime);
            g2.setColor(new Color(220, 80, 80));
            g2.fillRect(x1, margin + chartHeight - seqHeight, barWidth, seqHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(x1, margin + chartHeight - seqHeight, barWidth, seqHeight);

            // Parallel bar
            int parHeight = (int) ((parTime * chartHeight) / maxTime);
            g2.setColor(new Color(80, 180, 80));
            g2.fillRect(x2, margin + chartHeight - parHeight, barWidth, parHeight);
            g2.setColor(Color.BLACK);
            g2.drawRect(x2, margin + chartHeight - parHeight, barWidth, parHeight);

            // Labels
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(Color.BLACK);
            g2.drawString("Sequential", x1, h - 15);
            g2.drawString("Parallel", x2 + 5, h - 15);

            // Values
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.drawString(seqTime + " ms", x1 + 5, margin + chartHeight - seqHeight - 5);
            g2.drawString(parTime + " ms", x2 + 5, margin + chartHeight - parHeight - 5);

            // Speedup
            if (seqTime > 0 && parTime > 0) {
                double speedup = (double) seqTime / parTime;
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(new Color(0, 100, 0));
                g2.drawString(String.format("Speedup: %.2fx", speedup), margin + 20, margin - 10);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {}
            new Question7().setVisible(true);
        });
    }
}
