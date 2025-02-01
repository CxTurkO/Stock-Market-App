package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StockSearchGUI extends JFrame {

    private static final String API_KEY = "YOUR_API_KEY";

    private JComboBox<StockItem> stockComboBox;
    private RoundedButton searchButton;
    private JPanel chartContainer;
    private List<StockItem> masterStockList;
    private boolean isUpdating = false;

    public StockSearchGUI() {
        super("Stock App");

        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(new Color(220, 220, 250));
        masterStockList = DBHelper.getStockItems();

        DefaultComboBoxModel<StockItem> model = new DefaultComboBoxModel<>();
        for (StockItem item : masterStockList) {
            model.addElement(item);
        }
        stockComboBox = new JComboBox<>(model);
        stockComboBox.setEditable(true);
        stockComboBox.setPreferredSize(new Dimension(300, 25));

        JTextField editor = (JTextField) stockComboBox.getEditor().getEditorComponent();
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterComboBox();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterComboBox();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterComboBox();
            }

            private void filterComboBox() {
                if (isUpdating) return;
                isUpdating = true;
                String filterText = editor.getText().toLowerCase();
                DefaultComboBoxModel<StockItem> filteredModel = new DefaultComboBoxModel<>();
                for (StockItem item : masterStockList) {
                    if (item.getName().toLowerCase().contains(filterText) ||
                            item.getSymbol().toLowerCase().contains(filterText)) {
                        filteredModel.addElement(item);
                    }
                }
                stockComboBox.setModel(filteredModel);
                stockComboBox.getEditor().setItem(filterText);
                stockComboBox.showPopup();
                isUpdating = false;
            }
        });

        searchButton = new RoundedButton("Ara");
        searchButton.setBackground(new Color(100, 149, 237));
        searchButton.setForeground(Color.WHITE);
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = stockComboBox.getSelectedItem();
                StockItem selectedItem = null;
                if (selected instanceof StockItem) {
                    selectedItem = (StockItem) selected;
                } else if (selected instanceof String) {
                    String text = ((String) selected).trim();
                    DefaultComboBoxModel<StockItem> model = (DefaultComboBoxModel<StockItem>) stockComboBox.getModel();
                    for (int i = 0; i < model.getSize(); i++) {
                        StockItem item = model.getElementAt(i);
                        if (item.toString().equalsIgnoreCase(text)) {
                            selectedItem = item;
                            break;
                        }
                    }
                }
                if (selectedItem != null) {
                    loadChartForSymbol(selectedItem.getSymbol());
                } else {
                    JOptionPane.showMessageDialog(StockSearchGUI.this, "Please choose a valid option!!!");
                }
            }
        });

        topPanel.add(new JLabel("Choose a stock item:"));
        topPanel.add(stockComboBox);
        topPanel.add(searchButton);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setPreferredSize(new Dimension(800, 600));
        chartContainer.setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(chartContainer, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void loadChartForSymbol(String symbol) {
        SwingWorker<JFreeChart, Void> worker = new SwingWorker<JFreeChart, Void>() {
            @Override
            protected JFreeChart doInBackground() throws Exception {
                String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
                        + symbol + "&apikey=" + API_KEY + "&outputsize=compact";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonContent = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    jsonContent.append(inputLine);
                }
                in.close();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(jsonContent.toString());
                JsonNode timeSeriesNode = rootNode.get("Time Series (Daily)");
                if (timeSeriesNode == null) {
                    throw new Exception("No DATA CHECK YOUR API OR YOU EXCEED THE LIMIT OF 25 (DAILY)");
                }
                TimeSeries series = new TimeSeries(symbol + "Close Price");
                TimeSeriesCollection dataset = new TimeSeriesCollection();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                int count = 0;
                Iterator<Map.Entry<String, JsonNode>> fields = timeSeriesNode.fields();
                while (fields.hasNext() && count < 30) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String dateStr = entry.getKey();
                    Date date = dateFormat.parse(dateStr);
                    JsonNode dailyData = entry.getValue();
                    double closePrice = dailyData.get("4. close").asDouble();
                    series.add(new Day(date), closePrice);
                    count++;
                }
                dataset.addSeries(series);
                JFreeChart chart = ChartFactory.createTimeSeriesChart(
                        symbol + " Last 1 Month Close Price",
                        "Date",
                        "Price (USD)",
                        dataset,
                        true,
                        true,
                        false
                );
                return chart;
            }

            @Override
            protected void done() {
                try {
                    JFreeChart chart = get();
                    chartContainer.removeAll();
                    ChartPanel chartPanel = new ChartPanel(chart);
                    chartPanel.setPreferredSize(new Dimension(800, 600));
                    chartContainer.add(chartPanel, BorderLayout.CENTER);
                    chartContainer.revalidate();
                    chartContainer.repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(StockSearchGUI.this, "An Mistake occur in creating graph: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StockSearchGUI gui = new StockSearchGUI();
            gui.setVisible(true);
        });
    }
}
