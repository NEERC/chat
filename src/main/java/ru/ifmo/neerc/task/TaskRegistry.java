package ru.ifmo.neerc.task;

import java.util.*;

/**
 * @author Evgeny Mandrikov
 */
public final class TaskRegistry {

    private static final TaskRegistry INSTANCE = new TaskRegistry();

    private final Map<String, Task> tasks = new HashMap<String, Task>();

    private final Collection<TaskRegistryListener> listeners = new ArrayList<TaskRegistryListener>();

    public static TaskRegistry getInstance() {
        return INSTANCE;
    }

    private long nextId = 0;

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
        return Long.toString(nextId++);
    }

    public Task getById(String id) {
        return tasks.get(id);
    }
    
    public void reset() {
        tasks.clear();
        notifyResetListeners();
    }
    
    public void update(Task task) {
        String id = task.getId();
        if ("remove".equals(task.getType())) {
            tasks.remove(id);
        } else if (id == null) {
            id = genId();
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
