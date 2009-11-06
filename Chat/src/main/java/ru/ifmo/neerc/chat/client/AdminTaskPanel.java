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

    private Frame owner;
    private AdminTaskList taskList;
    private JButton btnAssignTask;
    private JButton btnRemoveTask;

    public Component toolBar;

    public AdminTaskPanel(Frame owner, TaskRegistry taskRegistry) {
        super(new BorderLayout());
        this.owner = owner;
        taskList = new AdminTaskList(taskRegistry);

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

        add(new JScrollPane(taskList), BorderLayout.CENTER);
        toolBar = createToolBar();
        toolBar.setVisible(false);
        add(toolBar, BorderLayout.WEST);
        enableButtons();
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

        toolBar.add(createAddTaskButton(TaskActions.TYPE_TODO, "Add TODO"));
        toolBar.add(createAddTaskButton(TaskActions.TYPE_CONFIRM, "Add Confirmation"));
        toolBar.add(createAddTaskButton(TaskActions.TYPE_REASON, "Add Ok/Fail Reason"));
        toolBar.add(createAddTaskButton(TaskActions.TYPE_QUESTION, "Add Question"));

        btnAssignTask = createButton(TaskIcon.iconTaskAssign, "Assign Task");
        btnAssignTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Task[] tasks = getSelectedTasks();
                // TODO
//                new AssignTaskDialog(owner, clientReader, tasks).setVisible(true);
                enableButtons();
            }
        });
        toolBar.add(btnAssignTask);

        btnRemoveTask = createButton(TaskIcon.iconTaskRemove, "Delete task");
        btnRemoveTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Task[] tasks = getSelectedTasks();
                for (Task task : tasks) {
                    // TODO
//                    clientReader.write(new TaskMessage(TaskMessage.DELETE, -1, task, null));
                }
                enableButtons();
            }
        });
        toolBar.add(btnRemoveTask);

        // TODO?
//        JButton btnImportTasks = createButton(TaskIcon.iconTaskImport, "Import tasks");
//        btnImportTasks.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                new ImportTasksDialog(owner, clientReader).setVisible(true);
//            }
//        });
//        toolBar.add(btnImportTasks);

        return toolBar;
    }

    private JButton createAddTaskButton(final String type, final String message) {
        JButton btnCreateTask = createButton(
                TaskIcon.TYPE.get(type),
                message
        );
        btnCreateTask.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String taskDescription = JOptionPane.showInputDialog(AdminTaskPanel.this, message);
                if (taskDescription != null && taskDescription.trim().length() > 0) {
                    // TODO
//                    clientReader.write(new TaskMessage(TaskMessage.CREATE, -1, registry.createTask(taskDescription, type), null));
                }
                enableButtons();
            }
        });
        return btnCreateTask;
    }

    private JButton createButton(final ImageIcon icon, final String toolTipText) {
        JButton btnCreateTask = new JButton(icon);
        btnCreateTask.setBorderPainted(false);
        btnCreateTask.setToolTipText(toolTipText);
        btnCreateTask.setMargin(new Insets(0, 0, 0, 0));
        return btnCreateTask;
    }

    private Task[] getSelectedTasks() {
        int[] selectedIndices = taskList.getSelectedRows();
        Task[] tasks = new Task[selectedIndices.length];
        for (int i = 0; i < selectedIndices.length; i++) {
            tasks[i] = (Task) taskList.getValueAt(selectedIndices[i], 0);
        }
        return tasks;
    }

}
