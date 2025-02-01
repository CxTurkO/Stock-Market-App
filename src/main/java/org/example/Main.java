// Muhammed Devran Yılmaz @CxTurkO


package org.example;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        DBHelper.createStocksTable();
        DBHelper.fetchAndInsertStocksFromAlphaVantage();
        SwingUtilities.invokeLater(() -> {
            StockSearchGUI gui = new StockSearchGUI();
            gui.setVisible(true);
        });
    }
}
