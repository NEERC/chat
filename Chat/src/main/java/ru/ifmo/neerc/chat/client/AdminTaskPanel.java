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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Matvey Kazakov
 */
public class AdminTaskPanel extends JPanel {

    private Frame owner;
    private AdminTaskList taskList;
    private JButton btnAssignTask;
    private JButton btnRemoveTask;

    private AbstractButton btnActionDone;
    private AbstractButton btnActionStart;
    private AbstractButton btnActionFail;

    public Component adminToolBar;
    public Component userToolBar;
    public Component toolBar;

    private Chat chat;
    private String username;

    public AdminTaskPanel(Frame owner, TaskRegistry taskRegistry, Chat chat, String username) {
        super(new BorderLayout());
        this.owner = owner;
        this.chat = chat;
        this.username = username;
        taskList = new AdminTaskList(taskRegistry, username);

        taskList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                enableButtons();
            }
        });
        taskRegistry.addListener(new TaskRegistryListener() {
            public void taskChanged(Task task) {
                enableButtons();
            }
            public void tasksReset() {
                enableButtons();
            }
        });

        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    performDefaultAction();
                }
            }
        });

        toolBar = createToolBar();

        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);
        enableButtons();

        adminToolBar.setVisible(false);
    }

    private void enableButtons() {
        boolean enable = taskList.getSelectedRow() != -1;
        enableButton(enable, btnAssignTask);
        enableButton(enable, btnRemoveTask);

        Task task = getSelectedTask();
        enableButton(
                TaskActions.isActionSupported(task, username, TaskActions.ACTION_FAIL),
                btnActionFail
        );
        enableButton(
                TaskActions.isActionSupported(task, username, TaskActions.ACTION_START),
                btnActionStart
        );
        enableButton(
                TaskActions.isActionSupported(task, username, TaskActions.ACTION_DONE),
                btnActionDone
        );
    }

    private void enableButton(boolean enable, Component btn) {
        if (btn != null) {
            btn.setEnabled(enable);
        }
    }

    private Component createToolBar() {
        JToolBar toolBar = new JToolBar();

        toolBar.setBorderPainted(false);
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        adminToolBar = createAdminToolBar();
        userToolBar = createUserToolBar();

        toolBar.add(userToolBar);
        toolBar.add(Box.createHorizontalStrut(20));
        toolBar.add(adminToolBar);

        return toolBar;
    }

    private Component createAdminToolBar() {
        JToolBar toolBar = new JToolBar();

        toolBar.setBorderPainted(false);
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        toolBar.add(createAddTaskButton(TaskActions.TYPE_TODO, "Add TODO (@todo)"));
        toolBar.add(createAddTaskButton(TaskActions.TYPE_CONFIRM, "Add Confirmation (@confirm)"));
        toolBar.add(createAddTaskButton(TaskActions.TYPE_REASON, "Add Ok/Fail Reason (@okfail)"));
        toolBar.add(createAddTaskButton(TaskActions.TYPE_QUESTION, "Add Question (@q)"));

        btnAssignTask = createButton(TaskIcon.iconTaskAssign, "Assign Task");
        btnAssignTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Task[] tasks = getSelectedTasks();
                new AssignTaskDialog(owner, chat, tasks).setVisible(true);
                enableButtons();
            }
        });
        toolBar.add(btnAssignTask);

        btnRemoveTask = createButton(TaskIcon.iconTaskRemove, "Delete task");
        btnRemoveTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Task[] tasks = getSelectedTasks();
                for (Task task : tasks) {
                    chat.write(new Task(task.getId(), "remove", ""));
                }
                enableButtons();
            }
        });
        toolBar.add(btnRemoveTask);
        return toolBar;
    }


    private Component createUserToolBar() {
        JToolBar toolBar = new JToolBar();

        toolBar.setBorderPainted(false);
        toolBar.setMargin(new Insets(0, 0, 0, 0));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        btnActionDone = createButton(TaskActions.ACTION_DONE, "Task is done");
        btnActionFail = createButton(TaskActions.ACTION_FAIL, "Task is failed due to...");
        btnActionStart = createButton(TaskActions.ACTION_START, "Task is started");
        toolBar.add(btnActionStart);
        toolBar.add(btnActionDone);
        toolBar.add(btnActionFail);
        return toolBar;
    }

    
    private Component createAddTaskButton(final String type, final String message) {
        JButton btnCreateTask = createButton(
                TaskIcon.TYPE.get(type),
                message
        );
        btnCreateTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String taskDescription = JOptionPane.showInputDialog(AdminTaskPanel.this, message);
                if (taskDescription != null && taskDescription.trim().length() > 0) {
                    Task task = new Task(type, taskDescription);
                    chat.write(task);
                }
                enableButtons();
            }
        });
        return btnCreateTask;
    }

    private JButton createButton(final ImageIcon icon, final String toolTipText) {
        JButton btn = new JButton(icon);
        btn.setFocusable(false);
        btn.setBorderPainted(false);
        btn.setToolTipText(toolTipText);
        btn.setMargin(new Insets(0, 0, 0, 0));
        return btn;
    }

    private JButton createButton(final int action, final String toolTipText) {
        JButton btn = createButton(TaskIcon.ACTION.get(action), toolTipText);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performAction(action);
            }
        });
        return btn;
    }

    private Task[] getSelectedTasks() {
        int[] selectedIndices = taskList.getSelectedRows();
        Task[] tasks = new Task[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            tasks[i] = (Task) taskList.getValueAt(selectedIndices[i], 0);
        }
        return tasks;
    }
    
    private Task getSelectedTask() {
        int selectedRow = taskList.getSelectedRow();
        return selectedRow != -1 ? (Task) taskList.getModel().getValueAt(selectedRow, 0) : null;
    }

    private void performDefaultAction() {
        Task task = getSelectedTask();
        if (task == null) {
            return;
        }
        TaskStatus taskStatus = task.getStatuses().get(username);
        if (taskStatus == null) {
            return;
        }
        String type = task.getType();
        String status = taskStatus.getType();
        
        int action = TaskActions.ACTION_DONE;
        if (type.equals(TaskActions.TYPE_TODO)) {
            action = status.equals(TaskActions.STATUS_RUNNING) ? TaskActions.ACTION_DONE : TaskActions.ACTION_START;
        }
        performAction(action);
    }

    private void performAction(int action) {
        Task task = getSelectedTask();
        if (task == null) {
            return;
        }
        TaskStatus taskStatus = task.getStatuses().get(username);
        if (taskStatus == null) {
            return;
        }
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

        String status = TaskActions.getNewStatus(task, username, action);
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
}
