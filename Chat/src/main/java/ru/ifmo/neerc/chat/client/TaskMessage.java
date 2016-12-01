package ru.ifmo.neerc.chat.client;

import java.awt.Color;
import java.awt.Font;
import java.util.Date;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.task.Task;

public class TaskMessage extends AbstractMessage {

    private Task task;

    public TaskMessage(Task task) {
        this.task = task;
    }

    @Override
    public Date getDate() {
        return task.getDate();
    }

    @Override
    public UserEntry getUser() {
        return null;
    }

    @Override
    public String getText() {
        return "!!! New task '" + task.getTitle() + "' has been assigned to you !!!";
    }

    @Override
    public boolean isImportant() {
        return true;
    }

    @Override
    public float getScale() {
        return 2.0f;
    }

    @Override
    public int getStyle() {
        return Font.BOLD;
    }

    @Override
    public Color getColor() {
        return Color.red;
    }
}
