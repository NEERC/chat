// $Id$
/**
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;

/**
 * @author Matvey Kazakov
 */
public class LoginMessage extends Message {

    private String user;
    private static final String TAG_LOGIN = "login";
    private static final String ATTR_USER = "@user";

    public LoginMessage() {
        super(LOGIN_MESSAGE);
    }

    public LoginMessage(String user) {
        this();
        this.user = user;
    }

    protected void serialize(Config message) {
        message.createNode(TAG_LOGIN).setProperty(ATTR_USER, user);
    }

    protected void deserialize(Config message) {
        user = message.getNode(TAG_LOGIN).getProperty(ATTR_USER);
    }

    public String asString() {
        return new StringBuilder().append("User").append(user).append(" logging in").toString();
    }

    public String getUser() {
        return user;
    }

}

