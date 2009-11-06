package ru.ifmo.neerc.task;

/**
 * @author Evgeny Mandrikov
 */
public class TaskActions {
    public static final String TYPE_TODO = "todo";
    public static final String TYPE_CONFIRM = "confirm";
    public static final String TYPE_REASON = "okfail";
    public static final String TYPE_QUESTION = "question";

    public static final String STATUS_NEW = "none";
    public static final String STATUS_ACK = "acknowledged";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";

    public static final int ACTION_START = 0;
    public static final int ACTION_DONE = 1;
    public static final int ACTION_FAIL = 2;

    public static boolean isActionSupported(Task task, String user, int action) {
        if (task == null) {
            return false;
        }
        String type = task.getType();
        TaskStatus status = task.getStatuses().get(user);
        if (TYPE_TODO.equals(type)) {
            if (STATUS_RUNNING.equals(status.getType())) {
                return action == ACTION_DONE;
            } else {
                return action == ACTION_START;
            }
        } else if (TYPE_CONFIRM.equals(type)) {
            return action == ACTION_DONE;
        } else if (TYPE_REASON.equals(type)) {
            return action == ACTION_DONE || action == ACTION_FAIL;
        } else if (TYPE_QUESTION.equals(type)) {
            return action == ACTION_DONE;
        }
        return false;
    }

    public static String getNewStatus(Task task, String user, int action) {
        String type = task.getType();
        String status = task.getStatuses().get(user).getType();
        if (action == ACTION_START) {
            return STATUS_RUNNING;
        }
        if (action == ACTION_DONE) {
            return STATUS_SUCCESS.equals(status) ? STATUS_ACK : STATUS_SUCCESS;
        }
        if (action == ACTION_FAIL) {
            return STATUS_FAIL;
        }
        throw new IllegalArgumentException();
    }
}
