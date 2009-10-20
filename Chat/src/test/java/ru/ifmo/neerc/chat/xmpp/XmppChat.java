package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.*;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.client.Chat;
import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.UserMessage;

import java.util.Random;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChat implements Chat {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChat.class);

    private static final Random random = new Random();

    private MultiUserChat muc;

    public XmppChat(String name, XmppAdapter adapter) {
        try {
            // Create the configuration for this new connection
            ConnectionConfiguration config = new ConnectionConfiguration("localhost", 5222); // TODO make this configurable
            config.setCompressionEnabled(false);
            config.setSASLAuthenticationEnabled(true);
            config.setReconnectionAllowed(true);
//        config.setDebuggerEnabled(true);

            SASLAuthentication.supportSASLMechanism("PLAIN", 0);

            XMPPConnection connection = new XMPPConnection(config);
            // Connect to the server
            connection.connect();

            // Log into the server
            // You have to specify your Jabber ID addres WITHOUT @jabber.org at the end
            // TODO make this configurable
            connection.login("tester", "12345", "SomeResource" + random.nextInt());
//            connection.login("admin", "12345", "SomeResource" + random.nextInt());

            LOG.debug("AUTHENTICATED: " + connection.isAuthenticated());

            // Create a MultiUserChat using an XMPPConnection for a room
            // TODO make this configurable
            muc = new MultiUserChat(connection, "neerc@conference.localhost");

            // Joins the new room
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(5);
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
            } else {
                throw new UnsupportedOperationException(message.getClass().getSimpleName());
            }
        } catch (XMPPException e) {
            e.printStackTrace(); // TODO
        }
    }
}
