// $Id$
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.neerc.chat.message.Message;

/**
 * Interface should be implemented by any class who wants to listen for chat messages.
 * @author Matvey Kazakov
 */
public interface MessageListener {
    /**
     * Method is called when message is received.
     * @param message message received
     */
    void processMessage(Message message);
}

