// $Id$
/**
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;
import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;

/**
 * @author Matvey Kazakov
 */
public class UserMessage extends Message {
    private int from;
    private UserText text;
    private static final String TAG_USER = "user";
    private static final String ATTR_FROM = "@from";
    private static final String ATTR_TO = "@to";

    public UserMessage() {
        super(USER_MESSAGE);
    }

    public UserMessage(UserText text) {
        this(-1, text);
    }

    public UserMessage(int from, UserText text) {
        this(from, -1, text);
    }
    
    public UserMessage(int from, int to, UserText text) {
        super(USER_MESSAGE, to);
        this.text = text;
        this.from = from;
    }

    public void serialize(Config message) {
        Config userNode = message.createNode(TAG_USER);
        userNode.setProperty(ATTR_FROM, "" + from);
        userNode.setProperty(ATTR_TO, "" + getDestination());
        message.setProperty(TAG_USER, text.asString());
    }

    public void deserialize(Config message) {
        Config userElement = message.getNode(TAG_USER);
        from = userElement.getInt(ATTR_FROM);
        setDestination(userElement.getInt(ATTR_TO));
        text = new UserText();
        text.fromString(message.getProperty(TAG_USER));
    }

    public String asString() {
        String fromName = UserRegistry.getInstance().search(from).getName();
        StringBuilder route = getDestination() < 0 ? new StringBuilder().append(fromName) 
                : new StringBuilder().append(fromName).append(">").append(
                UserRegistry.getInstance().search(getDestination()).getName());
        return route.append(": ").append(text.getText()).toString();
    }

    public void setFrom(int from) {
        setSerialized(null);
        this.from = from;
    }

    public int getFrom() {
        return from;
    }

    public UserText getText() {
        return text;
    }
    
    public boolean isImportant() {
        try {
            char c = '.';
            if (text != null && text.getText() != null && text.getText().length() > 0) {
                c = text.getText().charAt(0);
            }
            UserEntry userEntry = UserRegistry.getInstance().search(from);
            return (c == '#' || c == '¹' || c == '!' || c == '?') && userEntry != null && userEntry.isPower();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPrivate() {
        return getDestination() >= 0;
    }

    public boolean shouldBeSentTo(int id) {
        return super.shouldBeSentTo(id) || from == id;
    }
}

