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
package ru.ifmo.neerc.chat.client;
// $Id$
/**
 * Date: 28.10.2005
 */

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ifmo.neerc.chat.user.UserEntry;

/**
 * @author Matvey Kazakov
 */
public class NewChatMessageRenderer extends JTextArea implements TableCellRenderer {
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();

    private int fontStyle = -1;
    private NameColorizer nameColorizer;
    private UserEntry currentUser;

    public NewChatMessageRenderer() {
        this(-1);
    }

    public NewChatMessageRenderer(UserEntry user) {
        this(-1);
        currentUser = user;
    }

    public NewChatMessageRenderer(int fontStyle) {
        this.fontStyle = fontStyle;
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public NewChatMessageRenderer(int fontStyle, NameColorizer colorizer, UserEntry user) {
        this(fontStyle);
        nameColorizer = colorizer;
        currentUser = user;
    }

    public Component getTableCellRendererComponent(//
                                                   JTable table, Object obj, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        // set the colours, etc. using the standard for that platform
        adaptee.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
        setForeground(adaptee.getForeground());
        setBackground(adaptee.getBackground());
        setBorder(adaptee.getBorder());
        if (fontStyle != -1) {
            setFont(adaptee.getFont().deriveFont(fontStyle));
        } else {
            setFont(adaptee.getFont());
        }
        if (obj instanceof Message && column == 2) {
            Message message = (Message) obj;
            setForeground(message.getColor());
            setFont(adaptee.getFont()
                .deriveFont(adaptee.getFont().getSize() * message.getScale())
                .deriveFont(message.getStyle())
            );
            setText(message.getText());
        } else if (obj instanceof UserEntry && column == 1) {
            UserEntry user = (UserEntry) obj;
            setText(user.getName());
            setForeground(generateColor(user));
        } else if (obj instanceof Date && column == 0) {
            setText(DATE_FORMAT.format((Date) obj));
        } else {
            setText(adaptee.getText());
        }

        TableColumnModel columnModel = table.getColumnModel();
        setSize(columnModel.getColumn(column).getWidth(), Integer.MAX_VALUE);

        return this;
    }

    public Color generateColor(UserEntry name) {
        if (nameColorizer == null) return Color.BLACK;
        return nameColorizer.generateColor(name);
    }

}
