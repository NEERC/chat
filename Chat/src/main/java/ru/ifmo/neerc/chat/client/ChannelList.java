package ru.ifmo.neerc.chat.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChannelList {
    private Set<String> channels = new HashSet<>();
    private List<SubscriptionListener> listeners = new ArrayList<>();

    public void addListener(SubscriptionListener listener) {
        listeners.add(listener);
    }

    private void notifySubscriptionChanged() {
        for (SubscriptionListener listener : listeners)
            listener.subscriptionChanged();
    }

    public void subscribeTo(String channel) {
        channels.add(channel);
        notifySubscriptionChanged();
    }

    public void unsubscribeFrom(String channel) {
        channels.remove(channel);
        notifySubscriptionChanged();
    }

    public boolean isSubscribed(String channel) {
        return channels.contains(channel);
    }

    public String toString() {
        if (channels.size() == 0) return "no subscriptions";
        return "subscriptions: " + Arrays.toString(channels.toArray()).replaceAll("[\\[\\]]", "");
    }
}
