package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.task.TaskActions;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeny Mandrikov
 */
public final class TaskIcon {
    public static final Map<String, ImageIcon> STATUS = new HashMap<String, ImageIcon>();
    public static final Map<Integer, ImageIcon> ACTION = new HashMap<Integer, ImageIcon>();
    public static final Map<String, ImageIcon> TYPE = new HashMap<String, ImageIcon>();

    public static final ImageIcon iconTaskAssign = new ImageIcon(AdminTaskPanel.class.getResource("res/task_assign.gif"));
    public static final ImageIcon iconTaskRemove = new ImageIcon(AdminTaskPanel.class.getResource("res/task_remove.gif"));
    public static final ImageIcon iconTaskImport = new ImageIcon(AdminTaskPanel.class.getResource("res/task_import.png"));

    static {
        STATUS.put(
                TaskActions.STATUS_SUCCESS,
                new ImageIcon(TaskIcon.class.getResource("res/task_state_done.gif"))
        );
        STATUS.put(
                TaskActions.STATUS_FAIL,
                new ImageIcon(TaskIcon.class.getResource("res/task_state_fail.png"))
        );
        STATUS.put(
                TaskActions.STATUS_RUNNING,
                new ImageIcon(TaskIcon.class.getResource("res/task_state_inprogress.png"))
        );
        STATUS.put(
                TaskActions.STATUS_NEW,
                new ImageIcon(TaskIcon.class.getResource("res/task_state_new.gif"))
        );
        STATUS.put(
                TaskActions.STATUS_ACK,
                new ImageIcon(TaskIcon.class.getResource("res/task_state_new.gif"))
        );

        ACTION.put(
                TaskActions.ACTION_DONE,
                new ImageIcon(TaskIcon.class.getResource("res/task_action_complete.gif"))
        );
        ACTION.put(
                TaskActions.ACTION_FAIL,
                new ImageIcon(TaskIcon.class.getResource("res/task_action_fail.png"))
        );
        ACTION.put(
                TaskActions.ACTION_START,
                new ImageIcon(TaskIcon.class.getResource("res/task_action_start.png"))
        );

        TYPE.put(
                TaskActions.TYPE_TODO,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_todo.png"))
        );
        TYPE.put(
                TaskActions.TYPE_CONFIRM,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_confirm.png"))
        );
        TYPE.put(
                TaskActions.TYPE_REASON,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_text.png"))
        );
        TYPE.put(
                TaskActions.TYPE_QUESTION,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_quest.png"))
        );
    }

    /**
     * Hide utility class contructor.
     */
    private TaskIcon() {
    }
}
