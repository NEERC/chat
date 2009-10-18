/*
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat;

/**
 * <code>TaskFactory</code> class
 *
 * @author Matvey Kazakov
 */
public class TaskFactory {
    public static final int ACTION_START = 0;
    public static final int ACTION_DONE = 1;
    public static final int ACTION_FAIL = 2;
    
    
    public static final int TASK_TODO = 0;
    public static final int TASK_REASON = 1;
    public static final int TASK_CONFIRM = 2;
    public static final int TASK_QUESTION = 3;
    
    public static final int VSTATE_DONE = 0;
    public static final int VSTATE_INPROGRESS = 1;
    public static final int VSTATE_NEW = 2;
    public static final int VSTATE_FAIL = 3;
    
    public static TaskResult create(int type) {
        switch (type) {
            case TASK_REASON:
                return new ReasonTaskResult();
            case TASK_TODO:
                return new TodoTaskResult();
            case TASK_CONFIRM:
                return new ConfirmTaskResult();
            case TASK_QUESTION:
                return new QuestionTaskResult();
        }
        return null;
    }
}

