package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.XmlStringBuilder;

import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercClockPacketExtension implements ExtensionElement {
    public static final String ELEMENT = "x";
    public static final String NAMESPACE = XmlUtils.NAMESPACE_CLOCK;

    private Clock clock;

    public NeercClockPacketExtension() {
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
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
        xml.closeElement(getElementName());

        return xml;
    }
}
