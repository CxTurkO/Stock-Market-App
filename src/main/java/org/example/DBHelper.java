package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {

    private static final String DB_URL = "jdbc:sqlite:src/main/resources/mydatabase.db";
    private static final String API_KEY = "YOUR_API_KEY";

    public static void createStocksTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS stocks ("
                + "symbol TEXT PRIMARY KEY, "
                + "name TEXT NOT NULL, "
                + "openPrice REAL, "
                + "currentPrice REAL"
                + ");";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(createTableSQL)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void fetchAndInsertStocksFromAlphaVantage() {
        String apiUrl = "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=" + API_KEY;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            boolean isFirstLine = true;
            Connection dbConn = DriverManager.getConnection(DB_URL);
            String insertSQL = "INSERT OR IGNORE INTO stocks (symbol, name, openPrice, currentPrice) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = dbConn.prepareStatement(insertSQL);

            while ((line = in.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length >= 2) {
                    String symbol = fields[0].trim();
                    String name = fields[1].trim();

                    pstmt.setString(1, symbol);
                    pstmt.setString(2, name);
                    pstmt.setDouble(3, 0.0);
                    pstmt.setDouble(4, 0.0);
                    pstmt.executeUpdate();
                }
            }
            pstmt.close();
            dbConn.close();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<StockItem> getStockItems() {
        List<StockItem> stocks = new ArrayList<>();
        String query = "SELECT symbol, name, openPrice, currentPrice FROM stocks";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String symbol = rs.getString("symbol");
                String name = rs.getString("name");

                stocks.add(new StockItem(symbol, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }
}
