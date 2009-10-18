// $Id$
/**
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.MessageFactory;
import ru.ifmo.neerc.chat.message.EOFMessage;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * @author Matvey Kazakov
 */
public class MessageUtils {
    public static Message getMessage(InputStream in) {
        try {
            ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
            while (true) {
                int i = in.read();
                if (i == -1) {
                    return EOFMessage.instance;
                }
                if (i == 0) {
                    break;
                }
                messageBuffer.write(i);
            }
            return MessageFactory.getInstance().deserialize(messageBuffer.toByteArray());
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            return EOFMessage.instance;
        }
    }
}

