package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.MessageListener;
import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;
import ru.ifmo.neerc.chat.message.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

/**
 * Adapts XMPP protocol to neerc-chat protocol, which based on {@link ru.ifmo.neerc.chat.message.Message}.
 *
 * @author Evgeny Mandrikov
 */
public abstract class XmppAdapter implements PacketListener, MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(XmppAdapter.class);

    private MultiUserChat chat;

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
        final Occupant occupant = chat.getOccupant(user);
        final String role = occupant == null ? "member" : occupant.getRole();
        return getUser(user, role);
    }

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
        message.setTimestamp(timestamp);
        if (timestamp != null) {
            lastActivity = timestamp;
        }
        processMessage(message);
    }

    public void registerListeners(MultiUserChat chat) {
        this.chat = chat;
        chat.addMessageListener(this);
        chat.addParticipantStatusListener(new MyParticipantStatusListener());
        chat.addUserStatusListener(new MyUserStatusListener());

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

    public Date getLastActivity() {
        return lastActivity;
    }
}
