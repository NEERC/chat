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
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.task.TaskStatus;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;

/**
 * @author Matvey Kazakov
 */
public class AdminTaskList extends JTable {
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

        private void updateTasks(boolean softUpdate) {
            tasks = new ArrayList<Task>();
            users = new ArrayList<UserEntry>();
            taskIds = new HashSet<String>();
            boolean admin = UserRegistry.getInstance().findByName(username).isPower();
            for (Task task: registry.getTasks()) {
                TaskStatus ourStatus = task.getStatus(username);
                if (admin || ourStatus != null) {
                    tasks.add(task);
                    taskIds.add(task.getId());
                }
            }
            for (UserEntry user: UserRegistry.getInstance().getUsers()) {
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


    private class AdminTaskRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }

            setFont(table.getFont());

            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                if (table.isCellEditable(row, column)) {
                    super.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    super.setBackground(UIManager.getColor("Table.focusCellBackground"));
                }
            } else {
                setBorder(noFocusBorder);
            }

            if (value instanceof Task) {
                Task task = (Task) value;
                setText(task.getTitle());
                setIcon(null);
//                setIcon(TaskList.getIcon(task.getVisualState()));
            } else if (value instanceof TaskStatus) {
                TaskStatus status = (TaskStatus) value;
                setIcon(TaskIcon.STATUS.get(status.getType()));
                setText(status.getValue());
            } else {
                setIcon(null);
                setValue(value);
            }
            return this;
        }
    }

}
