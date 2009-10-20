package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.client.Chat;
import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.MessageFactory;
import ru.ifmo.neerc.chat.message.TaskMessage;
import ru.ifmo.neerc.chat.message.UserMessage;

import java.util.Random;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChat implements Chat {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChat.class);

    private static final Random random = new Random(System.currentTimeMillis());

    private MultiUserChat muc;

    private XmppAdapter adapter;

    public XmppChat(String name, XmppAdapter adapter) {
        this.adapter = adapter;
        try {
            // Create the configuration for this new connection
            ConnectionConfiguration config = new ConnectionConfiguration("localhost", 5222); // TODO make this configurable
            config.setCompressionEnabled(false);
            config.setSASLAuthenticationEnabled(true);
            config.setReconnectionAllowed(true);
//            config.setDebuggerEnabled(true);

            SASLAuthentication.supportSASLMechanism("PLAIN", 0);

            XMPPConnection connection = new XMPPConnection(config);
            // Connect to the server
            connection.connect();
            connection.addConnectionListener(new MyConnectionListener());

            // Log into the server
            // You have to specify your Jabber ID addres WITHOUT @jabber.org at the end
            // TODO make this configurable
            connection.login(name, "12345", "SomeResource" + random.nextInt());

            LOG.debug("AUTHENTICATED: " + connection.isAuthenticated());

            // Create a MultiUserChat using an XMPPConnection for a room
            // TODO make this configurable
            muc = new MultiUserChat(connection, "neerc@conference.localhost");

            // Joins the new room
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(100); // TODO set since
            muc.join(
                    name, // nick
                    "",   // password
                    history,
                    SmackConfiguration.getPacketReplyTimeout()
            );

            LOG.debug("JOINED: " + muc.isJoined());

            adapter.registerListeners(muc);
        } catch (XMPPException e) {
            e.printStackTrace(); // TODO
        }
    }

    @Override
    public void write(Message message) {
        try {
            if (message instanceof UserMessage) {
                UserMessage userMessage = (UserMessage) message;
                muc.sendMessage(userMessage.getText().getText());
            } else if (message instanceof TaskMessage) {
                TaskMessage taskMessage = (TaskMessage) message;
                byte[] bytes = MessageFactory.getInstance().serialize(taskMessage);
                org.jivesoftware.smack.packet.Message msg = muc.createMessage();
                msg.setProperty("taskMessage", bytes);
                msg.setBody("Task");
                muc.sendMessage(msg);

                adapter.processMessage(message);
            } else {
                throw new UnsupportedOperationException(message.getClass().getSimpleName());
            }
        } catch (XMPPException e) {
            e.printStackTrace(); // TODO
        }
    }

    private class MyConnectionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            LOG.debug("Connection closed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            LOG.error("Connection closed on error", e);
        }

        @Override
        public void reconnectingIn(int i) {
            LOG.debug("Reconnecting in " + i);
        }

        @Override
        public void reconnectionSuccessful() {
            LOG.debug("Reconnected");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            LOG.error("Reconnection failed", e);
        }
    }
}
