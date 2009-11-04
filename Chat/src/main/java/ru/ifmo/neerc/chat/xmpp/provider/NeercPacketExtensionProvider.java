package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercPacketExtensionProvider implements PacketExtensionProvider {
    public static void register() {
        ProviderManager.getInstance().addExtensionProvider(
                "x",
                XmlUtils.NAMESPACE_TASKS,
                new NeercPacketExtensionProvider()
        );
    }

    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        NeercPacketExtension neercPacketExtension = new NeercPacketExtension();
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("task")) {
                    neercPacketExtension.setTask(parseTask(parser));
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("x")) {
                    done = true;
                }
            }
        }
        return neercPacketExtension;
    }

    private Task parseTask(XmlPullParser parser) throws Exception {
        Task task = new Task(
                parser.getAttributeValue("", "id"),
                parser.getAttributeValue("", "type"),
                parser.getAttributeValue("", "title")
        );
        boolean done = false;
        while (!done) {
            int eventType = parser.next();
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("status")) {
                    task.setStatus(
                            parser.getAttributeValue("", "for"),
                            parser.getAttributeValue("", "type"),
                            parser.getAttributeValue("", "value")
                    );
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("task")) {
                    done = true;
                }
            }
        }
        return task;
    }
}
