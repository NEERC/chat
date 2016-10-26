package ru.ifmo.neerc.chat.xmpp.packet;

import java.io.IOException;

import org.jivesoftware.smack.packet.IQ;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NeercIQ extends IQ {
	public NeercIQ(String name) {
		this(name, "query");
	}

	public NeercIQ(String name, String element) {
		super(element, XmlUtils.NAMESPACE + "#" + name);
	}

    protected IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.rightAngleBracket();
        return xml;
    }

    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        throw new UnsupportedOperationException();
    }
}
