/*
 * Date: Nov 4, 2006
 *
 * $Id$
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.ips.config.Config;

/**
 * <code>TaskResult</code> class
 *
 * @author Matvey Kazakov
 */
public abstract class TaskResult {
    
    public abstract String toString();

    public abstract void serialize(Config node);
    public abstract void deserialize(Config node);


    public abstract boolean actionSupported(int action);
    public abstract void performAction(int action, Object... param);
    
    public abstract int getVisualState();

}

