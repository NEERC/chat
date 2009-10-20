package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.DefaultParticipantStatusListener;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.*;
import ru.ifmo.neerc.chat.message.ServerMessage;
import ru.ifmo.neerc.chat.message.TaskMessage;
import ru.ifmo.neerc.chat.message.UserMessage;
import ru.ifmo.neerc.chat.message.UserText;

import java.util.Iterator;

/**
 * Adapts XMPP protocol to neerc-chat protocol, which based on {@link ru.ifmo.neerc.chat.message.Message}.
 *
 * @author Evgeny Mandrikov
 */
public abstract class XmppAdapter implements PacketListener, MessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(XmppAdapter.class);

    private MultiUserChat chat;

    private UserEntry getUser(String user, String role) {
        final UserRegistry userRegistry = UserRegistry.getInstance();
        final String nick = user.substring(user.indexOf('/') + 1);
        UserEntry userEntry = userRegistry.findByName(nick);
        final boolean power = "moderator".equalsIgnoreCase(role);
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
        // TODO unchecked cast
        org.jivesoftware.smack.packet.Message xmppMessage = (org.jivesoftware.smack.packet.Message) packet;

        Form form = Form.getFormFrom(packet);
        if (form != null) {
            if (form.getField("user") != null) {
                String user = form.getField("user").getValues().next();
                LOG.debug("Form for user " + user);
            }

            LOG.debug("Form found");
            Task task = new Task(1, "test", TaskFactory.TASK_TODO);
            TaskMessage taskMessage = new TaskMessage(
                    TaskMessage.ASSIGN,
                    UserRegistry.getInstance().findByName("tester").getId(),
                    task,
                    new TodoTaskResult()
            );
            TaskRegistry.getInstance().registerTask(task);

            processMessage(taskMessage);
            return;
        }

        for (PacketExtension extension : xmppMessage.getExtensions()) {
            LOG.debug("Found packet extenstion {} with namespace {}",
                    extension.getClass().getSimpleName(),
                    extension.getNamespace()
            );
        }

        DelayInformation delayInformation = (DelayInformation) xmppMessage.getExtension("jabber:x:delay");
        if (delayInformation != null) {
            LOG.debug(delayInformation.getStamp().toString());
        }

        UserEntry user = getUser(xmppMessage.getFrom());
        UserMessage message = new UserMessage(
                user.getId(),
                new UserText(xmppMessage.getBody())
        );
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
}
