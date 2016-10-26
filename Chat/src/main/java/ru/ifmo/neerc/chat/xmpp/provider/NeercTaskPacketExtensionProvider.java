package ru.ifmo.neerc.chat.xmpp.provider;

import java.util.Date;
import java.io.IOException;

import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Evgeny Mandrikov
 */
public class NeercTaskPacketExtensionProvider extends ExtensionElementProvider<NeercTaskPacketExtension> {
    public static void register() {
        ProviderManager.addExtensionProvider(
                NeercTaskPacketExtension.ELEMENT,
                NeercTaskPacketExtension.NAMESPACE,
                new NeercTaskPacketExtensionProvider()
        );
    }

    @Override
    public NeercTaskPacketExtension parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
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

    private Task parseTask(XmlPullParser parser) throws XmlPullParserException, IOException {
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
