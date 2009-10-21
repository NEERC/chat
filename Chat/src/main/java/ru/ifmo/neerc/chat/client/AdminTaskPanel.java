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
public class AdminTaskPanel extends JPanel {
    private static final ImageIcon iconTaskAddTodo = new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_todo.png"));
    private static final ImageIcon iconTaskAddConfirm = new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_confirm.png"));
    private static final ImageIcon iconTaskAddText = new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_text.png"));
    private static final ImageIcon iconTaskAddQuest = new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_quest.png"));
    private static final ImageIcon iconTaskAssign = new ImageIcon(AdminTaskPanel.class.getResource("res/task_assign.gif"));
    private static final ImageIcon iconTaskRemove = new ImageIcon(AdminTaskPanel.class.getResource("res/task_remove.gif"));
    private static final ImageIcon iconTaskImport = new ImageIcon(AdminTaskPanel.class.getResource("res/task_import.png"));

    private Frame owner;
    private TaskRegistry registry;
    private ClientReader clientReader;
    private AdminTaskList taskList;
    private JButton btnAssignTask;
    private JButton btnRemoveTask;

    public AdminTaskPanel(Frame owner, TaskRegistry taskRegistry, ClientReader clientReader) {
        super(new BorderLayout());
        this.owner = owner;
        this.registry = taskRegistry;
        this.clientReader = clientReader;
        taskList = new AdminTaskList(taskRegistry);

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
        add(createToolBar(), BorderLayout.WEST);
        enableButtons();
    }

    static JDialog createPowerDialog(Frame frame, TaskRegistry taskRegistry, ClientReader clientReader) {
        JDialog dialog = new JDialog(frame);
        dialog.setTitle("Tasks");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new AdminTaskPanel(frame, taskRegistry, clientReader));
        dialog.setSize(300, 300);
        dialog.setLocationRelativeTo(frame);
        return dialog;
    }

    private void enableButtons() {
        boolean enable = taskList.getSelectedRowCount() > 0;
        enableButton(enable, btnAssignTask);
        enableButton(enable, btnRemoveTask);
    }

    private void enableButton(boolean enable, Component btn) {
        if (btn != null) {
            btn.setEnabled(enable);
        }
    }

    private Component createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setOrientation(JToolBar.VERTICAL);
        toolBar.setRollover(true);
        toolBar.add(createAddTaskButton(iconTaskAddTodo, TaskFactory.TASK_TODO, "Add TODO"));
        toolBar.add(createAddTaskButton(iconTaskAddConfirm, TaskFactory.TASK_CONFIRM, "Add Confirmation"));
        toolBar.add(createAddTaskButton(iconTaskAddText, TaskFactory.TASK_REASON, "Add Ok/Fail Reason"));
        toolBar.add(createAddTaskButton(iconTaskAddQuest, TaskFactory.TASK_QUESTION, "Add Question"));
        btnAssignTask = createButton(iconTaskAssign, "Assign Task");
        btnAssignTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Task[] tasks = getSelectedTasks();
                new AssignTaskDialog(owner, clientReader, tasks).setVisible(true);
                enableButtons();
            }
        });
        toolBar.add(btnAssignTask);
        btnRemoveTask = createButton(iconTaskRemove, "Delete task");
        btnRemoveTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Task[] tasks = getSelectedTasks();
                for (int i = 0; i < tasks.length; i++) {
                    Task task = tasks[i];
                    clientReader.write(new TaskMessage(TaskMessage.DELETE, -1, task, null));
                }
                enableButtons();
            }
        });
        toolBar.add(btnRemoveTask);
        JButton btnImportTasks = createButton(iconTaskImport, "Import tasks");
        btnImportTasks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ImportTasksDialog(owner, clientReader).setVisible(true);
            }
        });
        toolBar.add(btnImportTasks);
        return toolBar;
    }

    private JButton createAddTaskButton(ImageIcon icon, final int type, final String message) {
        JButton btnCreateTask = createButton(icon, message);
        btnCreateTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String taskDescription = JOptionPane.showInputDialog(AdminTaskPanel.this, message);
                if (taskDescription != null && taskDescription.trim().length() > 0) {
                    clientReader.write(new TaskMessage(TaskMessage.CREATE, -1, registry.createTask(taskDescription, type), null));
                }
                enableButtons();
            }
        });
        return btnCreateTask;
    }

    private JButton createButton(ImageIcon icon, String toolTip) {
        JButton btnCreateTask = new JButton(icon);
        btnCreateTask.setBorderPainted(false);
        btnCreateTask.setToolTipText(toolTip);
        btnCreateTask.setMargin(new Insets(0, 0, 0, 0));
        return btnCreateTask;
    }

    private Task[] getSelectedTasks() {
        int[] selectedIndices = taskList.getSelectedRows();
        Task[] tasks = new Task[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            tasks[i] = (Task)taskList.getValueAt(selectedIndices[i], 0);
        }
        return tasks;
    }

}
