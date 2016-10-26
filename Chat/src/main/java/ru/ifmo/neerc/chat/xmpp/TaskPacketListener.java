package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import ru.ifmo.neerc.chat.xmpp.provider.NeercTaskPacketExtension;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class TaskPacketListener implements StanzaListener {
    @Override
    public void processPacket(Stanza packet) {
        NeercTaskPacketExtension extension = packet.getExtension(NeercTaskPacketExtension.ELEMENT, NeercTaskPacketExtension.NAMESPACE);
        DelayInformation delay = packet.getExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE);
        if (extension != null && delay == null) {
            TaskRegistry.getInstance().update(extension.getTask());
        }
    }
}
