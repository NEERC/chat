package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.jivesoftware.smackx.packet.MUCUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.client.Chat;
import ru.ifmo.neerc.chat.message.*;
import ru.ifmo.neerc.chat.message.MessageListener;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.chat.utils.DebugUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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

    private String name;
    private String password = System.getProperty("password", "12345");

    MessageListener messageListener;

    public XmppChat(String name, MessageListener messageListener) {
        this.messageListener = messageListener;
        this.name = name;
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
        muc.addMessageListener(new MyMessageListener());

        connection.addPacketListener(new MyPresenceListener(), new PacketTypeFilter(Presence.class));

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
            if (lastActivity != null) {
                history.setSince(new Date(lastActivity.getTime() + 1));
            } else {
                if (System.getProperty("history") != null) {
                    int size = Integer.parseInt(System.getProperty("history"));
                    history.setMaxStanzas(size);
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    history.setSince(calendar.getTime());
                }
            }
            muc.join(
                    name, // nick
                    "",   // password
                    history,
                    SmackConfiguration.getPacketReplyTimeout()
            );
        } catch (XMPPException e) {
            LOG.error("Unable to join room", e);
        }
    }

    public void debugConnection() {
        LOG.debug("User: {}", connection.getUser());
        LOG.debug("Connected: {}", connection.isConnected());
        LOG.debug("Authenticated: {}", connection.isAuthenticated());
        LOG.debug("Joined: {}", muc.isJoined());
    }

    @Override
    public void write(Message message) {
        try {
            if (message instanceof UserMessage) {
                UserMessage userMessage = (UserMessage) message;
                muc.sendMessage(userMessage.getText());
            } else if (message instanceof TaskMessage) {
                TaskMessage taskMessage = (TaskMessage) message;
                byte[] bytes = MessageFactory.getInstance().serialize(taskMessage);
                org.jivesoftware.smack.packet.Message msg = muc.createMessage();
                msg.setProperty("taskMessage", bytes);
                msg.setBody("Task");
                muc.sendMessage(msg);

                processMessage(message);
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

    private Date lastActivity = null;

    public void processMessage(Message message) {
        messageListener.processMessage(message);
    }

    private class MyPresenceListener implements PacketListener {
        public void processPacket(Packet packet) {
            if (!(packet instanceof Presence)) {
                return;
            }
            Presence presence = (Presence) packet;
            // Filter presence by room name
            final String from = presence.getFrom();
            if (!from.startsWith(ROOM)) {
                return;
            }
            final UserEntry user = UserRegistry.getInstance().findOrRegister(from);
            final MUCUser mucExtension = (MUCUser) packet.getExtension("x", "http://jabber.org/protocol/muc#user");
            if (mucExtension != null) {
                MUCUser.Item item = mucExtension.getItem();
                LOG.debug(from + " " + DebugUtils.userItemToString(item));
                UserRegistry.getInstance().setRole(from, item.getRole());
            }
            if (presence.isAvailable()) {
                UserRegistry.getInstance().putOnline(from);
                processMessage(new ServerMessage(
                        "User " + user.getName() + " has joined chat"
                ));
            } else {
                UserRegistry.getInstance().putOffline(from);
                processMessage(new ServerMessage(
                        "User " + user.getName() + " has left chat"
                ));
            }
        }
    }

    private class MyMessageListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            if (!(packet instanceof org.jivesoftware.smack.packet.Message)) {
                return;
            }

            org.jivesoftware.smack.packet.Message xmppMessage = (org.jivesoftware.smack.packet.Message) packet;

            UserEntry user = UserRegistry.getInstance().findOrRegister(xmppMessage.getFrom());
            Date timestamp = null;

            for (PacketExtension extension : xmppMessage.getExtensions()) {
                if ("jabber:x:delay".equals(extension.getNamespace())) {
                    DelayInformation delayInformation = (DelayInformation) extension;
                    timestamp = delayInformation.getStamp();
                } else {
                    LOG.debug("Found unknown packet extenstion {} with namespace {}",
                            extension.getClass().getSimpleName(),
                            extension.getNamespace()
                    );
                }
            }

            Message message;

            Object taskMessageProperty = xmppMessage.getProperty("taskMessage");
            if (taskMessageProperty != null) {
                byte[] bytes = (byte[]) taskMessageProperty;
                bytes = Arrays.copyOf(bytes, bytes.length - 1); // TODO Godin: WTF?
                message = MessageFactory.getInstance().deserialize(bytes);
                LOG.debug("Found taskMessage: " + message.asString());
            } else {
                message = new UserMessage(
                        user.getId(),
                        xmppMessage.getBody()
                );
            }

            if (timestamp == null) {
                timestamp = new Date();
            }

            message.setTimestamp(timestamp);
            lastActivity = timestamp;

            processMessage(message);
        }
    }
}
