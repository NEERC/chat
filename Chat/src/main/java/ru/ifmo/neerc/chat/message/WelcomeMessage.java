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
import ru.ifmo.neerc.chat.Task;
import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;

/**
 * @author Matvey Kazakov
 */
public class WelcomeMessage extends Message {

    private UserEntry[] entries;
    private Task[] tasks;
    private int userId;
    private static final String TAG_WELCOME = "welcome";
    private static final String TAG_USER = "u";
    private static final String TAG_TASK = "t";
    private static final String ATTR_USERID = "@userid";

    public WelcomeMessage(UserEntry[] entries, Task[] tasks, int userId) {
        this();
        this.entries = entries;
        this.tasks = tasks;
        this.userId = userId;
    }

    WelcomeMessage() {
        super(WELCOME_MESSAGE);
    }

    public UserEntry[] getEntries() {
        return entries;
    }

    public Task[] getTasks() {
        return tasks;
    }

    protected void serialize(Config message) {
        Config welcomeElement = message.createNode(TAG_WELCOME);
        welcomeElement.setProperty(ATTR_USERID, "" + userId);
        for (int i = 0; i < entries.length; i++) {
            UserEntry entry = entries[i];
            Config userElement = welcomeElement.createNode(TAG_USER + "#" + i);
            entry.serialize(userElement);
        }
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            Config taskElement = welcomeElement.createNode(TAG_TASK + "#" + i);
            task.serialize(taskElement);
        }
    }

    protected void deserialize(Config message) {
        Config welcomeElement = message.getNode(TAG_WELCOME);
        userId = welcomeElement.getInt(ATTR_USERID);
        Config[] list;
        int length;
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
        try {
            list = welcomeElement.getNodeList(TAG_TASK);
            length = list.length;
            tasks = new Task[length];
            for (int i = 0; i < length; i++) {
                Config task = list[i];
                Task taskEntry = new Task();
                taskEntry.deserialize(task);
                tasks[i] = taskEntry;
            }
        } catch (ConfigException e) {
            // do nothing
        }
    }

    public String asString() {
        return new StringBuilder().append("User ").append(UserRegistry.getInstance().search(userId).getName()).append(" is welcome").toString();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}
