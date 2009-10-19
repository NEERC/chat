/*
   Copyright 2009 NEERC team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
