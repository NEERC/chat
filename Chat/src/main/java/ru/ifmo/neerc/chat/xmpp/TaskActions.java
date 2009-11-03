package ru.ifmo.neerc.chat.xmpp;

import ru.ifmo.neerc.chat.task.TaskFactory;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;

/**
 * @author Evgeny Mandrikov
 */
public class TaskActions {
    public static final String TYPE_TODO = "todo";
    public static final String TYPE_CONFIRM = "confirm";
    public static final String TYPE_REASON = "reason";
    public static final String TYPE_QUESTION = "question";

    public static boolean isActionSupported(Task task, String user, int action) {
        if (task == null) {
            return false;
        }
        String type = task.getType();
        TaskStatus status = task.getStatuses().get(user);
        if (TYPE_TODO.equals(type)) {
            if ("inProgress".equals(status.getType())) {
                return action == TaskFactory.ACTION_DONE;
            } else {
                return action == TaskFactory.ACTION_START;
            }
        } else if (TYPE_CONFIRM.equals(type)) {
            return action == TaskFactory.ACTION_DONE;
        } else if (TYPE_REASON.equals(type)) {
            return action == TaskFactory.ACTION_DONE || action == TaskFactory.ACTION_FAIL;
        }/* else if (TYPE_QUESTION.equals(type)) {
            return action == TaskFactory.ACTION_DONE;
        }*/
        return false;
    }

}
