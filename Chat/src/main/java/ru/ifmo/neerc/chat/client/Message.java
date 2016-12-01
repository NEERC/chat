package ru.ifmo.neerc.chat.client;

import java.awt.Color;
import java.util.Date;

import ru.ifmo.neerc.chat.user.UserEntry;

public interface Message extends Comparable<Message> {

    String getChannel();

    Date getDate();

    UserEntry getUser();

    String getText();

    boolean isImportant();

    float getScale();

    int getStyle();

    Color getColor();
}
