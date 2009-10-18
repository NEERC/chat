// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.ips.Utils;
import ru.ifmo.ips.config.Config;
import ru.ifmo.ips.config.ConfigException;
import ru.ifmo.neerc.chat.message.UserText;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Matvey Kazakov
 */
public class Task implements Comparable<Task> {
    private UserText description = new UserText();
    private Set<Integer> assignedUsers = new HashSet<Integer>();
    private Map<Integer, TaskResult> taskResults = new HashMap<Integer, TaskResult>();
    private int id;
    
    private int type = TaskFactory.TASK_TODO;

    private static int LAST_ID = 0;
    private static final String TASK_NODE = "task";
    private static final String ATTR_ID = "@id";
    private static final String ATTR_TYPE = "@type";
    private static final String NODE_DESC = "desc";
    private static final String NODE_ASSIGNED = "assigned";
    private static final String NODE_RESULT = "result";


    Task(int id, String description, int type) {
        this();
        this.description.setText(description);
        this.id = id;
        this.type = type;
    }

    public Task() {
    }

    void genId() {
        id = LAST_ID++;
    }

    public synchronized void assign(int user) {
        assignedUsers.add(user);
    }

    public String getDescription() {
        return description.getText();
    }

    public Set<Integer> getAssignedUsers() {
        return assignedUsers;
    }

    public int getId() {
        return id;
    }

    /**
     * Returns <code>true</code> in case there are no users assigned to this task.
     *
     * @return
     */
    public boolean isCompleted() {
        return assignedUsers.size() == 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Task)) {
            return false;
        }

        final Task task = (Task)o;

        if (id != task.id) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return id;
    }

    public void serialize(Config config) {
        Config node = config.createNode(TASK_NODE + "#" + id);
        node.setProperty(ATTR_TYPE, "" + type);
        node.setProperty(NODE_DESC, description.asString());
        for (Integer user : assignedUsers) {
            node.createNode(NODE_ASSIGNED + "#" + user);
        }
        for (Map.Entry<Integer, TaskResult> entry : taskResults.entrySet()) {
            int userId = entry.getKey();
            TaskResult result = entry.getValue();
            result.serialize(node.createNode(NODE_RESULT + "#" + userId));
        }
        
    }

    public void deserialize(Config config) {
        Config node = config.getNode(TASK_NODE);
        id = node.getInt(ATTR_ID);
        type = node.getInt(ATTR_TYPE);
        description.fromString(node.getString(NODE_DESC));
        try {
            Config[] assigned = node.getNodeList(NODE_ASSIGNED);
            for (int i = 0; i < assigned.length; i++) {
                assignedUsers.add(assigned[i].getInt(ATTR_ID));
            }
        } catch (ConfigException e) {
            // do nothing
        }
        try {
            Config[] results = node.getNodeList(NODE_RESULT);
            for (int i = 0; i < results.length; i++) {
                Config resultNode = results[i];
                int userId = resultNode.getInt(ATTR_ID);
                TaskResult result = TaskFactory.create(type);
                result.deserialize(resultNode);
                taskResults.put(userId, result);
            }
        } catch (ConfigException e) {
            // do nothing
        }
    }

    public int compareTo(Task task) {
        return getId() - task.getId();
    }

    public String toString() {
        return description.getText() + ": assigned " + assignedUsers.size();
    }

    public void completeTask(int uid, TaskResult result) {
        taskResults.put(uid, result);
    }

    public TaskResult getAnswer(int user) {
        return taskResults.get(user);
    }

    public boolean isAssigned(int userId) {
        synchronized (assignedUsers) {
            return assignedUsers.contains(userId);
        }
    }
    
    public TaskResult getResult(int userId) {
        TaskResult taskResult = taskResults.get(userId);
        if (taskResult == null && isAssigned(userId)) {
            taskResult = TaskFactory.create(type);
            taskResults.put(userId, taskResult);
        }
        return taskResult;
    }

    public int getVisualState() {
        int vstate = TaskFactory.VSTATE_DONE;
        for (int user : assignedUsers) {
            vstate = Math.max(vstate, getResult(user).getVisualState());
        }
        return vstate;
    }
}

