package ru.ifmo.neerc.chat.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChannelList {
    private Set<String> channels = new HashSet<>();

    public void subscribeTo(String channel) {
        channels.add(channel);
    }

    public void unsubscribeFrom(String channel) {
        channels.remove(channel);
    }

    public boolean isSubscribed(String channel) {
        return channels.contains(channel);
    }

    public String toString() {
        if (channels.size() == 0) return "no subscriptions";
        return "subscriptions: " + Arrays.toString(channels.toArray()).replaceAll("[\\[\\]]", "");
    }
}
