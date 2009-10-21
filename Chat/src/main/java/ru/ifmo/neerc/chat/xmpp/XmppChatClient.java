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
package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.client.AbstractChatClient;
import ru.ifmo.neerc.chat.message.ServerMessage;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;

import javax.swing.*;
import java.awt.*;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChatClient extends AbstractChatClient {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChatClient.class);

    public XmppChatClient() {
        final String name = System.getProperty("username");

        user = new UserEntry(0, name, true); // TODO power
        UserRegistry.getInstance().register(user);
        UserRegistry.getInstance().putOnline(user);

        chat = new XmppChat(name, this);

        setupUI();

        ((XmppChat) chat).getConnection().addConnectionListener(new MyConnectionListener());
        if (((XmppChat) chat).getMultiUserChat().isJoined()) {
            final String message = "Connected";
            setConnectionStatus(message);
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

    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            final String message = "Connection closed";
            setConnectionError(message);
            processMessage(new ServerMessage(message));
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            final String message = "Connection closed on error";
            setConnectionError(message);
            processMessage(new ServerMessage(message));
            for (UserEntry user : UserRegistry.getInstance().getUsers()) {
                UserRegistry.getInstance().putOffline(user);
            }
        }

        @Override
        public void reconnectingIn(int i) {
            setConnectionError("Reconnecting in " + i);
        }

        @Override
        public void reconnectionSuccessful() {
            final String message = "Reconnected";
            setConnectionStatus(message);
            processMessage(new ServerMessage(message));
        }

        @Override
        public void reconnectionFailed(Exception e) {
            setConnectionError("Reconnection failed");
        }
    }

    private void setConnectionStatus(String status, boolean isError) {
        if (status.equals(connectionStatus.getText())) {
            return;
        }
        if (!isError) {
            LOG.info("Connection status: " + status);
            connectionStatus.setForeground(Color.BLUE);
        } else {
            LOG.error("Connection status: " + status);
            connectionStatus.setForeground(Color.RED);
        }
        connectionStatus.setText(status);
    }

    private void setConnectionStatus(String status) {
        setConnectionStatus(status, false);
    }

    private void setConnectionError(String error) {
        setConnectionStatus(error, true);
    }

}