package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.user.UserEntry;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class NameColorizer {
    private boolean isColored;
    private Random generator = new Random();
    private HashMap<String, Color> mapping = new HashMap<>();

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
        if (!isColored) mapping.clear();
        isColored = colored;
    }

    public Color generateColor(UserEntry userEntry) {
        if (userEntry == null) return Color.BLACK;

        if (userEntry.isPower()) {
            return Color.RED;
        } else {
            if (!isColored()) return Color.BLACK;
            if (!mapping.containsKey(userEntry.getName())) {
                float hue = (generator.nextInt(31) * 10 + 30) / 360.f;
                mapping.put(userEntry.getName(), Color.getHSBColor(hue, 1.0f, 0.8f));
            }
            return mapping.get(userEntry.getName());
        }
    }
}
