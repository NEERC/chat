package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;

import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercTaskPacketExtension implements ExtensionElement {
    public static final String ELEMENT = "x";
    public static final String NAMESPACE = XmlUtils.NAMESPACE_TASKS;

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
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public CharSequence toXML() {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();

        if (getTask() != null) {
            // TODO
        }

        xml.closeElement(getElementName());

        return xml;
    }
}
