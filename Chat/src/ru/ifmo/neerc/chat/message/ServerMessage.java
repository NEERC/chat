// $Id$
/**
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;
import ru.ifmo.neerc.chat.UserEntry;

/**
 * @author Matvey Kazakov
 */
public class ServerMessage extends Message {
    public static final int USER_JOINED = 100;
    public static final int USER_LEFT = 101;
    private UserEntry user;
    private int eventType;
    private static final String TAG_SERVER = "server";
    private static final String ATTR_EVENT = "@event";

    ServerMessage() {
        super(SERVER_MESSAGE);
    }

    public ServerMessage(int eventType, UserEntry user) {
        this();
        this.user = user;
        this.eventType = eventType;
    }

    public int getEventType() {
        return eventType;
    }

    public UserEntry getUser() {
        return user;
    }

    protected void serialize(Config message) {
        Config serverElement = message.createNode(TAG_SERVER);
        serverElement.setProperty(ATTR_EVENT, "" + eventType);
        user.serialize(serverElement);
    }

    protected void deserialize(Config message) {
        Config serverElement = message.getNode(TAG_SERVER);
        user = new UserEntry();
        user.deserialize(serverElement);
        eventType = serverElement.getInt(ATTR_EVENT);
    }

    public String asString() {
        if (eventType == USER_JOINED) {
            return new StringBuilder().append("----->>> User ").append(user).append(" has joined the chat.").toString();
        } else {
            return new StringBuilder().append("----->>> User ").append(user).append(" has left the chat.").toString();
        }
    }

}

