package ru.ifmo.neerc.task;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeny Mandrikov
 */
public class Task {

    private String id;

    private String type;

    private String title;
    
    private Date date = new Date();

    private Map<String, TaskStatus> statuses = new HashMap<String, TaskStatus>();

    public Task(String type, String title) {
    	this.type = type;
        this.title = title;
    }
    
    public Task(String id, String type, String title, Date date) {
    	this(id, type, title);
        this.date = date;
    }

    public Task(String id, String type, String title) {
        this(type, title);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }
    
    public Date getDate() {
        return date;
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
    
    public TaskStatus getStatus(String from) {
        return statuses.get(from);
    }

    public void setId(String id) {
        this.id = id;
    }
}
