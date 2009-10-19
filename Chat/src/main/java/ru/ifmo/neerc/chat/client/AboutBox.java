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
// $Id: AboutBox.java,v 1.5 2007/10/28 07:32:12 matvey Exp $
/**
 * Date: 29.10.2004
 */
package ru.ifmo.neerc.chat.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Represents about box for Chat Client.
 * 
 * @author Matvey Kazakov
 */
public class AboutBox extends JDialog{

    /**
     * Constructs about box
     * @param owner wner window
     */
    public AboutBox(Frame owner) {
        super(owner, true);
        // change title of the about box
        setTitle("About NEERC chat");
        JPanel mainPanel = new JPanel(new BorderLayout());
        // read help from file and create label with the help

        JEditorPane html;
        try {
            html = new JEditorPane(getClass().getResource("res/help.html"));
            html.setEditable(false);
            mainPanel.add(new JScrollPane(html), BorderLayout.CENTER);
        } catch (IOException e) {
        }
        
        // create OK button
        JButton buttonOK = new JButton("OK");
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // OK button will just dispose about box
                dispose();
            }
        });
        // set border for about box
        mainPanel.setBorder(BorderFactory.createLineBorder(mainPanel.getBackground(), 10));
        setContentPane(mainPanel);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(owner);
    }

}
