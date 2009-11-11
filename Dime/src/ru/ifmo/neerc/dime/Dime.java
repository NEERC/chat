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

package ru.ifmo.neerc.dime;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.io.*;

public class Dime implements Runnable {
    public List<String> getText(String fileName) {
        LinkedList<String> list = new LinkedList<String>();
        try {
            Scanner in = new Scanner(new File(fileName));
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.trim().length() == 0) {
                    list.add(null);
                } else {
                    list.add(line);
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    public List<String> messages = getText("text.txt");
    private JLabel label;

    public void run() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        JFrame window = new JFrame();
        DefaultListModel model = new DefaultListModel();
        window.setUndecorated(true);

        label = new JLabel(){
//            public void paintComponent(Graphics g) {
//                if (g instanceof Graphics2D) {
//                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                }
//                super.paintComponent(g);
//            }
        };
        label.setBackground(Color.WHITE);
        label.setOpaque(true);
        label.setFont(new Font("Lucida Console", Font.BOLD, 40));
        System.out.println("Hello1");
        label.setVerticalAlignment(JLabel.TOP);
        //label.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        clear();
        device.setFullScreenWindow(window);

        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kfm.addKeyEventPostProcessor(new KeyEventPostProcessor() {
            int currentMessage = 0;

            public boolean postProcessKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode()  == KeyEvent.VK_SPACE && currentMessage < messages.size()) {
                    write(messages.get(currentMessage));
                    currentMessage++;
                }
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode()  == KeyEvent.VK_BACK_SPACE && currentMessage > 0) {
                    currentMessage--;
                    clear();
                    for (int i = Math.max(0, currentMessage - 20); i < currentMessage; i++) {
                        write(messages.get(i));
                    }
                }
                if (e.getKeyCode()  == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                return true;
            }
        });

        System.out.println(device.getFullScreenWindow());

        window.getContentPane().add(label);

    }

    void write(String message) {
        if (message != null) {
            writeMessage(message);
        } else {
            clear();
        }
    }

    public void clear() {
        this.text = "";
        append("");

    }

    void writeMessage(String message) {
        append(message);
    }

    String text = "";
    void append(String text) {
        this.text = text + this.text;
        label.setText("<html><pre style='background: blue; color: white; margin:0 0 0 0; font-weight: bold; padding-top: 10px'> Team                         =  Time Rank     </pre>" + this.text);
//        label.setText("<html><pre style='background: blue; color: white; margin:0 0 0 0; font-weight: bold; padding-top: 10px'> Команда                           = Время Место   </pre>" + this.text);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Dime());
    }
}
