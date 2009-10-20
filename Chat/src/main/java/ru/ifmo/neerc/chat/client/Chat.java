package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.message.Message;

/**
 * @author Evgeny Mandrikov
 */
public interface Chat {
    void write(Message message);
}
