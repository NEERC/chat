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
        char c = e.getKeyChar();
        if (c == '\n') {
            dispose();
        } 
    } // action
}
