/*
   Copyright 2009 NEERC team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
// $Id$
/**
 * Date: 24.10.2005
 */
package ru.ifmo.neerc.chat.client;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Matvey Kazakov
 */
public class MyJTable<T> extends JPanel {

    private List<T> data;
    private MyJTableModel tableModel;
    private MyJTableScrollBar scrollBar;
    private JTable table;
    private TableCellRenderer cellRenderer = new DefaultTableCellRenderer();

    private boolean appendToTheEnd = false;

    public MyJTable(List<T> data) {
        super(new BorderLayout());
        this.data = data;
        tableModel = new MyJTableModel();
        table = new JTable(tableModel);
        table.setShowGrid(false);
        table.setRowHeight(24);
        scrollBar = new MyJTableScrollBar();
        add(table, BorderLayout.CENTER);
        add(scrollBar, BorderLayout.EAST);
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                tableModel.updateRowSizes();
            }
        });
    }

    public void setAppendToTheEnd(boolean appendToTheEnd) {
        this.appendToTheEnd = appendToTheEnd;
    }

    public JTable getTable() {
        return table;
    }

    protected void setRenderer(TableCellRenderer renderer) {
        cellRenderer = renderer;
        table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
    }


    protected void addElement(T element) {
        tableModel.addElement(element);
    }

    public Dimension getMinimumSize() {
        Dimension minimumSize = super.getMinimumSize();
        minimumSize.height = 10;
        return minimumSize;
    }

    class MyJTableScrollBar extends JScrollBar {

        public MyJTableScrollBar() {
            super(JScrollBar.VERTICAL, 0, 1, 0, Math.max(1, tableModel.maxStart() + 1));
            final BoundedRangeModel model = getModel();
            model.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    tableModel.setStart(model.getValue());
                }
            });
        }

        private void changeTableParams() {
            int newMax = tableModel.maxStart() + 1;
            int newValue = tableModel.start;
            if (newMax != getMaximum()) {
                setMaximum(newMax);
            }
            if (newValue != getValue()) {
                setValue(newValue);
            }
        }
    }

    class MyJTableModel extends AbstractTableModel {

        int start = 0;
        int length = 0;
        int maxLength = 0;
        int size = 0;
        /*
            length = min(size, maxLength)
            start >= 0
            start + length - 1 < size <=> start <= size - length
             
             size, maxLength -> length, start
             length -> start
        */
        public void setMaxLength(int newMaxLength) {
            boolean skipPolicy = size != start + length;
            int diff = maxLength - newMaxLength;
            maxLength = newMaxLength;
            updateLength(diff, skipPolicy);
        }

        private void updateLength(int diff, boolean skipPolicy) {
            if (setLength(diff, skipPolicy)) {
                fireTableDataChanged();
            }
            scrollBar.changeTableParams();
        }

        public void setSize(int newSize) {
            boolean skipPolicy = size != start + length;
            int diff = newSize - size;
            size = newSize;
            updateLength(diff, skipPolicy);
        }

        private boolean setLength(int diff, boolean skipPolicy) {
            int newLength = Math.min(size, maxLength);
            boolean updateNeeded = length != newLength;
            length = newLength;
            if (!skipPolicy && appendToTheEnd) {
                start += diff;
                if (start < 0) {
                    start = 0;
                    updateNeeded = true;
                }
            }
            if (start > size - length) {
                start = size - length;
                updateNeeded = true;
            }
            return updateNeeded;
        }

        private void setStart(int newStart) {
            if (start != newStart && start >= 0 && start <= size - length) {
                start = newStart;
                fireTableDataChanged();
                scrollBar.changeTableParams();
            }
        }

        public int getRowCount() {
            return length;
        }

        public int getColumnCount() {
            return 1;
        }

        public String getColumnName(int columnIndex) {
            return "Row";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return start + rowIndex < data.size() ? data.get(start + rowIndex) : null;
        }

        boolean tableDataChangedLock = false;

        public void fireTableDataChanged() {
            if (!tableDataChangedLock) {
                tableDataChangedLock = true;
                int savedStart = start;
                int savedLength = length;
                do {
                    savedStart = start;
                    savedLength = length;
                    updateRowSizes();
                } while(savedLength != length || savedStart != start);
                super.fireTableDataChanged();;
                resizeTableRows();
                tableDataChangedLock = false;
            }
        }

        public int maxStart() {
            return Math.max(size - length, 0);
        }

        public void addElement(T element) {
            data.add(element);
            setSize(data.size());
        }

        public void updateRowSizes() {
            tableModel.setMaxLength(calculateNewLength());
        }

        Map<Integer, Integer> rowsHeights = new HashMap<Integer, Integer>();

        private int calculateNewLength() {
            int direction = appendToTheEnd ? -1 : 1;
            int start = appendToTheEnd ? tableModel.getRowCount() - 1 : 0;
            int h = getHeight();
            int i = start;
            while (true) {
                if (i < 0 || i > tableModel.getRowCount() - 1) {
                    break;
                }
                Component tableCellRendererComponent = cellRenderer.getTableCellRendererComponent(table,
                        tableModel.getValueAt(i, 0), false, false, i, 0);
                tableCellRendererComponent.setSize(getWidth(), tableCellRendererComponent.getHeight());
                // receive its preffered height
                int height = tableCellRendererComponent.getPreferredSize().height;
                rowsHeights.put(i, height);
//                int oldHeight = table.getRowHeight(i);
//                if (oldHeight != height) {
//                    table.setRowHeight(i, height);
//                }
                if (h < height) {
                    // this row won't fit
                    break;
                }
                h -= height;
                i += direction;
            }

            return (i - start) / direction + h / table.getRowHeight();
        }

        /**
         * Correctly resizes chat area in order to correctly support auto-wrapping in cells.
         */
        private void resizeTableRows() {
            // starting from top row to bootom we resize all rows
            for (int i = 0; i < table.getRowCount(); i++) {
                int height = rowsHeights.get(i);
                // get old row height
                int oldHeight = table.getRowHeight(i);
                if (height != oldHeight) {
                    // changing height of the row
                    table.setRowHeight(i, height);
                }
            }
        }
        

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("My table");
        ArrayList<String> data = new ArrayList<String>();
//        for (int i = 0; i < 1000; i++) {
//            data.add("Row#" + String.valueOf(i));
//        }

        final MyJTable<String> table = new MyJTable<String>(data);
        table.setAppendToTheEnd(true);
        frame.getContentPane().add(table, BorderLayout.CENTER);
        JButton button = new JButton("Add");
        final int[] i = new int[]{0};
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                table.addElement("Row #" + (++i[0]));
            }
        });
        frame.getContentPane().add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
