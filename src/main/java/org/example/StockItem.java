package org.example;

public class StockItem {
    private String symbol;
    private String name;

    public StockItem(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}

