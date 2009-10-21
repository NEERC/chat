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

/**
 * @author Evgeny Mandrikov
 */
public class XmppChat implements Chat {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChat.class);

    private static final String SERVER_HOST = System.getProperty("server.host", "localhost");
    private static final int SERVER_PORT = Integer.parseInt(System.getProperty("server.port", "5222"));
    private static final String ROOM = "neerc@conference.localhost";
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("smack.debug", "false"));

    private MultiUserChat muc;
    private XMPPConnection connection;

    private XmppAdapter adapter;

    private String name;
    private String password = System.getProperty("password", "12345");

    public XmppChat(String name, XmppAdapter adapter) {
        this.name = name;
        this.adapter = adapter;
        // Create the configuration for this new connection
        ConnectionConfiguration config = new ConnectionConfiguration(SERVER_HOST, SERVER_PORT);
        config.setCompressionEnabled(true);
        config.setSASLAuthenticationEnabled(true);
        config.setReconnectionAllowed(true);
        config.setDebuggerEnabled(DEBUG);

        SASLAuthentication.supportSASLMechanism("PLAIN", 0);

        connection = new XMPPConnection(config);
        // Connect to the server
        try {
            connection.connect();
        } catch (XMPPException e) {
            LOG.error("Unable to connect", e);
            throw new RuntimeException(e);
        }

        authenticate();

        // Create a MultiUserChat using an XMPPConnection for a room
        muc = new MultiUserChat(connection, ROOM);
        adapter.registerListeners(muc);

        join();

        debugConnection();

        connection.addConnectionListener(new DefaultConnectionListener() {
            @Override
            public void reconnectionSuccessful() {
                authenticate();
                join();
                debugConnection();
            }
        });
    }

    private void authenticate() {
        // Log into the server
        // You have to specify your Jabber ID addres WITHOUT @jabber.org at the end
        try {
            connection.login(name, password, connection.getHost());
        } catch (XMPPException e) {
            LOG.error("Unable to authenticate", e);
        }
    }

    private void join() {
        try {
            // Joins the new room and retrieves history
            DiscussionHistory history = new DiscussionHistory();
            if (adapter.getLastActivity() != null) {
                history.setSince(adapter.getLastActivity());
            } else {
                history.setMaxStanzas(100); // TODO
            }
            muc.join(
                    name, // nick
                    "",   // password
                    history,
                    SmackConfiguration.getPacketReplyTimeout()
            );

            LOG.debug("JOINED: {}", muc.isJoined());

            /*
            try {
                for (Affiliate affiliate : muc.getOwners()) {
                    String jid = affiliate.getJid();
                    final String nick = jid.substring(0, jid.indexOf('@'));
                    final String affiliation = affiliate.getAffiliation();
                    LOG.debug("Nick: {} Affiliation: {}", nick, affiliation);
                    adapter.getUser(nick, affiliation);
                }

                for (Affiliate affiliate : muc.getAdmins()) {
                    String jid = affiliate.getJid();
                    final String nick = jid.substring(0, jid.indexOf('@'));
                    final String affiliation = affiliate.getAffiliation();
                    LOG.debug("Nick: {} Affiliation: {}", nick, affiliation);
                }
                for (Affiliate affiliate : muc.getMembers()) {
                    String jid = affiliate.getJid();
                    final String nick = jid.substring(0, jid.indexOf('@'));
                    final String affiliation = affiliate.getAffiliation();
                    LOG.debug("Nick: {} Affiliation: {}", nick, affiliation);
                    adapter.getUser(nick, affiliation);
                }
            } catch (XMPPException e) {
                LOG.error("Unable to retrieve room users", e);
            }
            */
        } catch (XMPPException e) {
            LOG.error("Unable to join room", e);
        }
    }

    public void debugConnection() {
        LOG.debug("User: {}", connection.getUser());
        LOG.debug("Connected: {}", getConnection().isConnected());
        LOG.debug("Authenticated: {}", getConnection().isAuthenticated());
        LOG.debug("Joined: {}", getMultiUserChat().isJoined());
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
            LOG.error("Unable to write message", e);
        }
    }

    public MultiUserChat getMultiUserChat() {
        return muc;
    }

    public XMPPConnection getConnection() {
        return connection;
    }
}
