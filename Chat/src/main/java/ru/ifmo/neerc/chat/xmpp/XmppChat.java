package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smackx.muc.*;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;
import ru.ifmo.neerc.chat.client.Chat;
import ru.ifmo.neerc.chat.message.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

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

    ru.ifmo.neerc.chat.MessageListener messageListener;

    public XmppChat(String name, ru.ifmo.neerc.chat.MessageListener messageListener) {
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
        registerRoomListeners(muc);

        registerConnectionListeners(connection);

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
                history.setSince(lastActivity);
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

    public UserEntry getUser(String user, String role) {
        final UserRegistry userRegistry = UserRegistry.getInstance();
        final String nick = user.substring(user.indexOf('/') + 1);
        UserEntry userEntry = userRegistry.findByName(nick);
        final boolean power = "moderator".equalsIgnoreCase(role) || "owner".equalsIgnoreCase(role);
        if (userEntry == null) {
            final int id = userRegistry.getUserNumber() + 1;
            LOG.debug("Added {} {} with id {}", new Object[]{role, user, id});
            userEntry = new UserEntry(
                    id,
                    nick,
                    power
            );
        } else {
            userEntry.setPower(power);
        }
        userRegistry.register(userEntry);
        return userEntry;
    }

    private UserEntry getUser(String user) {
        final Occupant occupant = muc.getOccupant(user);
        final String role = occupant == null ? "member" : occupant.getRole();
        return getUser(user, role);
    }

    public void registerConnectionListeners(XMPPConnection conn) {
        conn.addPacketListener(new MyPresenceListener(), new PacketTypeFilter(Presence.class));
    }

    public void registerRoomListeners(MultiUserChat chat) {
        chat.addMessageListener(new MyPacketListener());

        // chat.addParticipantStatusListener(new MyParticipantStatusListener());
        // chat.addUserStatusListener(new MyUserStatusListener());

        LOG.debug("Occupants count = " + chat.getOccupantsCount());
        Iterator<String> occupants = chat.getOccupants();
        while (occupants.hasNext()) {
            String user = occupants.next();
            Occupant occupant = chat.getOccupant(user);
            LOG.debug("JID={} Nick={} Role={} Affiliation={}", new Object[]{
                    occupant.getJid(),
                    occupant.getNick(),
                    occupant.getRole(),
                    occupant.getAffiliation()
            });
            UserRegistry.getInstance().putOnline(getUser(user, occupant.getRole()), true);
        }
    }

    public void processMessage(Message message) {
        messageListener.processMessage(message);
    }

    private class MyPresenceListener implements PacketListener {
        public void processPacket(Packet packet) {
	        if (!(packet instanceof Presence)) {
    	    	return;
	        }
	        Presence presence = (Presence)packet;

            MUCUser mucExtension = (MUCUser) packet.getExtension("x", "http://jabber.org/protocol/muc#user");
            if (mucExtension != null)
            {
	            String newAffiliation = mucExtension.getItem().getAffiliation();
    	        String newRole = mucExtension.getItem().getRole();
        	    // TODO zibada: use this to update power status
        	}

            String from = packet.getFrom();
            boolean avail = presence.isAvailable();
            if (avail) {
                LOG.debug("JOINED: {}", from);
                processMessage(new ServerMessage(ServerMessage.USER_JOINED, getUser(from)));
            }
            else {
                LOG.debug("LEFT: {}", from);
                processMessage(new ServerMessage(ServerMessage.USER_LEFT, getUser(from)));
            }
        }
    }

    private class MyPacketListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            if (!(packet instanceof org.jivesoftware.smack.packet.Message)) {
                // TODO Godin: maybe throw exception?
                return;
            }

            org.jivesoftware.smack.packet.Message xmppMessage = (org.jivesoftware.smack.packet.Message) packet;

            UserEntry user = getUser(xmppMessage.getFrom());
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
                        new UserText(xmppMessage.getBody())
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

    private class MyUserStatusListener extends DefaultUserStatusListener {
        @Override
        public void moderatorGranted() {
            LOG.debug("Moderator granted");
        }

        @Override
        public void moderatorRevoked() {
            LOG.debug("Moderator revoked");
        }
    }

    private class MyParticipantStatusListener extends DefaultParticipantStatusListener {
        @Override
        public void joined(String participant) {
            LOG.debug("JOINED: {}", participant);
            processMessage(new ServerMessage(ServerMessage.USER_JOINED, getUser(participant)));
        }

        @Override
        public void left(String participant) {
            LOG.debug("LEFT: {}", participant);
            processMessage(new ServerMessage(ServerMessage.USER_LEFT, getUser(participant)));
        }
    }
}
