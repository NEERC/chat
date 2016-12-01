package ru.ifmo.neerc.chat.client;

import java.awt.Color;
import java.awt.Font;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ifmo.neerc.chat.user.UserEntry;

public abstract class AbstractMessage implements Message {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String getChannel() {
        return null;
    }

    @Override
    public boolean isImportant() {
        return false;
    }

    @Override
    public float getScale() {
        return 1.0f;
    }

    @Override
    public int getStyle() {
        return Font.PLAIN;
    }

    @Override
    public Color getColor() {
        return Color.black;
    }

    @Override
    public int compareTo(Message message) {
        return getDate().compareTo(message.getDate());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DATE_FORMAT.format(getDate()));
        builder.append(": ");
        if (getUser() != null) {
            builder.append(getUser());
            builder.append("> ");
        }
        builder.append(getText());
        return builder.toString();
    }
}
