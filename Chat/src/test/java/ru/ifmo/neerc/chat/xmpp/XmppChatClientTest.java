package ru.ifmo.neerc.chat.xmpp;

import javax.swing.*;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChatClientTest {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new XmppChatClient().setVisible(true);
            }
        });
    }

}
