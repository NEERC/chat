package ru.ifmo.neerc.chat.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChannelList {
    private AbstractChatClient client;
    private Set<String> channels = new HashSet<>();
    private Map<String, ChatWindow> windows = new HashMap<>();
    private List<SubscriptionListener> listeners = new ArrayList<>();
    private boolean isSeparated = false;

    public ChannelList(AbstractChatClient client) {
        this.client = client;
    }

    public void addListener(SubscriptionListener listener) {
        listeners.add(listener);
    }

    private void notifySubscriptionChanged() {
        for (SubscriptionListener listener : listeners)
            listener.subscriptionChanged();
    }

    public void subscribeTo(String channel) {
        ChatWindow window = getOrCreateWindow(channel);
        if (isSeparated)
            window.setVisible(true);
        channels.add(channel);
        notifySubscriptionChanged();
    }

    public void unsubscribeFrom(String channel) {
        ChatWindow window = getOrCreateWindow(channel);
        if (isSeparated)
            window.setVisible(false);
        channels.remove(channel);
        notifySubscriptionChanged();
    }

    public boolean isSubscribed(String channel) {
        return channels.contains(channel);
    }

    public boolean isSeparated() {
        return isSeparated;
    }

    private ChatWindow getOrCreateWindow(final String channel) {
        if (!windows.containsKey(channel)) {
            ChatWindow window = new ChatWindow(client, channel);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    unsubscribeFrom(channel);
                }
            });
            window.setVisible(isSeparated);
            windows.put(channel, window);
            notifySubscriptionChanged();
        }

        return windows.get(channel);
    }

    public void showMessage(Message message) {
        if (message.getChannel() == null)
            return;

        getOrCreateWindow(message.getChannel()).addMessage(message);
    }

    public void setSeparated(boolean isSeparated) {
        this.isSeparated = isSeparated;
        if (isSeparated) {
            for (String channel : channels)
                getOrCreateWindow(channel).setVisible(true);
        } else {
            for (ChatWindow window : windows.values())
                window.setVisible(false);
        }
        notifySubscriptionChanged();
    }

    public Set<String> getChannels() {
        return Collections.unmodifiableSet(windows.keySet());
    }

    public String toString() {
        if (channels.size() == 0) return "no subscriptions";
        return "subscriptions: " + Arrays.toString(channels.toArray()).replaceAll("[\\[\\]]", "");
    }
}
