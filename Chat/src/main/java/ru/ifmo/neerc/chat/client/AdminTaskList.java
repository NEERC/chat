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
import ru.ifmo.neerc.chat.xmpp.XmppChat;
import ru.ifmo.neerc.task.*;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Component;
import java.awt.Insets;
import java.util.*;

/**
 * @author Matvey Kazakov
 */
public class AdminTaskList extends JTable {
	private static final Logger LOG = LoggerFactory.getLogger(XmppChat.class);
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private static final int TASK_DEFAULT_WIDTH = 250;

    private TaskRegistry registry;
    private String username;

	private TaskListModel dataModel;

	private TableRowSorter<TaskListModel> sorter;
	private SortOrder lastOrder = SortOrder.DESCENDING;

    public AdminTaskList(TaskRegistry taskRegistry, String username) {
        this.registry = taskRegistry;
        this.username = username;
        dataModel = new TaskListModel(username);
        this.registry.addListener(dataModel);
        UserRegistry.getInstance().addListener(dataModel);
        setModel(dataModel);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        AdminTaskRenderer renderer = new AdminTaskRenderer();
        setDefaultRenderer(Object.class, renderer);
        sorter = new TableRowSorter<>(dataModel);
        setRowSorter(sorter);
        sorter.setSortsOnUpdates(true);
    }
    
    private void sort() {
    	List<? extends SortKey> keys = sorter.getSortKeys();
    	if (keys != null && keys.isEmpty()) {
	    	ArrayList<SortKey> list = new ArrayList<SortKey>();
	    	list.add( new RowSorter.SortKey(0, lastOrder));
	    	sorter.setSortKeys(list);
    	}
        sorter.sort();
    }
    
    private void saveSort() {
    	if (sorter == null) {
    		return;
    	}
    	
    	List<? extends SortKey> keys = sorter.getSortKeys();
    	if (keys != null && !keys.isEmpty()) {
    		if (keys.get(0).getSortOrder() != SortOrder.UNSORTED) {
    			lastOrder = keys.get(0).getSortOrder();
    		}
    	}
    }
    
    public void doLayout() {
        final TableColumn taskColumn = getColumnModel().getColumn(0);
        taskColumn.setPreferredWidth(TASK_DEFAULT_WIDTH);
        super.doLayout();
    }
    
    private class TaskListModel extends AbstractTableModel implements TaskRegistryListener, UserRegistryListener {
        private static final long serialVersionUID = 7990317100207622830L;

        private List<Task> tasks = new ArrayList<>();
        private ArrayList<UserEntry> users = new ArrayList<>();
        private String username;
        

        public TaskListModel(String username) {
            this.username = username;
            updateTasks();
        }

        public int getColumnCount() {
            return users.size() + 1;
        }

        public int getRowCount() {
            return tasks.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
                return null;
            }
            
            if (columnIndex == 0) {
                return tasks.get(rowIndex);
            } else {
                UserEntry currentUser = users.get(columnIndex - 1);
                Task currentTask = tasks.get(rowIndex);
                return currentTask.getStatuses().get(currentUser.getName());
            }
        }

        public void taskChanged(Task task) {
        	for (int i = 0; i < tasks.size(); ++i) {
        		if (tasks.get(i).getId().equals(task.getId())) {
        			updateTask(i, task);
        			return;
        		}
        	}
            
            insertTask(task);
        }

        public void tasksReset() {
            updateTasks();
        }

        public void userChanged(UserEntry userEntry) {
            updateTasks();
        }

        public void userPresenceChanged(UserEntry userEntry) {
            // ignore
        }

        private void updateTask(int position, Task task) {
            if ("remove".equals(task.getType())) {
            	saveSort();
                tasks.remove(position);
                fireTableRowsDeleted(position, position);
                updateTasks();
                return;
            }
            
            tasks.set(position, task);
            fireTableRowsUpdated(position, position);
            
            Set<String> taskUsers = new HashSet<>();
            taskUsers.addAll(task.getStatuses().keySet());
            for (UserEntry ue : users) {
            	taskUsers.remove(ue.getName());
            }
            if (!taskUsers.isEmpty()) {
            	updateTasks();
            }
        }
        
