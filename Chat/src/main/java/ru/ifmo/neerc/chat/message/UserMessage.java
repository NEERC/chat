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
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;

import java.util.Date;

/**
 * @author Matvey Kazakov
 */
public class UserMessage extends Message {
    private String text;

    private String jid;

    public UserMessage(String jid, int destination, String text) {
        super(USER_MESSAGE, destination);
        this.jid = jid;
        this.text = text;
    }

    public UserMessage(String jid, String text, Date timestamp) {
        super(USER_MESSAGE);
        this.jid = jid;
        this.text = text;
        setTimestamp(timestamp);
    }

    public String getJid() {
        return jid;
    }

    public String asString() {
        String fromName = UserRegistry.getInstance().findOrRegister(jid).getName();
        StringBuilder route = getDestination() < 0 ? new StringBuilder().append(fromName)
                : new StringBuilder().append(fromName).append(">").append(
                UserRegistry.getInstance().findOrRegister(jid).getName());
        return route.append(": ").append(getText()).toString();
    }

    public String getText() {
        return text;
    }

    public boolean isImportant() {
        try {
            char c = '.';
            if (getText() != null && getText().length() > 0) {
                c = getText().charAt(0);
            }
            UserEntry userEntry = UserRegistry.getInstance().findOrRegister(jid);
            return (c == '#' || c == '\uFFFD' || c == '!' || c == '?') && userEntry != null && userEntry.isPower();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPrivate() {
        return getDestination() >= 0;
    }
}
