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
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;

import java.util.Date;

/**
 * Represents abstract chat message.
 *
 * @author Matvey Kazakov
 */
public abstract class Message {

    /**
     * Information message from server
     */
    protected static final int SERVER_MESSAGE = 0;
    /**
     * Message contains some task
     */
    protected static final int TASK_MESSAGE = 1;
    /**
     * Message from user
     */
    protected static final int USER_MESSAGE = 2;

    /**
     * Message timestamp.
     */
    private Date timestamp;

    /**
     * Stores message type.
     */
    private int type;
    /**
     * Serialized representation of this message.
     */
    private byte[] serialized = null;

    /**
     * Destination user. -1 -means everyone.
     */
    private int destination = -1;

    /**
     * Constructor cteates message of given type.
     *
     * @param type        new message type.
     * @param destination destination of the message.
     */
    protected Message(int type, int destination) {
        this.type = type;
        this.destination = destination;
    }

    /**
     * Constructor cteates message of given type.
     *
     * @param type new message type.
     */
    protected Message(int type) {
        this(type, -1);
    }

    /**
     * Return message type.
     *
     * @return message type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns message destination.
     *
     * @return user ID
     */
    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    /**
     * Returns message timestamp.
     *
     * @return message timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns serialized form of the message.
     *
     * @param serialized byte array representing this message.
     */
    public void setSerialized(byte[] serialized) {
        this.serialized = serialized;
    }

    public byte[] getSerialized() {
        return serialized;
    }

    protected void serialize(Config message) {
        throw new UnsupportedOperationException();
    }

    protected void deserialize(Config message) {
        throw new UnsupportedOperationException();
    }

    public abstract String asString();

    public boolean shouldBeSentTo(int id) {
        return destination == -1 || destination == id;
    }
}
