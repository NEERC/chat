// $Id$
/**
 * Date: 28.10.2005
 */
package ru.ifmo.neerc.chat.server;

import ru.ifmo.neerc.chat.ChatLogger;
import ru.ifmo.neerc.chat.MessageListener;
import ru.ifmo.neerc.chat.message.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Matvey Kazakov
 */
class ChatServerMessageLogger implements MessageListener {
    public void processMessage(Message message) {
        String s = message.asString();
        if (s != null) {
            ChatLogger.logChat("                                 " + s);
        }
    }
}

