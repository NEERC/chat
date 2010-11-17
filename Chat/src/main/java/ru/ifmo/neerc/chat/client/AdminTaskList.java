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
// $Id: AdminTaskList.java,v 1.3 2007/10/28 07:32:12 matvey Exp $
/**
 * Date: 29.11.2004
 */
package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.chat.user.UserRegistryListener;
import ru.ifmo.neerc.task.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.*;

/**
 * @author Matvey Kazakov
 */
public class AdminTaskList extends JTable {
    private static final int TASK_DEFAULT_WIDTH = 250;

    private TaskRegistry registry;
    private String username;

    public AdminTaskList(TaskRegistry taskRegistry, String username) {
        this.registry = taskRegistry;
        this.username = username;
        TaskListModel dataModel = new TaskListModel(username);
        this.registry.addListener(dataModel);
        UserRegistry.getInstance().addListener(dataModel);
        setModel(dataModel);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        AdminTaskRenderer renderer = new AdminTaskRenderer();
        setDefaultRenderer(Object.class, renderer);
    }
    
    public void doLayout() {
        final TableColumn taskColumn = getColumnModel().getColumn(0);
        taskColumn.setPreferredWidth(TASK_DEFAULT_WIDTH);
        super.doLayout();
    }

    private class TaskListModel extends AbstractTableModel implements TaskRegistryListener, UserRegistryListener {
        private static final long serialVersionUID = 7990317100207622830L;

        private ArrayList<Task> tasks;
        private HashSet<String> taskIds;
        private ArrayList<UserEntry> users;
        private String username;

        public TaskListModel(String username) {
            this.username = username;
            updateTasks(false);
        }

        public int getColumnCount() {
            return users.size() + 1;
        }

        public int getRowCount() {
            return tasks.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return tasks.get(rowIndex);
            } else {
                UserEntry currentUser = users.get(columnIndex - 1);
                Task currentTask = tasks.get(rowIndex);
                return currentTask.getStatuses().get(currentUser.getName());
            }
        }

        public void taskChanged(Task task) {
            if (taskIds.contains(task.getId()) && !("remove".equals(task.getType()))) {
                updateTasks(true);
            } else {
                updateTasks(false);
            }
        }

        public void tasksReset() {
            updateTasks(false);
        }

        public void userChanged(UserEntry userEntry) {
            updateTasks(false);
        }

        public void userPresenceChanged(UserEntry userEntry) {
            // ignore
        }

        private void updateTasks(boolean softUpdate) {
            tasks = new ArrayList<Task>();
            users = new ArrayList<UserEntry>();
            taskIds = new HashSet<String>();
            boolean admin = UserRegistry.getInstance().findByName(username).isPower();
            for (Task task : registry.getTasks()) {
                TaskStatus ourStatus = task.getStatus(username);
                if (admin || ourStatus != null) {
                    tasks.add(task);
                    taskIds.add(task.getId());
                }
            }
            for (UserEntry user : UserRegistry.getInstance().getUsers()) {
                if (admin || !user.isPower()) {
                    users.add(user);
                }
            }
            Collections.sort(users);
            if (softUpdate) {
                fireTableDataChanged();
            } else {
                fireTableStructureChanged();
            }
        }

        public String getColumnName(int column) {
            return (column == 0) ? "Task" : users.get(column - 1).getName();
        }
    }


    private class AdminTaskRenderer extends JTextPane implements TableCellRenderer {
        private final Map<Integer, Map<Integer, Integer>> cellSizes
                = new HashMap<Integer, Map<Integer, Integer>>();

        private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setForeground(adaptee.getForeground());
            setBackground(adaptee.getBackground());
            setFont(adaptee.getFont());

            setText("");
            setToolTipText(null);
            if (value instanceof Task) {
                Task task = (Task) value;
                setText(task.getTitle());
                setToolTipText(task.getTitle());

                String status = TaskActions.STATUS_SUCCESS;
                if (task.getStatuses().size() > 0) {
                    for (TaskStatus taskStatus : task.getStatuses().values()) {
                        if (TaskActions.STATUS_FAIL.equals(taskStatus.getType())) {
                            status = TaskActions.STATUS_FAIL;
                            break;
                        } else if (TaskActions.STATUS_RUNNING.equals(taskStatus.getType())) {
                            status = TaskActions.STATUS_RUNNING;
                            break;
                        } else if (TaskActions.STATUS_SUCCESS.equals(taskStatus.getType())) {
                        } else {
                            status = TaskActions.STATUS_NEW;
                            break;
                        }
                    }
                    setCaretPosition(0);
                    insertIcon(TaskIcon.STATUS.get(status));
                }
            } else if (value instanceof TaskStatus) {
                TaskStatus status = (TaskStatus) value;
                setText(status.getValue());
                setCaretPosition(0);
                insertIcon(TaskIcon.STATUS.get(status.getType()));
                setToolTipText(status.getValue());
            }

            TableColumnModel columnModel = table.getColumnModel();
            setSize(columnModel.getColumn(column).getWidth(), 100000);
            int height_wanted = (int) getPreferredSize().getHeight();

            if (!cellSizes.containsKey(row)) {
                cellSizes.put(row, new HashMap<Integer, Integer>());
            }
            cellSizes.get(row).put(column, height_wanted);
            int maxHeight = 10;
            for (Integer h: cellSizes.get(row).values()) {
                maxHeight = Math.max(maxHeight, h);
            }

            if (maxHeight != table.getRowHeight(row)) {
                table.setRowHeight(row, maxHeight);
            }

            return this;
        }
    }
}
