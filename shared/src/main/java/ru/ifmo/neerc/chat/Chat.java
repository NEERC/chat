package ru.ifmo.neerc.chat;

import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;

public interface Chat {

    public void addListener(ChatListener listener);

    public void sendMessage(ChatMessage message);

    public void sendTask(Task task);

    public void sendTaskStatus(Task task, TaskStatus status);
}
