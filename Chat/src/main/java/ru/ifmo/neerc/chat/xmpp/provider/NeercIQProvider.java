package ru.ifmo.neerc.chat.xmpp.provider;

import java.io.IOException;

import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.ifmo.neerc.chat.xmpp.packet.NeercIQ;
import ru.ifmo.neerc.chat.xmpp.packet.NeercTaskListIQ;
import ru.ifmo.neerc.chat.xmpp.packet.NeercUserListIQ;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NeercIQProvider extends IQProvider<NeercIQ> {
	public static void register() {
		IQProvider provider = new NeercIQProvider();
		ProviderManager.addIQProvider("query", XmlUtils.NAMESPACE_USERS, provider);
		ProviderManager.addIQProvider("query", XmlUtils.NAMESPACE_TASKS, provider);
	}

	@Override
	public NeercIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
		String namespace = parser.getNamespace();
		NeercIQ packet;
		if (XmlUtils.NAMESPACE_USERS.equals(namespace)) {
			packet = new NeercUserListIQ();
		} else if (XmlUtils.NAMESPACE_TASKS.equals(namespace)) {
			packet = new NeercTaskListIQ();
		} else {
			throw new UnsupportedOperationException();
		}
		// TODO: clock
		packet.parse(parser);
		return packet;
	}
}
