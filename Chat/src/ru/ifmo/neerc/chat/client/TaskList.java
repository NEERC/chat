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

import ru.ifmo.neerc.chat.Task;
import ru.ifmo.neerc.chat.TaskFactory;
import ru.ifmo.neerc.chat.TaskRegistry;
import ru.ifmo.neerc.chat.TaskRegistryListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <code>TaskList</code> class
 *
 * @author Matvey Kazakov
 */
public class TaskList extends JList {
    private TaskRegistry registry;
    private int userId;
    private static final ImageIcon iconTaskStateNew = new ImageIcon(TaskList.class.getResource("res/task_state_new.gif"));
    private static final ImageIcon iconTaskStateFail = new ImageIcon(TaskList.class.getResource("res/task_state_fail.png"));
    private static final ImageIcon iconTaskStateInProgress = new ImageIcon(TaskList.class.getResource("res/task_state_inprogress.png"));
    private static final ImageIcon iconTaskStateDone = new ImageIcon(TaskList.class.getResource("res/task_state_done.gif"));
    
    private static final Map<Integer, ImageIcon> icons;
    
    static {
        Map<Integer, ImageIcon> map = new HashMap<Integer, ImageIcon>();
        map.put(TaskFactory.VSTATE_DONE, iconTaskStateDone);
        map.put(TaskFactory.VSTATE_FAIL, iconTaskStateFail);
        map.put(TaskFactory.VSTATE_INPROGRESS, iconTaskStateInProgress);
        map.put(TaskFactory.VSTATE_NEW, iconTaskStateNew);
        icons = map;
    }
    
    public static ImageIcon getIcon(int state) {
        return icons.get(state);
    }

    public TaskList(TaskRegistry taskRegistry, int userId) {
        this.registry = taskRegistry;
        this.userId = userId;
        TaskListModel dataModel = new TaskListModel();
        this.registry.addListener(dataModel);
        setModel(dataModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCellRenderer(new PersonalTaskListRenderer());
    }

    private class TaskListModel extends AbstractListModel implements TaskRegistryListener {
        private ArrayList<Task> tasks;

        public TaskListModel() {
            updateTasks();
        }

        public void taskAdded(Task task) {
            updateTasks();
        }

        public void taskDeleted(Task taskId) {
            updateTasks();
        }

        public void taskChanged(Task taskId) {
            updateTasks();
        }

        private void updateTasks() {
            tasks = new ArrayList<Task>(registry.getAssignedTasks(userId));
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
            Task task = (Task)value;
            setText(task.getDescription());
            setIcon(icons.get(task.getResult(userId).getVisualState()));
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

