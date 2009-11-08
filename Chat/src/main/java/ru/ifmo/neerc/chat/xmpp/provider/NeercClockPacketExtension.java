package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercClockPacketExtension implements PacketExtension {
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
        return "x";
    }

    @Override
    public String getNamespace() {
        return XmlUtils.NAMESPACE_CLOCK;
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
        buf.append("</").append(getElementName()).append(">");
        return buf.toString();
    }
}
