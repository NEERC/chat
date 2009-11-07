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
 * Date: 29.10.2004
 */
package ru.ifmo.neerc.chat.client;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Matvey Kazakov
 */
public class ChatArea extends JTable {
    private static final int USER_COLUMN_WIDTH = 50;
    private static final int MAXIMUM_LINES = 100;
    private ChatModel model;
    private static final int TIME_COLUMN_WIDTH = 60;
    private TableCellRenderer cellRenderer = new NewChatMessageRenderer();
    private boolean doScroll = false;

    public ChatArea() {
        model = new ChatModel();
        setModel(model);
        setShowHorizontalLines(false);
        setShowGrid(false);
        setTableHeader(null);
        setBackground(Color.white);
        final TableColumn timeColumn = getColumnModel().getColumn(0);
        timeColumn.setResizable(false);
        timeColumn.setCellRenderer(new NewChatMessageRenderer());
        final TableColumn userColumn = getColumnModel().getColumn(1);
        userColumn.setCellRenderer(new NewChatMessageRenderer(Font.BOLD));
        final TableColumn messageColumn = getColumnModel().getColumn(2);
        messageColumn.setCellRenderer(new NewChatMessageRenderer());
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                timeColumn.setPreferredWidth(TIME_COLUMN_WIDTH);
                userColumn.setPreferredWidth(USER_COLUMN_WIDTH);
                messageColumn.setPreferredWidth(getWidth() - TIME_COLUMN_WIDTH - USER_COLUMN_WIDTH);
                if (doScroll) {
                    scrollRectToVisible(new Rectangle(1, 100000, 1, 1));
                }
            }
        });
    }

    public void addToModel(final ChatMessage message) {
        model.add(message);
    }

    public void addMessage(final ChatMessage message) {
        if ((new Date()).getTime() - message.getTimestamp() > 1000) {
            model.add(message);
            doScroll = true;
            return;
        }
        final int index = model.append(message);
        doScroll = false;

        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Component tableCellRendererComponent = cellRenderer.getTableCellRendererComponent(ChatArea.this,
                            message, false, false, index, 2);
                    int height = tableCellRendererComponent.getPreferredSize().height;
                    synchronized (ChatArea.this) {
                        setRowHeight(index, height);
                        Rectangle cellRectPrev = getCellRect(index - 1, 2, true);
                        Rectangle cellRect = getCellRect(index, 2, true);
                        cellRect.setSize(tableCellRendererComponent.getPreferredSize());
                        Rectangle visibleRect = getVisibleRect();
                        if (visibleRect.y + visibleRect.height >= cellRectPrev.y + cellRectPrev.height) {
                            scrollRectToVisible(cellRect);
                        }
                    }
                }
            });
        } catch (InterruptedException e) {
//            e.printStackTrace();
        } catch (InvocationTargetException e) {
//            e.printStackTrace();
        }

    }
    

    private class ChatModel extends AbstractTableModel {
        private ArrayList<ChatMessage> cache = new ArrayList<ChatMessage>();
        private TreeSet<ChatMessage> messages = new TreeSet<ChatMessage>();
        private boolean valid = true;

        private synchronized void validate() {
            if (valid) return;
            cache.clear();
            cache.addAll(messages);
            valid = true;
        }
        
        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return messages.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            validate();
            ChatMessage chatMessage = cache.get(rowIndex);
            if (columnIndex == 0) {
                return chatMessage.getTime();
//            } else if (columnIndex == 1) {
//                UserEntry user = chatMessage.getUser();
//                if (user != null) {
//                    return user.getName();
//                } else {
//                    return "";
//                }
            } else {
                return chatMessage;
            }
        }

        public synchronized int add(ChatMessage message) {
            valid = false;
            return append(message);
        }

        public synchronized int append(ChatMessage message) {
            int size = cache.size();
            if (messages.contains(message)) {
                return size - 1;
            }
            if (size >= MAXIMUM_LINES) {
                messages.remove(messages.first());
                cache.remove(0);
                fireTableRowsDeleted(0, 0);
                size--;
            }
            messages.add(message);
            cache.add(message);
            fireTableRowsInserted(size, size);
            return size;
        }

    }

}
