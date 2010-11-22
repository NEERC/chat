package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.user.UserEntry;

import java.util.*;

public interface UsersPanelListener {
    void userClicked(UserEntry user);
}
