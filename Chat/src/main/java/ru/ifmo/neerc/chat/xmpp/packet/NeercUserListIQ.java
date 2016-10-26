package ru.ifmo.neerc.chat.xmpp.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.jivesoftware.smack.packet.IQ;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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

    @Override
    protected IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();

		for (UserEntry user : users) {
            xml.halfOpenElement("user");
            xml.attribute("name", user.getName());
            xml.attribute("group", user.getGroup());
            xml.attribute("power", user.isPower() ? "yes" : "no");
            xml.closeEmptyElement();
		}

        return xml;
	}

    @Override
	public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
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

	private UserEntry parseUser(XmlPullParser parser) {
		String name = parser.getAttributeValue("", "name");
		String group = parser.getAttributeValue("", "group");
		boolean power = "yes".equals(parser.getAttributeValue("", "power"));
		UserEntry user = new UserEntry(name, 0, name, power);
		user.setGroup(group);
		return user;
	}
}
