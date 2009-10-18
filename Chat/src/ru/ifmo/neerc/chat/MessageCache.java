/*
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.UserMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * <code>MessageCache</code> class
 *
 * @author Matvey Kazakov
 */
public class MessageCache implements MessageListener {
    
    private static MessageCache instance = new MessageCache();

    public static MessageCache getInstance() {
        return instance;
    }

    private MessageCache() {}

    private Queue<UserMessage> messages = new LinkedList<UserMessage>();
    private static final int MESSAGE_CACHE_SIZE = 20;

    public void processMessage(Message message) {
        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage)message;
            if (userMessage.isImportant()) {
                queueMessage(userMessage);
            }
        }
    }

    public synchronized void queueMessage(UserMessage userMessage) {
        messages.offer(userMessage);
        if (messages.size() > MESSAGE_CACHE_SIZE) {
            messages.poll();
        }
    }
    
    public synchronized List<UserMessage> getMessages() {
        return new ArrayList<UserMessage>(messages);
    }
    
    
}

