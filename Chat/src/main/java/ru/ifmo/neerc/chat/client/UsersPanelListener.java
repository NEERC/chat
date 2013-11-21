package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.user.UserEntry;

public interface UsersPanelListener {
    void userClicked(UserEntry user);
}
