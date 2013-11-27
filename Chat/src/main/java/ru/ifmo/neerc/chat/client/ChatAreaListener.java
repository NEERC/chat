package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.user.UserEntry;

public interface ChatAreaListener {
    void userClicked(UserEntry user);
}
