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
import ru.ifmo.neerc.chat.client.Chat;
import ru.ifmo.neerc.chat.client.ChatMessage;
import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.MessageFactory;
import ru.ifmo.neerc.chat.message.ServerMessage;
import ru.ifmo.neerc.chat.message.UserMessage;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChatClient extends AbstractChatClient {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChatClient.class);

    private XmppChat xmppChat;

    public XmppChatClient() {
        final String name = System.getProperty("username");

        UserRegistry userRegistry = UserRegistry.getInstance();
        user = userRegistry.findOrRegister(name);
        userRegistry.putOnline(name);
        userRegistry.setRole(name, "moderator");

        chat = new MyChat();
        setupUI();

        MyListener listener = new MyListener();
        xmppChat = new XmppChat(name, listener);

        xmppChat.getConnection().addConnectionListener(listener);
        if (xmppChat.getMultiUserChat().isJoined()) {
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

    private String getNick(String participant) {
        return UserRegistry.getInstance().findOrRegister(participant).getName();
    }

    private class MyChat implements Chat {
        @Override
        public void write(Message message) {
            xmppChat.write(message);
        }
    }

    private class MyListener implements MUCListener, ConnectionListener {
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
                UserRegistry.getInstance().putOffline(user.getName());
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

        @Override
        public void joined(String participant) {
            UserRegistry.getInstance().putOnline(participant);
            processMessage(new ServerMessage(
                    "User " + getNick(participant) + " has joined chat"
            ));
        }

        @Override
        public void left(String participant) {
            UserRegistry.getInstance().putOffline(participant);
            processMessage(new ServerMessage(
                    "User " + getNick(participant) + " has left chat"
            ));
        }

        @Override
        public void roleChanged(String jid, String role) {
            UserRegistry.getInstance().setRole(jid, role);
            String nick = getNick(jid);
            processMessage(new ServerMessage(
                    "User " + nick + " now " + role
            ));
            if (nick.equals(user.getName())) {
                taskPanel.toolBar.setVisible("moderator".equals(role));
            }
        }

        @Override
        public void messageReceived(String jid, String message, Date timestamp) {
            processMessage(new UserMessage(jid, message, timestamp));
        }

        @Override
        public void historyMessageReceived(String jid, String message, Date timestamp) {
            addToModel(ChatMessage.createUserMessage(new UserMessage(jid, message, timestamp)));
//            processMessage(new UserMessage(jid, message, timestamp));
        }

        @Override
        public void taskReceived(byte[] bytes, Date timestamp) {
            bytes = Arrays.copyOf(bytes, bytes.length - 1); // TODO Godin: WTF?
            Message message = MessageFactory.getInstance().deserialize(bytes);
            LOG.debug("Found taskMessage: " + message.asString());
            message.setTimestamp(timestamp);
            processMessage(message);
        }
    }
}