        private boolean isAdmin() {
            return UserRegistry.getInstance().findByName(username).isPower();
        }
        
        private boolean isTaskRelevant(Task task) {
            return isAdmin() || task.getStatus(username) != null;
        }
        
        private boolean isUserRelevant(UserEntry user) {
            return (isAdmin() && user.getName().toLowerCase().startsWith("hall")) || userHasTasks(user);
        }
        
        private boolean userHasTasks(UserEntry user) {
        	for (Task t : tasks) {
        		if (t.getStatuses().keySet().contains(user.getName())) {
        			return true;
        		}
        	}
        	
        	return false;
        }

        private void insertTask(Task task) {
            if (isTaskRelevant(task)) {
                tasks.add(task);
                fireTableRowsInserted(tasks.size() - 1, tasks.size() - 1);
                
                Set<String> taskUsers = new HashSet<>();
                taskUsers.addAll(task.getStatuses().keySet());
                for (UserEntry ue : users) {
                	taskUsers.remove(ue.getName());
                }
                if (!taskUsers.isEmpty()) {
                	updateTasks();
                }
                
                if (tasks.size() == 1) {
                	sort();
                }
            }
        }
        
        private void updateTasks() {
        	saveSort();
            tasks = new ArrayList<Task>();
            users = new ArrayList<UserEntry>();
            for (Task task : registry.getTasks()) {
                if (isTaskRelevant(task)) {
                    tasks.add(task);
                }
            }
            for (UserEntry user : UserRegistry.getInstance().getUsers()) {
                if (isUserRelevant(user)) {
                    users.add(user);
                }
            }
            Collections.sort(users);
	        fireTableStructureChanged();
	        for (int i = 1; i < users.size(); ++i) {
	        	sorter.setSortable(i, false);
	        }
        }

        public String getColumnName(int column) {
            return (column == 0) ? "Task" : users.get(column - 1).getName();
        }
        
        @Override
        public Class<?> getColumnClass(int i) {
			return Task.class;
        	
        }
    }


    private class AdminTaskRenderer extends JTextPane implements TableCellRenderer {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final Map<Integer, Map<Integer, Integer>> cellSizes
                = new HashMap<Integer, Map<Integer, Integer>>();

        private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            adaptee.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setForeground(adaptee.getForeground());
            setBackground(adaptee.getBackground());
            setFont(adaptee.getFont());
            setMargin(new Insets(1, 1, 1, 1));

            setText("");
            setToolTipText(null);
            if (value instanceof Task) {
                Task task = (Task) value;
                setText(task.getTitle());
                setToolTipText(task.getTitle());

                TaskStatus ourStatus = task.getStatus(username);
                String displayStatus = null;
                
                if (ourStatus != null) {
                    displayStatus = ourStatus.getType();
                } else if (task.getStatuses().size() > 0) {
                    displayStatus = TaskActions.STATUS_SUCCESS;
                    for (TaskStatus taskStatus : task.getStatuses().values()) {
                        if (TaskActions.STATUS_FAIL.equals(taskStatus.getType())) {
                            displayStatus = TaskActions.STATUS_FAIL;
                            break;
                        } else if (TaskActions.STATUS_RUNNING.equals(taskStatus.getType())) {
                            displayStatus = TaskActions.STATUS_RUNNING;
                            break;
                        } else if (TaskActions.STATUS_SUCCESS.equals(taskStatus.getType())) {
                        } else {
                            displayStatus = TaskActions.STATUS_NEW;
                            break;
                        }
                    }
                }
                
                if (displayStatus != null) {
                    setCaretPosition(0);
                    insertIcon(TaskIcon.STATUS.get(displayStatus));
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
