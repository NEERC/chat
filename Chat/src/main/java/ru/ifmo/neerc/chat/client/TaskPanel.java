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
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskActions;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.task.TaskStatus;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Matvey Kazakov
 */
public class TaskPanel extends JPanel {
    private String user;
    private JList taskList;
    private AbstractButton btnActionDone;
    private AbstractButton btnActionStart;
    private AbstractButton btnActionFail;
    private Chat chat;

    public TaskPanel(TaskRegistry taskRegistry, UserEntry user, Chat chat) {
        super(new BorderLayout());
        this.user = user.getName();
        this.chat = chat;
        taskList = new TaskList(taskRegistry, this.user);

        taskList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                enableButtons();
            }
        });
        taskRegistry.addListener(new TaskRegistryListener() {
            public void taskChanged(Task taskId) {
                enableButtons();
            }
            public void tasksReset() {
                enableButtons();
            }
        });

        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.NORTH);
        enableButtons();
    }

    private void enableButtons() {
        Task task = null;
        try {
            task = (Task) taskList.getSelectedValue();
        } catch (Exception ignore) {
        }

        enableButton(
                btnActionFail,
                TaskActions.isActionSupported(task, user, TaskActions.ACTION_FAIL)
        );
        enableButton(
                btnActionStart,
                TaskActions.isActionSupported(task, user, TaskActions.ACTION_START)
        );
        enableButton(
                btnActionDone,
                TaskActions.isActionSupported(task, user, TaskActions.ACTION_DONE)
        );
    }

    private void enableButton(Component btn, boolean enable) {
        if (btn != null) {
            btn.setEnabled(enable);
        }
    }

    private Component createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        btnActionDone = createButton(
                TaskActions.ACTION_DONE,
                "Task is done"
        );
        btnActionFail = createButton(
                TaskActions.ACTION_FAIL,
                "Task is failed due to..."
        );
        btnActionStart = createButton(
                TaskActions.ACTION_START,
                "Task is started"
        );
        toolBar.add(btnActionStart);
        toolBar.add(btnActionDone);
        toolBar.add(btnActionFail);
        return toolBar;
    }

    private void performAction(int action) {
        Task task = (Task) taskList.getSelectedValue();
        TaskStatus taskStatus = task.getStatuses().get(user);
        String value = "";
        if (action == TaskActions.ACTION_FAIL) {
            String message = "Give the reason";
            value = JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    message,
                    taskStatus.getValue()
            );
            if (value == null) {
                return;
            }
        }
        if (task.getType().equals(TaskActions.TYPE_QUESTION)) {
            String message = "Your answer";
            value = JOptionPane.showInputDialog(
                    SwingUtilities.getWindowAncestor(this),
                    message,
                    taskStatus.getValue()
            );
            if (value == null) {
                return;
            }
        }

        String status = TaskActions.getNewStatus(task, user, action);
        try {
            chat.write(task, new TaskStatus(status, value));
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(this),
                "Not connected to server",
                "Error",
                JOptionPane.WARNING_MESSAGE
            );
        }
        enableButtons();
    }

    private AbstractButton createButton(final int action, final String toolTipText) {
        AbstractButton btn = new JButton(TaskIcon.ACTION.get(action));
        btn.setFocusable(false);
        btn.setBorderPainted(false);
        btn.setRolloverEnabled(true);
        btn.setToolTipText(toolTipText);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performAction(action);
            }
        });
        return btn;
    }

}
