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

import ru.ifmo.neerc.chat.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Matvey Kazakov
 */
public class AdminTaskList extends JTable {
    private TaskRegistry registry;

    public AdminTaskList(TaskRegistry taskRegistry) {
        this.registry = taskRegistry;
        TaskListModel dataModel = new TaskListModel();
        this.registry.addListener(dataModel);
        UserRegistry.getInstance().addListener(dataModel);
        setModel(dataModel);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        AdminTaskRenderer renderer = new AdminTaskRenderer();
        setDefaultRenderer(Object.class, renderer);
    }

    private class TaskListModel extends AbstractTableModel implements TaskRegistryListener, UserRegistryListener {
        private ArrayList<Task> tasks;
        private ArrayList<UserEntry> users;

        public TaskListModel() {
            updateTasks();
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
                UserEntry currentUser = (UserEntry)users.get(columnIndex - 1);
                Task currentTask = (Task)tasks.get(rowIndex);
                return new TaskObject(currentTask, currentUser);
            }
        }

        public void taskAdded(Task task) {
            int size = tasks.size();
            tasks.add(task);
            fireTableRowsInserted(size, size);
        }

        public void taskDeleted(Task task) {
            int index = tasks.indexOf(task);
            tasks.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public void taskChanged(Task task) {
            int index = tasks.indexOf(task);
            fireTableRowsUpdated(index, index);
        }

        public void userAdded(UserEntry userEntry) {
            updateTasks();
        }

        public void userRemoved(UserEntry userEntry) {
            updateTasks();
        }

        public void userChanged(UserEntry userEntry) {
        }

        private void updateTasks() {
            tasks = new ArrayList<Task>(registry.getTasks());
            users = new ArrayList<UserEntry>(UserRegistry.getInstance().getUsers());
            Collections.sort(users);
            fireTableStructureChanged();
        }

        public String getColumnName(int column) {
            return (column == 0) ? "Task" : ((UserEntry)users.get(column - 1)).getName();
        }
    }

    private class TaskObject {
        public static final int UNASSIGNED = 0;
        public static final int ASSIGNED = 1;
        public static final int COMPLETED = 2;
        private int state;
        private String message = null;

        public TaskObject(Task task, UserEntry user) {
            TaskResult taskResult = task.getResult(user.getId());
            if (taskResult == null) {
                state = -1;
            } else {
                state = taskResult.getVisualState();
                message = taskResult.toString();
            }
        }

        public int getVisualState() {
            return state;
        }

        public String getMessage() {
            return message;
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
                Task task = (Task)value;
                setText(task.getDescription());
                setIcon(TaskList.getIcon(task.getVisualState()));
            } else if (value instanceof TaskObject) {
                TaskObject taskObject = (TaskObject)value;
                setIcon(TaskList.getIcon(taskObject.getVisualState()));
                setText(taskObject.getMessage());
            } else {
                setValue(value);
            }

            return this;
        }
    }

}

