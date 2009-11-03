package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.task.TaskFactory;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeny Mandrikov
 */
public final class TaskIcon {
    public static final Map<String, ImageIcon> STATUS = new HashMap<String, ImageIcon>();
    public static final Map<Integer, ImageIcon> ACTION = new HashMap<Integer, ImageIcon>();
    public static final Map<Integer, ImageIcon> TYPE = new HashMap<Integer, ImageIcon>();

    public static final ImageIcon iconTaskAssign = new ImageIcon(AdminTaskPanel.class.getResource("res/task_assign.gif"));
    public static final ImageIcon iconTaskRemove = new ImageIcon(AdminTaskPanel.class.getResource("res/task_remove.gif"));
    public static final ImageIcon iconTaskImport = new ImageIcon(AdminTaskPanel.class.getResource("res/task_import.png"));

    static {
        STATUS.put(
                "done",
                new ImageIcon(TaskList.class.getResource("res/task_state_done.gif"))
        );
        STATUS.put(
                "fail",
                new ImageIcon(TaskList.class.getResource("res/task_state_fail.png"))
        );
        STATUS.put(
                "inProgress",
                new ImageIcon(TaskList.class.getResource("res/task_state_inprogress.png"))
        );
        STATUS.put(
                "new",
                new ImageIcon(TaskIcon.class.getResource("res/task_state_new.gif"))
        );

        ACTION.put(
                TaskFactory.ACTION_DONE,
                new ImageIcon(TaskPanel.class.getResource("res/task_action_complete.gif"))
        );
        ACTION.put(
                TaskFactory.ACTION_FAIL,
                new ImageIcon(TaskPanel.class.getResource("res/task_action_fail.png"))
        );
        ACTION.put(
                TaskFactory.ACTION_START,
                new ImageIcon(TaskPanel.class.getResource("res/task_action_start.png"))
        );

        TYPE.put(
                TaskFactory.TASK_TODO,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_todo.png"))
        );
        TYPE.put(
                TaskFactory.TASK_CONFIRM,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_confirm.png"))
        );
        TYPE.put(
                TaskFactory.TASK_REASON,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_text.png"))
        );
        TYPE.put(
                TaskFactory.TASK_QUESTION,
                new ImageIcon(AdminTaskPanel.class.getResource("res/task_add_quest.png"))
        );
    }

    /**
     * Hide utility class contructor.
     */
    private TaskIcon() {
    }
}
