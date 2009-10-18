/*
 * Date: Oct 24, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;

/**
 * <code>EOFMessage</code> class
 *
 * @author Matvey Kazakov
 */
public class EOFMessage extends Message{
    
    public static final EOFMessage instance = new EOFMessage();

    public EOFMessage() {
        super(EOF_MESSAGE);
    }

    protected void serialize(Config message) {
    }

    protected void deserialize(Config message) {
    }

    public String asString() {
        return "EOD Message";
    }
}

