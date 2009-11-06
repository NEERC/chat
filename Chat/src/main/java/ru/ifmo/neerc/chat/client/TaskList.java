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
/*
 * Date: Oct 28, 2004
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.task.TaskStatus;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * <code>TaskList</code> class
 *
 * @author Matvey Kazakov
 */
public class TaskList extends JList {
    private TaskRegistry registry;

    private String user;

    public TaskList(TaskRegistry taskRegistry, String user) {
        this.user = user;
        this.registry = taskRegistry;
        TaskListModel dataModel = new TaskListModel();
        this.registry.addListener(dataModel);
        setModel(dataModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new PersonalTaskListRenderer());
    }

    private class TaskListModel extends AbstractListModel implements TaskRegistryListener {
        private static final long serialVersionUID = -3486815875540806367L;

        private ArrayList<Task> tasks;

        public TaskListModel() {
            updateTasks();
        }

        public void taskChanged(Task taskId) {
            updateTasks();
        }

        public void tasksReset() {
            updateTasks();
        }

        private void updateTasks() {
            tasks = new ArrayList<Task>(registry.getAssignedTasks(user));
            fireContentsChanged(this, 0, tasks.size());
        }

        public int getSize() {
            return tasks.size();
        }

        public Object getElementAt(int index) {
            return tasks.get(index);
        }
    }

    private class PersonalTaskListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Task task = (Task) value;
            setText(task.getTitle());
            TaskStatus taskStatus = task.getStatuses().get(user);
            if (taskStatus != null) {
                setIcon(TaskIcon.STATUS.get(taskStatus.getType()));
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

}
