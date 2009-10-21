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
// $Id: ChatClient.java,v 1.13 2007/10/28 07:32:12 matvey Exp $
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;
import ru.ifmo.neerc.chat.client.AbstractChatClient;
import ru.ifmo.neerc.chat.message.Message;

import javax.swing.*;
import java.awt.*;

/**
 * Fork of {@link ru.ifmo.neerc.chat.client.ChatClient}.
 *
 * @author Evgeny Mandrikov
 */
public class XmppChatClient extends AbstractChatClient {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChatClient.class);

    public XmppChatClient() {
        final String name = System.getProperty("username");

        user = new UserEntry(0, name, false); // TODO power
        UserRegistry.getInstance().register(user);
        UserRegistry.getInstance().putOnline(user, true);

        chat = new XmppChat(name, new ChatMessageListener());

        setupUI();

        ((XmppChat) chat).getConnection().addConnectionListener(new MyConnectionListener());
        if (((XmppChat) chat).getMultiUserChat().isJoined()) {
            setConnectionStatus("Connected");
        } else {
            setConnectionError("Unable to connect");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new XmppChatClient().setVisible(true);
            }
        });
    }

    private class ChatMessageListener extends XmppAdapter {
        @Override
        public void processMessage(Message message) {
            XmppChatClient.this.processMessage(message);
        }
    }

    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            setConnectionError("Connection closed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            setConnectionError("Connection closed on error");
        }

        @Override
        public void reconnectingIn(int i) {
            setConnectionError("Reconnecting in " + i);
        }

        @Override
        public void reconnectionSuccessful() {
            setConnectionStatus("Reconnected");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            setConnectionError("Reconnection failed");
        }
    }

    private void setConnectionStatus(String status) {
        LOG.info("Connection status: " + status);
        connectionStatus.setForeground(Color.BLUE);
        connectionStatus.setText(status);
        this.setEnabled(true);
    }

    private void setConnectionError(String error) {
        LOG.error("Connection status: " + error);
        connectionStatus.setForeground(Color.RED);
        connectionStatus.setText(error);
        this.setEnabled(false);
    }
}