package ru.ifmo.neerc.chat;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractChat implements Chat {

    protected final Set<ChatListener> listeners = new CopyOnWriteArraySet<ChatListener>();

    public void addListener(ChatListener listener) {
        listeners.add(listener);
    }
}
