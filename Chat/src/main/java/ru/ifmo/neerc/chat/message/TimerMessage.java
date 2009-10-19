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
 * Date: 28.10.2005
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;

/**
 * @author Matvey Kazakov
 */
public class TimerMessage extends Message{

    private long total;
    private long time;
    private int status;
    private static final String ATTR_TOTAL = "total";
    private static final String ATTR_TIME = "time";
    private static final String ATTR_STATUS = "status";

    public TimerMessage() {
        super(TIMER_MESSAGE);
    }

    public TimerMessage(long total, long left, int status) {
        this();
        this.status = status;
        this.total = total;
        this.time = left;
    }

    protected void serialize(Config message) {
        message.setProperty(ATTR_TOTAL, "" + total);
        message.setProperty(ATTR_TIME, "" + time);
        message.setProperty(ATTR_STATUS, "" + status);

    }

    protected void deserialize(Config message) {
        total = message.getInt(ATTR_TOTAL);
        time = message.getInt(ATTR_TIME);
        status = message.getInt(ATTR_STATUS);
    }

    public String asString() {
        return null;
    }

    public long getTotal() {
        return total;
    }

    public long getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }
}
