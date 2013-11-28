package ru.ifmo.neerc.chat.client;

import java.awt.*;

public class NameColorizer {
    private boolean isColored;

    public NameColorizer() {
        this(true);
    }

    public NameColorizer(boolean isColored) {
        this.setColored(isColored);
    }

    public boolean isColored() {
        return isColored;
    }

    public void setColored(boolean colored) {
        isColored = colored;
    }

    public Color generateColor(String name) {
        if (name == null || !isColored()) return Color.BLACK;

        int hash = name.hashCode();
        float hue = ((((hash & 0xff) * ((hash & 0xff00) >> 8)) & 0xff) * 1.0f) / 256.0f;
        return Color.getHSBColor(hue, 0.7f, 0.7f);
    }
}
