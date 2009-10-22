package ru.ifmo.neerc.task;

import java.util.*;

/**
 * @author Evgeny Mandrikov
 */
public class Task {

    private String type;

    private String title;

    private Map<String, TaskStatus> statuses = new HashMap<String, TaskStatus>();

    public Task() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, TaskStatus> getStatuses() {
        return Collections.unmodifiableMap(statuses);
    }

    public void setStatus(String from, String type, String value) {
        TaskStatus status = statuses.get(from);
        if (status == null) {
            status = new TaskStatus();
            statuses.put(from, status);
        }
        status.setType(type);
        status.setValue(value);
    }
}
