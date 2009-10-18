// $Id$
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.client;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Matvey Kazakov
 */
class UserInfoFrame extends Frame {

    public TextField UserNameField;

    public Applet parent;

    public UserInfoFrame(Applet parent) {
        UserNameField = new TextField(10);
        this.parent = parent;

        add("North", new Label("Please enter your name and hit ENTER"));
        add("South", UserNameField);
        setSize(300, 100);
        setVisible(true);
    }

    protected void processKeyEvent(KeyEvent e) {
        char c = (char)e.getKeyChar();
        if (c == '\n') {
            dispose();
        } 
    } // action
}


