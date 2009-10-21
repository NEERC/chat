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
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.chat.user.UserRegistryListener;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author Matvey Kazakov
 */
public class UsersPanel extends JPanel {
    private static final ImageIcon iconUserNormal = new ImageIcon(
            UsersPanel.class.getResource("res/user_normal.gif"));
    private static final ImageIcon iconUserPower = new ImageIcon(
            UsersPanel.class.getResource("res/user_power.gif"));

    private UserEntry user;

    public UsersPanel(UserEntry user) {
        this.user = user;
        setLayout(new BorderLayout());
        ListData model = new ListData();
        JList userList = new JList();
        userList.setModel(model);
        userList.setCellRenderer(new UserListCellRenderer());
        UserRegistry.getInstance().addListener(model);
        add(new JScrollPane(userList), BorderLayout.CENTER);
    }

    private class ListData extends AbstractListModel implements UserRegistryListener {

        private UserEntry[] userEntries;

        public ListData() {
            init();
        }

        private void init() {
            userEntries = UserRegistry.getInstance().serialize();
            Arrays.sort(userEntries);
        }

        public synchronized int getSize() {
            return userEntries.length;
        }

        public synchronized Object getElementAt(int index) {
            return userEntries[index];
        }

        public void userAdded(UserEntry userEntry) {
            update();
        }

        public void userChanged(UserEntry userEntry) {
            update();
        }

        public void userRemoved(UserEntry userEntry) {
            update();
        }

        private void update() {
            init();
            fireContentsChanged(this, 0, userEntries.length);
        }
    }

    private class UserListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            UserEntry entry = (UserEntry) value;
            Color foreground;
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                foreground = list.getSelectionForeground();
            } else {
                setBackground(list.getBackground());
                foreground = list.getForeground();
            }

            if (!entry.isOnline()) {
                foreground = Color.lightGray;
            }
            setForeground(foreground);

            if (entry.isPower()) {
                setIcon(iconUserPower);
            } else {
                setIcon(iconUserNormal);
            }

            setEnabled(list.isEnabled());
            Font font = list.getFont();
            if (entry.getId() == user.getId()) {
                font = font.deriveFont(Font.BOLD);
            } else {
                font = font.deriveFont(Font.PLAIN);
            }
            setFont(font);
            setText(entry.getName());
            setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

            return this;
        }
    }

}
