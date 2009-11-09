package ru.ifmo.neerc.chat.xmpp.packet;

import java.util.*;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NeercUserListIQ extends NeercIQ {
	private Collection<UserEntry> users = new ArrayList<UserEntry>();

	public NeercUserListIQ() {
		super("users");
	}
	public String getElementName() {
		return "query";
	}

	public String getNamespace() {
		return XmlUtils.NAMESPACE_USERS;
	}

	public Collection<UserEntry> getUsers() {
		return Collections.unmodifiableCollection(users);
	}
 
	public void addUser(UserEntry user) {
		users.add(user);
	}

	public void addUser(String name, String group, boolean power) {
		UserEntry user = new UserEntry(name, users.size(), name, power);
		user.setGroup(group);
		addUser(user);
	}

	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
		for (UserEntry user: users) {
			buf.append("<user");
			buf.append(" name=\"").append(escape(user.getName())).append("\"");
			buf.append(" group=\"").append(escape(user.getGroup())).append("\"");
			buf.append(" power=\"").append(user.isPower() ? "yes" :"no").append("\" />");
		}
		buf.append("</").append(getElementName()).append(">");
		return buf.toString();
	}

	public void parse(XmlPullParser parser) throws Exception {
		boolean done = false;
		while (!done) {
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("user")) {
					addUser(parseUser(parser));
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().equals("query")) {
					done = true;
				}
			}
		}
	}

	private UserEntry parseUser(XmlPullParser parser) throws Exception {
		String name = parser.getAttributeValue("", "name");
		String group = parser.getAttributeValue("", "group");
		boolean power = "yes".equals(parser.getAttributeValue("", "power"));
		UserEntry user = new UserEntry(name, 0, name, power);
		user.setGroup(group);
		return user;
	}
}
