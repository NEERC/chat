package ru.ifmo.neerc.chat.xmpp.provider;

import java.io.IOException;

import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercClockPacketExtensionProvider extends ExtensionElementProvider<NeercClockPacketExtension> {
    public static void register() {
        ProviderManager.addExtensionProvider(
                NeercClockPacketExtension.ELEMENT,
                NeercClockPacketExtension.NAMESPACE,
                new NeercClockPacketExtensionProvider()
        );
    }

    @Override
    public NeercClockPacketExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
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
