package ru.ifmo.neerc.chat.xmpp.provider;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.xmpp.packet.*;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NeercIQProvider implements IQProvider {
	public static void register() {
		ProviderManager pm = ProviderManager.getInstance();
		IQProvider provider = new NeercIQProvider();
		pm.addIQProvider("query", XmlUtils.NAMESPACE_USERS, provider);
		pm.addIQProvider("query", XmlUtils.NAMESPACE_TASKS, provider);
	}

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
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
