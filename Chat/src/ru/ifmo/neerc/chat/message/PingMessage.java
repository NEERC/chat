/*
 * Date: Oct 25, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;

/**
 * <code>PingMessage</code> class
 *
 * @author Matvey Kazakov
 */
public class PingMessage extends Message {

    public PingMessage() {
        super(PING_MESSAGE);
    }

    protected void serialize(Config message) {
        // do nothing
    }

    protected void deserialize(Config message) {
        // do nothing
    }

    public String asString() {
        return "PING";
    }
}

