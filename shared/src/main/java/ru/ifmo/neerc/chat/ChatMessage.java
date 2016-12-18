package ru.ifmo.neerc.chat;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ifmo.neerc.chat.user.UserEntry;

public class ChatMessage implements Comparable<ChatMessage>, Serializable {
    private static final Pattern MESSAGE_PATTERN =
        Pattern.compile("^(?:([a-zA-Z0-9%]+)>)? *(#+|\\?+|!+)?(.*)$", Pattern.DOTALL);

    private String text;
    private UserEntry user;
    private String to;
    private Date date;
    private Type type;
    private int priority;

    public ChatMessage(String text) {
        this(text, null);
    }

    public ChatMessage(String text, UserEntry user) {
        this(text, user, null, new Date());
    }

    public ChatMessage(String text, UserEntry user, String to, Date date) {
        this.user = user;
        this.to = to;
        this.date = date;
        parseMessage(text);
    }

    public UserEntry getUser() {
        return user;
    }

    public String getTo() {
        return to;
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Type getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }

    public int compareTo(ChatMessage message) {
        return getDate().compareTo(message.getDate());
    }

    protected void parseMessage(String text) {
        Matcher matcher = MESSAGE_PATTERN.matcher(text);
        if (!matcher.matches()) {
            this.text = text;
            return;
        }

        this.text = matcher.group(3);

        if (matcher.group(1) != null) {
            to = matcher.group(1);
            if (to.charAt(0) == '%') {
                type = Type.channel;
                return;
            }
        }
        
        String typeString = matcher.group(2);
        if (typeString != null) {
            switch (typeString.charAt(0)) {
            case '#':
                type = Type.info;
                break;
            case '?':
                type = Type.question;
                break;
            case '!':
                type = Type.urgent;
                break;
            }

            priority = typeString.length();
        } else {
            type = Type.normal;
        }
    }

    public static enum Type {
        normal,
        info,
        question,
        urgent,
        channel;
    }
}
