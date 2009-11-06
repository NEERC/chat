package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.task.*;

/**
 * @author Evgeny Mandrikov
 */
public interface Chat {
    void write(Message message);
    void write(Task task, TaskStatus status);
}
