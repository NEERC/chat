package ru.ifmo.neerc.chat.xmpp.provider;

import java.util.Date;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercTaskPacketExtensionProvider implements PacketExtensionProvider {
    public static void register() {
        ProviderManager.getInstance().addExtensionProvider(
                "x",
                XmlUtils.NAMESPACE_TASKS,
                new NeercTaskPacketExtensionProvider()
        );
    }

    @Override
    public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
        NeercTaskPacketExtension neercPacketExtension = new NeercTaskPacketExtension();
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
    	Date date = new Date();
    	String timestamp = parser.getAttributeValue("", "timestamp");
    	if (timestamp != null) {
    		date = new Date(Long.parseLong(timestamp));
    	}
        Task task = new Task(
                parser.getAttributeValue("", "id"),
                parser.getAttributeValue("", "type"),
                parser.getAttributeValue("", "title"),
                date
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
