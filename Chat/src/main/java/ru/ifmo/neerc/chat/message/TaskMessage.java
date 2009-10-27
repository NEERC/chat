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
import ru.ifmo.neerc.chat.task.Task;
import ru.ifmo.neerc.chat.task.TaskRegistry;
import ru.ifmo.neerc.chat.task.TaskResult;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import static ru.ifmo.neerc.chat.message.TaskMessage.Type.*;

/**
 * @author Matvey Kazakov
 */
public class TaskMessage extends Message {
	public static enum Type {
		CREATE(0), ASSIGN(1), COMPLETE(2), DELETE(3);
		public final int ID;
		Type(int id) {
			this.ID = id;
		}

		static Type valueOf(int id) {
			for (Type t : values()) {
				if (t.ID == id) {
					return t;
				}
			}
			throw new IllegalArgumentException("Unknown TaskMessage type ID: " + id);
		}
	}

    private Type type;
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

    public TaskMessage(Type type, int user, Task task, TaskResult answer) {
        this();
        this.type = type;
        this.user = user;
        this.task = task;
        this.answer = answer;
    }

    public boolean allowed(UserEntry entry) {
        return (type == COMPLETE || entry.isPower());
    }

    @Override
    protected void serialize(Config message) {
        Config node = message.createNode(NODE_TASK);
        node.setProperty(ATTR_TYPE, "" + type.ID);
        node.setProperty(ATTR_USER, "" + user);
        if (user != -1) {
            node.setProperty(ATTR_USERNAME, UserRegistry.getInstance().search(user).getName());
        }
        if (type == CREATE) {
            task.serialize(node);
        } else {
            node.setProperty(ATTR_TASK, String.valueOf(getTaskId()));
            if (type == COMPLETE) {
                answer.serialize(node.createNode(NODE_ANSWER));
            }
        }
    }

    @Override
    protected void deserialize(Config message) {
        Config node = message.getNode(NODE_TASK);
        int typeId = node.getInt(ATTR_TYPE);
		type = Type.valueOf(typeId);
        user = node.getInt(ATTR_USER);
        if (user != -1) {
            String userName = node.getString(ATTR_USERNAME);
            user = UserRegistry.getInstance().findByName(userName).getId();
        }
        if (type == CREATE) {
            task = new Task();
            task.deserialize(node);
        } else {
            taskId = node.getInt(ATTR_TASK);
            task = TaskRegistry.getInstance().findTask(taskId);
            if (task != null && type == COMPLETE) {
                answer = task.getResult(user);
                if (answer != null) {
                    answer.deserialize(node.getNode(NODE_ANSWER));
                }
            }
        }
    }

    public String asString() {
        StringBuilder res = new StringBuilder("-----!!! Task '" + task.getDescription() + "' ");
        switch (type) {
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

    public Type getTaskMsgType() {
        return type;
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
