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

import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;

/**
 * @author Matvey Kazakov
 */
public class UserMessage extends Message {
    private int from;
    private String text;

    public UserMessage(int from, String text) {
        super(USER_MESSAGE);
        this.from = from;
        this.text = text;
    }

    public UserMessage(int from, int to, String text) {
        super(USER_MESSAGE, to);
        this.from = from;
        this.text = text;
    }

    public String asString() {
        String fromName = UserRegistry.getInstance().search(from).getName();
        StringBuilder route = getDestination() < 0 ? new StringBuilder().append(fromName)
                : new StringBuilder().append(fromName).append(">").append(
                UserRegistry.getInstance().search(getDestination()).getName());
        return route.append(": ").append(getText()).toString();
    }

    public int getFrom() {
        return from;
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
            UserEntry userEntry = UserRegistry.getInstance().search(from);
            return (c == '#' || c == '\uFFFD' || c == '!' || c == '?') && userEntry != null && userEntry.isPower();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPrivate() {
        return getDestination() >= 0;
    }

    public boolean shouldBeSentTo(int id) {
        return super.shouldBeSentTo(id) || from == id;
    }
}
