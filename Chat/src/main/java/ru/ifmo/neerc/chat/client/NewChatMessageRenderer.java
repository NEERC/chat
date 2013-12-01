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
package ru.ifmo.neerc.chat.client;
// $Id$
/**
 * Date: 28.10.2005
 */

import ru.ifmo.neerc.chat.user.UserEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matvey Kazakov
 */
public class NewChatMessageRenderer extends JTextArea implements TableCellRenderer {
    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
    private UserEntry currentUser;

    /**
     * map from table to map of rows to map of column heights
     */
    private final Map<JTable, Map<Integer, Map<Integer, Integer>>> cellSizes
            = new HashMap<JTable, Map<Integer, Map<Integer, Integer>>>();
    private int fontStyle = -1;
    private NameColorizer nameColorizer;

    public NewChatMessageRenderer() {
        this(-1);
    }

    public NewChatMessageRenderer(UserEntry user) {
        this(-1);
        currentUser = user;
    }

    public NewChatMessageRenderer(int fontStyle) {
        this.fontStyle = fontStyle;
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public NewChatMessageRenderer(int fontStyle, NameColorizer colorizer, UserEntry user) {
        this(fontStyle);
        nameColorizer = colorizer;
        currentUser = user;
    }

    public Component getTableCellRendererComponent(//
                                                   JTable table, Object obj, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        // set the colours, etc. using the standard for that platform
        adaptee.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
        setForeground(adaptee.getForeground());
        setBackground(adaptee.getBackground());
        setBorder(adaptee.getBorder());
        if (fontStyle != -1) {
            setFont(adaptee.getFont().deriveFont(fontStyle));
        } else {
            setFont(adaptee.getFont());
        }
        if (obj instanceof ChatMessage && column == 2) {
            ChatMessage message = (ChatMessage) obj;
            updateRenderer(message);
        } else if (obj instanceof UserEntry && column == 1) {
            ChatMessage message = (ChatMessage) table.getValueAt(row, 2);
            UserEntry user = message.getUser();
            setText(user == null ? "" : user.getName());
            setForeground(generateColor(user));
        } else {
            setText(adaptee.getText());
        }

        // This line was very important to get it working with JDK1.4
        TableColumnModel columnModel = table.getColumnModel();
        setSize(columnModel.getColumn(column).getWidth(), 100000);
        int height_wanted = (int) getPreferredSize().getHeight();
        addSize(table, row, column, height_wanted);
        height_wanted = findTotalMaximumRowSize(table, row);
        if (height_wanted != table.getRowHeight(row)) {
            table.setRowHeight(row, height_wanted);
        }
        return this;
    }

    private void updateRenderer(ChatMessage message) {
        switch (message.getType()) {
            case SERVER_MESSAGE:
                setFont(adaptee.getFont().deriveFont(Font.BOLD));
                setForeground(Color.BLUE);
                setText(">>>>>   " + message.getText() + "   <<<<<");
                break;
            case TASK_MESSAGE:
                setFont(adaptee.getFont().deriveFont(Font.BOLD).deriveFont(20.0f));
                setForeground(Color.RED);
                setText(message.getText());
                break;
            case USER_MESSAGE:
                UserEntry user = message.getUser();
                String messageText = message.getText();
                boolean importantToUs = currentUser != null && messageText.contains(currentUser.getName());

                if (importantToUs) {
                    setForeground(Color.RED);
                }
                char c = '.';
                if (messageText != null && messageText.length() > 0) {
                    c = messageText.charAt(0);
                }

                if (user.isPower()) {

                    if (c == '#' || c == '\uFFFD') {
                        messageText = setupPowerMessage(messageText, Color.green.darker());
                    } else if (c == '!') {
                        messageText = setupPowerMessage(messageText, Color.red);
                    } else if (c == '?') {
                        messageText = setupPowerMessage(messageText, Color.blue.darker());
                    }  else if (message.isPrivate()) {
                        setForeground(new Color(0xFF5767));
                        setFont(adaptee.getFont().deriveFont(Font.BOLD));
                    }
                } else if (message.isPrivate() && c == '%') {
                    setForeground(Color.BLUE);
                } else if (message.isPrivate()) {
                    setForeground(new Color(0xFF5767));
					setFont(adaptee.getFont().deriveFont(Font.BOLD));
                }
                setText(messageText);
                break;
        }
    }

    private String setupPowerMessage(String messageText, Color fg) {
        char c = messageText.charAt(0);
        int counter = 0;
        while (counter < messageText.length() && messageText.charAt(counter) == c) {
            counter++;
        }
        messageText = messageText.substring(counter);
        setForeground(fg);
        setFont(adaptee.getFont().deriveFont((float) (12.0 + (counter - 1) * 12.0)).deriveFont(Font.BOLD));
        return messageText;
    }

    private synchronized void addSize(JTable table, int row, int column, int height) {
        Map<Integer, Map<Integer, Integer>> rows = cellSizes.get(table);
        if (rows == null) {
            cellSizes.put(table, rows = new HashMap<Integer, Map<Integer, Integer>>());
        }
        Map<Integer, Integer> rowheights = rows.get(row);
        if (rowheights == null) {
            rows.put(row, rowheights = new HashMap<Integer, Integer>());
        }
        rowheights.put(column, height);
    }

    /**
     * Look through all columns and get the renderer.  If it is
     * also a ru.ifmo.neerc.chat.client.NewChatMessageRenderer, we look at the maximum height in
     * its hash table for this row.
     */
    private int findTotalMaximumRowSize(JTable table, int row) {
        int maximum_height = 1;
        Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableCellRenderer cellRenderer = columns.nextElement().getCellRenderer();
            if (cellRenderer instanceof NewChatMessageRenderer) {
                NewChatMessageRenderer tar = (NewChatMessageRenderer) cellRenderer;
                maximum_height = Math.max(maximum_height, tar.findMaximumRowSize(table, row));
            }
        }
        return maximum_height;
    }

    private int findMaximumRowSize(JTable table, int row) {
        Map<Integer, Map<Integer, Integer>> rows = cellSizes.get(table);
        if (rows == null) {
            return 1;
        }
        Map<Integer, Integer> rowheights = rows.get(row);
        if (rowheights == null) {
            return 1;
        }
        int maximum_height = 1;
        for (Map.Entry<Integer, Integer> entry : rowheights.entrySet()) {
            int cellHeight = entry.getValue();
            maximum_height = Math.max(maximum_height, cellHeight);
        }
        return maximum_height;
    }

    public Color generateColor(UserEntry name) {
        if (nameColorizer == null) return Color.BLACK;
        return nameColorizer.generateColor(name);
    }

}
