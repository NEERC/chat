package ru.ifmo.neerc.task;

/**
 * @author Evgeny Mandrikov
 */
public interface TaskRegistryListener {

    void taskChanged(Task task);
    void tasksReset();
}
