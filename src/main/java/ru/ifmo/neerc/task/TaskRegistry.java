package ru.ifmo.neerc.task;

import java.util.*;

/**
 * @author Evgeny Mandrikov
 */
public final class TaskRegistry {

    private static final Map<String, TaskRegistry> INSTANCES = new HashMap<String, TaskRegistry>();

    private final Map<String, Task> tasks = new TreeMap<String, Task>();

    private final Collection<TaskRegistryListener> listeners = new ArrayList<TaskRegistryListener>();

    public static TaskRegistry getInstance() {
        return getInstanceFor(null);
    }

    public static TaskRegistry getInstanceFor(String roomName) {
        TaskRegistry taskRegistry = INSTANCES.get(roomName);
        if (taskRegistry == null) {
            taskRegistry = new TaskRegistry();
            INSTANCES.put(roomName, taskRegistry);
        }
        return taskRegistry;
    }

    /**
     * Hide default constructor.
     */
    private TaskRegistry() {
    }

    public Collection<Task> getTasks() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    public void addListener(TaskRegistryListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(Task task) {
        for (TaskRegistryListener listener : listeners) {
            listener.taskChanged(task);
        }
    }

    private void notifyResetListeners() {
        for (TaskRegistryListener listener : listeners) {
            listener.tasksReset();
        }
    }

    private String genId() {
        return UUID.randomUUID().toString();
    }

    public Task getById(String id) {
        return tasks.get(id);
    }
    
    public void reset() {
        Iterator<Map.Entry<String, Task>> it = tasks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();

            if (entry.getValue().getScheduleType() == Task.ScheduleType.NONE)
                it.remove();
        }

        notifyResetListeners();
    }
    
    public void update(Task task) {
        String id = task.getId();
        if ("remove".equals(task.getType())) {
            tasks.remove(id);
        } else if (id == null) {
            id = genId();
            if (task.getScheduleType() != Task.ScheduleType.NONE)
                id = "s" + id;
            task.setId(id);
            tasks.put(id, task);
        } else {
            tasks.put(id, task);
        }
        notifyListeners(task);
    }

    public List<Task> getAssignedTasks(String user) {
        ArrayList<Task> list = new ArrayList<Task>();
        for (Task task : tasks.values()) {
            if (task.getStatuses().containsKey(user)) {
                list.add(task);
            }
        }
        return list;
    }
}
