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
// $Id: ChatMessageRenderer.java,v 1.1 2005/11/27 08:55:09 matvey Exp $
/**
 * Date: 25.10.2005
 */
package ru.ifmo.neerc.chat.client;

import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * @author Matvey Kazakov
 */
class ChatMessageRenderer implements TableCellRenderer {
    private JLabel infoMessageLabel = new JLabel();
    private JPanel userMessagePanel = new JPanel();
    private JLabel userLabel = new JLabel();
    private JLabel timeLabel = new JLabel();
    private JLabel messageArea = new JLabel();
    
    public ChatMessageRenderer() {
        userMessagePanel.setLayout(new GridBagLayout());
        userLabel.setOpaque(false);
        timeLabel.setOpaque(false);
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN));
        userMessagePanel.add(timeLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        userMessagePanel.add(userLabel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
        messageArea.setOpaque(false);
//            messageArea.setLineWrap(true);
//            messageArea.setWrapStyleWord(true);
        messageArea.setFont(messageArea.getFont().deriveFont(Font.PLAIN));
        userMessagePanel.add(messageArea, new GridBagConstraints(2, 0, 1, 1, 1, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        userMessagePanel.add(Box.createHorizontalStrut(50), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
        userMessagePanel.add(Box.createHorizontalStrut(50), new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ChatMessage message = (ChatMessage)value;
        JComponent component;
        if (message != null && message.getType() == ChatMessage.USER_MESSAGE) {
            component = userMessagePanel;
            userLabel.setText(message.getUser().getName());
            timeLabel.setText(message.getTime());
            messageArea.setText(message.getConvertedMessage());
//            int newWidth = table.getWidth();
//            userMessagePanel.setMaximumSize(table.getSize());
            userMessagePanel.setSize(table.getColumn(column).getWidth(), 100000);
            int newHeight = userMessagePanel.getPreferredSize().height;
            int oldHeight = table.getRowHeight(row);
            if (newHeight != oldHeight) {
                table.setRowHeight(newHeight);
            }
        } else {
            component = infoMessageLabel;
            assert message != null; // todo: is this correct?
            infoMessageLabel.setText(message.getText());
            if (message.getType() == ChatMessage.TASK_MESSAGE) {
                infoMessageLabel.setForeground(Color.red);
            } else {
                infoMessageLabel.setForeground(Color.blue);
            }
        }
        if (isSelected) {
            component.setBackground(table.getSelectionBackground());
        } else {
            component.setBackground(table.getBackground());
        }
        
        return component;
    }

}
