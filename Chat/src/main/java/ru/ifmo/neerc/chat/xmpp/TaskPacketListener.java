package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import ru.ifmo.neerc.chat.xmpp.provider.NeercPacketExtension;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class TaskPacketListener implements PacketListener {
    @Override
    public void processPacket(Packet packet) {
        Message message = (Message) packet;
        NeercPacketExtension extension = (NeercPacketExtension) message.getExtension("x", XmlUtils.NAMESPACE_TASKS);
        PacketExtension delay = message.getExtension("x", "jabber:x:delay");
        if (extension != null && delay == null) {
            TaskRegistry.getInstance().update(extension.getTask());
        }
    }
}
