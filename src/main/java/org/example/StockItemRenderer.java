package org.example;

import javax.swing.*;
import java.awt.*;

public class StockItemRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof StockItem) {
            StockItem item = (StockItem) value;
            label.setText(item.getName()); //ONLY SHOW NAME
        }
        return label;
    }
}

