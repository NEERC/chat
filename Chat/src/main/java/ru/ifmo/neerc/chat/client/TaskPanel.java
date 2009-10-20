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

import ru.ifmo.neerc.chat.*;
import ru.ifmo.neerc.chat.message.TaskMessage;

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
//    private static final ImageIcon iconTaskAdd = new ImageIcon(TaskPanel.class.getResource("res/task_add.gif"));
//    private static final ImageIcon iconTaskAssign = new ImageIcon(TaskPanel.class.getResource("res/task_assign.gif"));
//    private static final ImageIcon iconTaskRemove = new ImageIcon(TaskPanel.class.getResource("res/task_remove.gif"));


    private static final ImageIcon iconActionDone = new ImageIcon(TaskPanel.class.getResource("res/task_action_complete.gif"));
    private static final ImageIcon iconActionFail = new ImageIcon(TaskPanel.class.getResource("res/task_action_fail.png"));
    private static final ImageIcon iconActionStart = new ImageIcon(TaskPanel.class.getResource("res/task_action_start.png"));

    private int userId;
    private Chat clientReader;
    private JList taskList;
    private AbstractButton btnActionDone;
    private AbstractButton btnActionStart;
    private AbstractButton btnActionFail;

    public TaskPanel(TaskRegistry taskRegistry, UserEntry user, Chat clientReader) {
        super(new BorderLayout());
        this.userId = user.getId();
        this.clientReader = clientReader;
        taskList = new TaskList(taskRegistry, userId);

        taskList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                enableButtons();
            }
        });
        taskRegistry.addListener(new TaskRegistryListener() {
            public void taskAdded(Task task) {
                enableButtons();
            }

            public void taskDeleted(Task task) {
                enableButtons();
            }

            public void taskChanged(Task taskId) {
                enableButtons();
            }
        });

        add(new JScrollPane(taskList), BorderLayout.CENTER);
        add(createToolBar(), BorderLayout.NORTH);
        enableButtons();
    }

    static JDialog createPowerDialog(Frame frame, TaskRegistry taskRegistry, UserEntry user, ClientReader clientReader) {
        JDialog dialog = new JDialog(frame);
        dialog.setTitle("Tasks");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new TaskPanel(taskRegistry, user, clientReader));
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(frame);
        return dialog;
    }

    private void enableButtons() {
        Task task = null;
        try {
            task = (Task) taskList.getSelectedValue();
        } catch (Exception e) {
        }
        enableButton(task != null && task.getResult(userId).actionSupported(TaskFactory.ACTION_FAIL), btnActionFail);
        enableButton(task != null && task.getResult(userId).actionSupported(TaskFactory.ACTION_START), btnActionStart);
        enableButton(task != null && task.getResult(userId).actionSupported(TaskFactory.ACTION_DONE), btnActionDone);
    }

    private void enableButton(boolean enable, Component btn) {
        if (btn != null) {
            btn.setEnabled(enable);
        }
    }

    private Component createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        btnActionDone = createButton(iconActionDone, TaskFactory.ACTION_DONE, "Task is done");
        btnActionFail = createButton(iconActionFail, TaskFactory.ACTION_FAIL, "Task is failed due to...");
        btnActionStart = createButton(iconActionStart, TaskFactory.ACTION_START, "Task is started");
        toolBar.add(btnActionStart);
        toolBar.add(btnActionDone);
        toolBar.add(btnActionFail);
        return toolBar;
    }

    private void performAction(int action) {
        Task task = (Task) taskList.getSelectedValue();
        TaskResult taskResult = task.getResult(userId);
        if (action == TaskFactory.ACTION_FAIL || action == TaskFactory.ACTION_DONE && taskResult instanceof QuestionTaskResult) {
            String message = action == TaskFactory.ACTION_FAIL ? "Give the reason" : "Your answer is";
            String reason = JOptionPane.showInputDialog(SwingUtilities.getWindowAncestor(this), message,
                    taskResult.toString());
            if (reason == null || reason.length() == 0) {
                return;
            }
            taskResult.performAction(action, reason);
        } else {
            taskResult.performAction(action);
        }
        clientReader.write(new TaskMessage(TaskMessage.COMPLETE, userId, task, taskResult));
        enableButtons();
    }

    private AbstractButton createButton(ImageIcon icon, final int action, String s) {
        AbstractButton btn = new JButton(icon);
        btn.setFocusable(false);
        btn.setBorderPainted(false);
        btn.setRolloverEnabled(true);
        btn.setToolTipText(s);
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performAction(action);
            }
        });
        return btn;
    }

}
