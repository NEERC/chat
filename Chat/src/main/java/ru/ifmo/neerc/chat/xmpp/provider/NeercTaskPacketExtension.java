package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercTaskPacketExtension implements PacketExtension {
    private Task task;

    public NeercTaskPacketExtension() {
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @Override
    public String getElementName() {
        return "x";
    }

    @Override
    public String getNamespace() {
        return XmlUtils.NAMESPACE_TASKS;
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        if (getTask() != null) {
            // TODO
        }
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }
}
