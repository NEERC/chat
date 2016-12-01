package ru.ifmo.neerc.chat.client;

import java.awt.Color;
import java.awt.Font;
import java.util.Date;

import ru.ifmo.neerc.chat.user.UserEntry;

public class StatusMessage extends AbstractMessage {

    private Date date;
    private String text;

    public StatusMessage(String text) {
        this(text, new Date());
    }

    public StatusMessage(String text, Date date) {
        this.text = text;
        this.date = date;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public UserEntry getUser() {
        return null;
    }

    @Override
    public String getText() {
        return ">>>>>   " + text + "   <<<<<";
    }

    @Override
    public int getStyle() {
        return Font.BOLD;
    }

    @Override
    public Color getColor() {
        return Color.blue;
    }
}
