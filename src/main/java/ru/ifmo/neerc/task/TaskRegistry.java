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

    private String genId() {
        return Long.toString(nextId++);
    }

    public void update(Task task) {
        if (task.getId() == null) {
            String id = genId();
            task = new Task(id, task.getTitle(), task.getType());
            tasks.put(id, task);
        }

        Task old = tasks.get(task.getId());
        for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
            TaskStatus taskStatus = entry.getValue();
            old.setStatus(entry.getKey(), taskStatus.getType(), taskStatus.getValue());
        }
        notifyListeners(task);
    }
}
