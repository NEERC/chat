package ru.ifmo.neerc.chat.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NeercIQ extends IQ {
	private String namespace;
	private String elementName;

	public NeercIQ(String name) {
		this(name, "query");
	}

	public NeercIQ(String name, String element) {
		super();
		namespace = XmlUtils.NAMESPACE + "#" + name;
		elementName = element;
	}

	public String getElementName() {
		return elementName;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\" />");
		return buf.toString();
	}
	
	public void parse(XmlPullParser parser) throws Exception {
		throw new UnsupportedOperationException();
	}
	
	protected String escape(String xml) {
		return xml.replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;");
	}
}
