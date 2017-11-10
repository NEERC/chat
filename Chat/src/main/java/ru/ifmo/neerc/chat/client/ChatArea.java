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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;

/**
 * @author Matvey Kazakov
 */
public class ChatArea extends JTable {
    private static final int TIME_COLUMN_WIDTH = 60;
    private static final int USER_COLUMN_WIDTH = 50;
    private static final int MAXIMUM_LINES = 1000;

    private final ChatModel model;

    private boolean forceScroll = false;

    private ArrayList<UserPickListener> userPickListeners = new ArrayList<>();

    public ChatArea() {
        this(null, null, null);
    }

    public ChatArea(UserEntry user, NameColorizer colorizer, ChannelList channels) {
        model = new ChatModel(channels);
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(final TableModelEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        updateRowHeights(e.getFirstRow(), e.getLastRow());
                        scrollToBottomIfNeeded();
                    }
                });
            }
        });

        setModel(model);

        setShowHorizontalLines(false);
        setShowGrid(false);
        setTableHeader(null);
        setBackground(Color.white);
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);

        final TableColumn timeColumn = getColumnModel().getColumn(0);
        timeColumn.setMinWidth(TIME_COLUMN_WIDTH);
        timeColumn.setMaxWidth(TIME_COLUMN_WIDTH);
        timeColumn.setResizable(false);
        timeColumn.setCellRenderer(new NewChatMessageRenderer());

        final TableColumn userColumn = getColumnModel().getColumn(1);
        userColumn.setMinWidth(USER_COLUMN_WIDTH);
        userColumn.setMaxWidth(USER_COLUMN_WIDTH);
        userColumn.setCellRenderer(new NewChatMessageRenderer(Font.BOLD, colorizer, user));

        final TableColumn messageColumn = getColumnModel().getColumn(2);
        messageColumn.setCellRenderer(new NewChatMessageRenderer(user));

        getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {

            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
                updateRowHeights(0, getRowCount() - 1);
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {

            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {

            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = rowAtPoint(evt.getPoint());
                int col = columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 1) {
                    UserEntry entry = (UserEntry)model.getValueAt(row, col);
                    if (entry != null) {
                        for (UserPickListener listener: userPickListeners) {
                            listener.userPicked((UserEntry) model.getValueAt(row, col));
                        }
                    }
                }
            }
        });
    }

    public void addUserPickListener(UserPickListener listener) {
        userPickListeners.add(listener);
    }

    public void addMessage(final Message message) {
        if ((new Date()).getTime() - message.getDate().getTime() > 1000) {
            model.add(message);
            forceScroll = true;
        } else {
            model.append(message);
            forceScroll = false;
        }
    }

    protected void updateRowHeights(int firstRow, int lastRow) {
        for (int row = firstRow; row <= lastRow; row++) {
            int rowHeight = 0;

            for (int column = 0; column < getColumnCount(); column++) {
                Component comp = prepareRenderer(getCellRenderer(row, column), row, column);
                rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
            }

            if (rowHeight != getRowHeight(row)) {
                setRowHeight(row, rowHeight);
            }
        }
    }

    protected void scrollToBottomIfNeeded() {
        Rectangle prevRect = getCellRect(getRowCount() - 2, 0, true);
        if (getVisibleRect().contains(prevRect) || forceScroll) {
            scrollRectToVisible(getCellRect(getRowCount() - 1, 0, true));
        }
    }

    private class ChatModel extends AbstractTableModel {
        private ArrayList<Message> cache = new ArrayList<Message>();
        private TreeSet<Message> messages = new TreeSet<Message>();
        private ChannelList channels;
        private boolean valid = true;

        public ChatModel(ChannelList channels) {
            this.channels = channels;

            if (this.channels != null) {
                this.channels.addListener(new SubscriptionListener() {
                    public void subscriptionChanged() {
                        valid = false;
                        validate();
                        fireTableDataChanged();
                    }
                });
            }
        }

        public boolean isMessageVisible(Message message) {
            return (channels == null
                || message.getChannel() == null
                || (!channels.isSeparated() && channels.isSubscribed(message.getChannel()))
            );
        }

        private synchronized void validate() {
            if (valid) return;
            cache.clear();
            for (Message message : messages) {
                if (isMessageVisible(message))
                    cache.add(message);
            }
            valid = true;
        }

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return cache.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            validate();
            Message message = cache.get(rowIndex);
            if (columnIndex == 0) {
                return message.getDate();
            } else if (columnIndex == 1) {
                return message.getUser();
            } else {
                return message;
            }
        }

        public synchronized int add(Message message) {
            valid = false;
            return append(message);
        }

        public synchronized int append(Message message) {
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
            if (isMessageVisible(message))
                cache.add(message);
            fireTableRowsInserted(size, size);
            return size;
        }

    }

}
