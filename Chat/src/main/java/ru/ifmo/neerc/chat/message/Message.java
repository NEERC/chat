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
     * Constructor cteates message of given type.
     *
     * @param type        new message type.
     */
    protected Message(int type) {
        this.type = type;
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
}
