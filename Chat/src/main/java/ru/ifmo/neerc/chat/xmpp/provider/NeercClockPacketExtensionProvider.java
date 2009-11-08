package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercClockPacketExtensionProvider implements PacketExtensionProvider {
    public static void register() {
        ProviderManager.getInstance().addExtensionProvider(
                "x",
                XmlUtils.NAMESPACE_CLOCK,
                new NeercClockPacketExtensionProvider()
        );
    }

    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        NeercClockPacketExtension neercPacketExtension = new NeercClockPacketExtension();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("clock")) {
                    Clock clock = new Clock();
                    clock.setTime(Long.parseLong(parser.getAttributeValue("", "time")));
                    clock.setTotal(Long.parseLong(parser.getAttributeValue("", "total")));
                    clock.setStatus(Integer.parseInt(parser.getAttributeValue("", "status")));
                    neercPacketExtension.setClock(clock);
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("x")) {
                    done = true;
                }
            }
        }
        return neercPacketExtension;
    }
}
