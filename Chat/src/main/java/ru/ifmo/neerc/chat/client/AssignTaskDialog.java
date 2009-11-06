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
 * Date: 28.10.2004
 */
package ru.ifmo.neerc.chat.client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;

/**
 * @author Matvey Kazakov
 */
public class AssignTaskDialog extends JDialog {
    private Chat chat;
    private Task[] tasks;
    private JTree tree;
    private String[] allGroups;
    private Map<String, UserEntry[]> users = new HashMap<String, UserEntry[]>();

    public AssignTaskDialog(Frame owner, Chat chat, Task[] tasks) throws HeadlessException {
        super(owner, true);
        setTitle("Choose user to perform task");
        this.chat = chat;
        this.tasks = tasks;
        initUsersList();
        tree = createTree();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(new JScrollPane(tree), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.add(Box.createHorizontalGlue());
        JButton okButton = new JButton("OK");
        btnPanel.add(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                apply();
            }
        });
        btnPanel.add(Box.createHorizontalStrut(10));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        btnPanel.add(cancelButton);
        btnPanel.add(Box.createHorizontalGlue());
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(owner);
    }

    private JTree createTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (String group : allGroups) {
            UserEntry[] curUsers = users.get(group);
            DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
            for (UserEntry user : curUsers) {
                groupNode.add(new DefaultMutableTreeNode(user, false));
            }
            root.add(groupNode);
        }
        JTree tree = new JTree(new DefaultTreeModel(root));

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setRootVisible(false);

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                if (leaf) {
                    UserEntry entry = (UserEntry) ((DefaultMutableTreeNode) value).getUserObject();
                    this.hasFocus = hasFocus;
                    setText(entry.getName());
                    setForeground(sel ? this.getTextSelectionColor() : this.getTextNonSelectionColor());
                    setEnabled(true);
                    setIcon(entry.isPower() ?
                            (entry.isOnline() ? UsersPanel.iconUserPower : UsersPanel.iconUserPowerOffline) :
                            (entry.isOnline() ? UsersPanel.iconUserNormal : UsersPanel.iconUserNormalOffline)
                    );
                    setComponentOrientation(tree.getComponentOrientation());
                    this.selected = sel;
                    return this;
                } else {
                    return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                }
            }
        };
        renderer.setLeafIcon(UsersPanel.iconUserNormal);
        tree.setCellRenderer(renderer);

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        return tree;
    }

    private void apply() {
        TreePath[] treePaths = tree.getSelectionModel().getSelectionPaths();
        List<UserEntry> selectedUsers = new ArrayList<UserEntry>();
        if (treePaths != null) {
            for (TreePath treePath : treePaths) {
                Object node = ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject();
                if (node instanceof UserEntry) {
                    selectedUsers.add((UserEntry) node);
                } else if (node instanceof String) {
                    selectedUsers.addAll(Arrays.asList(users.get((String) node)));
                }
            }
        }
        for (Task task : tasks) {
            Task updatedTask = new Task(task.getId(), task.getType(), task.getTitle());
            Map<String, TaskStatus> statuses = task.getStatuses();
            for (UserEntry user : selectedUsers) {
                updatedTask.setStatus(user.getName(), "none", "");
            }
            for (String user : statuses.keySet()) {
                TaskStatus status = statuses.get(user);
                updatedTask.setStatus(user, status.getType(), status.getValue());
            }
            chat.write(updatedTask);
        }
        dispose();
    }

    private void initUsersList() {
        UserEntry[] userEntries = UserRegistry.getInstance().serialize();
        Set<String> groups = new HashSet<String>();
        Map<String, List<UserEntry>> usersByGroup = new HashMap<String, List<UserEntry>>();
        for (UserEntry entry : userEntries) {
            String group = entry.getGroup();
            List<UserEntry> curUsers = usersByGroup.get(group);
            if (curUsers == null) {
                curUsers = new ArrayList<UserEntry>();
                usersByGroup.put(group, curUsers);
            }
            curUsers.add(entry);
        }
        for (Map.Entry<String, List<UserEntry>> entry : usersByGroup.entrySet()) {
            String group = entry.getKey();
            List<UserEntry> curUsers = entry.getValue();
            UserEntry[] curUsersArray = curUsers.toArray(new UserEntry[curUsers.size()]);
            Arrays.sort(curUsersArray);
            users.put(group, curUsersArray);
            groups.add(group);
        }
        allGroups = groups.toArray(new String[groups.size()]);
        Arrays.sort(allGroups);
    }

}
