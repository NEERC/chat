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
// $Id$
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.server;

import ru.ifmo.neerc.chat.MessageListener;
import ru.ifmo.neerc.chat.message.Message;

import java.util.*;

/**
 * Class represents checkpoint for all messages. It has special queue of messages and list of listeners.
 * After receiving message it spreads this message among listeners. Realized as singleton
 *
 * @author Matvey Kazakov
 */
class MessageMulticaster {
    private List<MessageListener> messageListeneres = new ArrayList<MessageListener>();
    private Queue<Message> messageQueue = new LinkedList<Message>();
    
    private static MessageMulticaster instance = new MessageMulticaster();

    /**
     * Returns multicater instance.
     */
    public static MessageMulticaster getInstance() {
        return instance;
    }

    /**
     * Guard to avoid direct instanciating
     */
    private MessageMulticaster() {}

    public synchronized void addMessageListener(MessageListener out) {
        messageListeneres.add(out);
    }

    /**
     * Removes message listener from the list
     * @param listener listener to be removed
     */
    public synchronized void removeMessageListener(MessageListener listener) {
        messageListeneres.remove(listener);
    }

    /**
     * Spreads message among all listeners.
     * @param msg message to be spread
     */
    public synchronized void sendMessage(Message msg) {
        // add emssageto the queue
        messageQueue.offer(msg);
        Message message;
        // while there are messages in the queue, we spread them
        // I'm not sure that this place will work more then ones, but still to be sure
        while ((message = messageQueue.poll()) != null) {
            // Go thru the list of listeners and send the message to them 
            for (MessageListener listener : messageListeneres) {
                listener.processMessage(message);
            }
        }
    }
}

