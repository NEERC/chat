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
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;
import ru.ifmo.neerc.chat.*;

/**
 * @author Matvey Kazakov
 */
public class TaskMessage extends Message {
    public static final int CREATE = 0;
    public static final int ASSIGN = 1;
    public static final int COMPLETE = 2;
    public static final int DELETE = 3;

    private int taskMsgType;
    private int user, taskId;
    private Task task;
    private TaskResult answer;
    private static final String NODE_TASK = "task";
    private static final String ATTR_TYPE = "@type";
    private static final String ATTR_USER = "@user";
    private static final String ATTR_TASK = "@task";
    private static final String ATTR_USERNAME = "@username";
    private static final String NODE_ANSWER = "complete";

    public TaskMessage() {
        super(TASK_MESSAGE);
    }

    public TaskMessage(int taskMsgType, int user, Task task, TaskResult answer) {
        this();
        this.taskMsgType = taskMsgType;
        this.user = user;
        this.task = task;
        this.answer = answer;
    }

    public boolean allowed(UserEntry entry) {
        return (taskMsgType == COMPLETE || entry.isPower());
    }

    @Override
    protected void serialize(Config message) {
        Config node = message.createNode(NODE_TASK);
        node.setProperty(ATTR_TYPE, "" + taskMsgType);
        node.setProperty(ATTR_USER, "" + user);
        if (user != -1) {
            node.setProperty(ATTR_USERNAME, UserRegistry.getInstance().search(user).getName());
        }
        if (taskMsgType == CREATE) {
            task.serialize(node);
        } else {
            node.setProperty(ATTR_TASK, String.valueOf(getTaskId()));
            if (taskMsgType == COMPLETE) {
                answer.serialize(node.createNode(NODE_ANSWER));
            }
        }
    }

    @Override
    protected void deserialize(Config message) {
        Config node = message.getNode(NODE_TASK);
        taskMsgType = node.getInt(ATTR_TYPE);
        user = node.getInt(ATTR_USER);
        if (user != -1) {
            String userName = node.getString(ATTR_USERNAME);
            user = UserRegistry.getInstance().findByName(userName).getId();
        }
        if (taskMsgType == CREATE) {
            task = new Task();
            task.deserialize(node);
        } else {
            taskId = node.getInt(ATTR_TASK);
            task = TaskRegistry.getInstance().findTask(taskId);
            if (task != null && taskMsgType == COMPLETE) {
                answer = task.getResult(user);
                if (answer != null) {
                    answer.deserialize(node.getNode(NODE_ANSWER));
                }
            }
        }
    }

    public String asString() {
        StringBuilder res = new StringBuilder("-----!!! Task '" + task.getDescription() + "' ");
        switch (taskMsgType) {
            case CREATE:
                res.append("is created");
                break;
            case DELETE:
                res.append("is deleted");
                break;
            case ASSIGN:
                res.append("is assigned to ").append(UserRegistry.getInstance().search(user).getName());
                break;
            case COMPLETE:
                res.append("is completed by ").append(UserRegistry.getInstance().search(user).getName());
                break;
        }
        return res.toString();
    }

    public int getTaskMsgType() {
        return taskMsgType;
    }

    public int getUser() {
        return user;
    }

    public Task getTask() {
        return task;
    }

    public int getTaskId() {
        return task != null ? task.getId() : taskId;
    }

    public TaskResult getAnswer() {
        return answer;
    }
}
