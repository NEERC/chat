package ru.ifmo.neerc.chat.client;

import java.awt.Color;
import java.awt.Font;
import java.util.Date;

import ru.ifmo.neerc.chat.ChatMessage;
import ru.ifmo.neerc.chat.user.UserEntry;

public class UserMessage extends AbstractMessage {

    private ChatMessage message;

    public UserMessage(ChatMessage message) {
        this.message = message;
    }

    @Override
    public String getChannel() {
        if (message.getType() == ChatMessage.Type.channel) {
            return message.getTo();
        }

        return super.getChannel();
    }

    @Override
    public Date getDate() {
        return message.getDate();
    }

    @Override
    public UserEntry getUser() {
        return message.getUser();
    }

    @Override
    public String getText() {
        String text = "";
        if (message.getTo() != null) {
            text += message.getTo() + "> ";
        }
        text += message.getText();
        return text;
    }

    @Override
    public boolean isImportant() {
        return getUser().isPower() &&
               (message.getType() == ChatMessage.Type.info
             || message.getType() == ChatMessage.Type.question
             || message.getType() == ChatMessage.Type.urgent);
    }

    @Override
    public float getScale() {
        if (getUser().isPower() && message.getPriority() > 0) {
            return message.getPriority();
        }

        return super.getScale();
    }

    @Override
    public int getStyle() {
        if (message.getType() == ChatMessage.Type.channel) {
            return super.getStyle();
        }

        if (getUser().isPower() && message.getType() != ChatMessage.Type.normal) {
            return Font.BOLD;
        }

        if (message.getTo() != null) {
            return Font.BOLD;
        }

        return super.getStyle();
    }

    @Override
    public Color getColor() {
        if (getUser().isPower()) {
            switch (message.getType()) {
                case info:
                    return Color.green.darker();
                case question:
                    return Color.blue.darker();
                case urgent:
                    return Color.red;
            }
        }

        if (message.getType() == ChatMessage.Type.channel) {
            return Color.blue;
        }

        if (message.getTo() != null) {
            return new Color(0xFF5767);
        }

        return super.getColor();
    }
}
