package ru.ifmo.neerc.chat.utils;

import java.util.Date;

/**
 * @author Evgeny Mandrikov
 */
public final class DateUtils {

    public static final long MINUTE = 60 * 1000;

    /**
     * Hide utility class contructor.
     */
    private DateUtils() {
    }

    public static long getTimeDifference(Date date) {
        return (new Date()).getTime() - date.getTime();
    }

}
