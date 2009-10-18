// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat;

/**
 * @author Matvey Kazakov
 */
public interface TaskRegistryListener {
    void taskAdded(Task task);
    void taskDeleted(Task taskId);
    void taskChanged(Task taskId);
}

