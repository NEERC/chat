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

import ru.ifmo.ips.config.Config;
import ru.ifmo.ips.config.ConfigException;
import ru.ifmo.neerc.chat.UserEntry;


/**
* @author Matvey Kazakov
*/
public class UserListUpdateMessage extends Message {

    private UserEntry[] entries;
    private static final String TAG_WELCOME = "welcome";
    private static final String TAG_USER = "u";

    public UserListUpdateMessage(UserEntry[] entries) {
        this();
        this.entries = entries;
    }

    UserListUpdateMessage() {
        super(UPDATE_USERS_LIST_MESSAGE);
    }

    public UserEntry[] getEntries() {
        return entries;
    }

    protected void serialize(Config message) {
        Config welcomeElement = message.createNode(TAG_WELCOME);
        for (int i = 0; i < entries.length; i++) {
            UserEntry entry = entries[i];
            Config userElement = welcomeElement.createNode(TAG_USER + "#" + i);
            entry.serialize(userElement);
        }
    }

    protected void deserialize(Config message) {
        Config welcomeElement = message.getNode(TAG_WELCOME);
        Config[] list = new Config[0];
        int length = 0;
        try {
            list = welcomeElement.getNodeList(TAG_USER);
            length = list.length;
            entries = new UserEntry[length];
            for (int i = 0; i < length; i++) {
                Config user = list[i];
                UserEntry userEntry = new UserEntry();
                userEntry.deserialize(user);
                entries[i] = userEntry;
            }
        } catch (ConfigException e) {
            // do nothing
        }
    }

    public String asString() {
        return new StringBuilder().append("Users list is updated").toString();
    }

}
