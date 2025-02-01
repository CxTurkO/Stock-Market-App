package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class StockListTableGUI extends JFrame {

    private static final String API_KEY = "YOUR_API_KEY"; //Enter Your API KEY HERE CAN BE FOUND ALPHA VINTAGE API

    private JList<StockItem> stockList;
    private JTable stockDataTable;
    private DefaultTableModel tableModel;

    public StockListTableGUI() {
        super("Stocks");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        DefaultListModel<StockItem> listModel = new DefaultListModel<>();
        List<StockItem> stocks = DBHelper.getStockItems();
        for (StockItem item : stocks) {
            listModel.addElement(item);
        }
        stockList = new JList<>(listModel);
        stockList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus)
            {

                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StockItem) {
                    StockItem item = (StockItem) value;
                    label.setText(item.getName());
                }
                return label;
            }
        });
        JScrollPane listScrollPane = new JScrollPane(stockList);
        listScrollPane.setPreferredSize(new Dimension(200, 600));

        String[] columns = {"Date", "Open Price", "High", "Low", "Close Price", "Volume"};
        tableModel = new DefaultTableModel(columns, 0);
        stockDataTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(stockDataTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 600));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, tableScrollPane);
        add(splitPane, BorderLayout.CENTER);

        stockList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    StockItem selectedItem = stockList.getSelectedValue();
                    if (selectedItem != null) {
                        loadTableForSymbol(selectedItem.getSymbol());
                    }
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void loadTableForSymbol(String symbol) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
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
                    throw new Exception("No Data Check API");
                }

                tableModel.setRowCount(0);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                int count = 0;
                Iterator<Map.Entry<String, JsonNode>> fields = timeSeriesNode.fields();
                while (fields.hasNext() && count < 30) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String dateStr = entry.getKey();
                    Date date = dateFormat.parse(dateStr);

                    JsonNode dailyData = entry.getValue();

                    String open = dailyData.get("1. open").asText();
                    String high = dailyData.get("2. high").asText();
                    String low = dailyData.get("3. low").asText();
                    String close = dailyData.get("4. close").asText();
                    String volume = dailyData.get("5. volume").asText();


                    tableModel.addRow(new Object[]{dateStr, open, high, low, close, volume});
                    count++;
                }
                return null;
            }

        };
        worker.execute();
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            StockListTableGUI gui = new StockListTableGUI();
            gui.setVisible(true);
        });
    }
}
