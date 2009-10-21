package ru.ifmo.neerc.chat.utils;

import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.packet.MUCUser;

/**
 * @author Evgeny Mandrikov
 */
public class DebugUtils {
    /**
     * Hide utility class contructor.
     */
    private DebugUtils() {
    }

    public static String occupantToString(Occupant occupant) {
        return new StringBuilder()
                .append("Occupant[")
                .append(" JID=").append(occupant.getJid())
                .append(" Nick=").append(occupant.getNick())
                .append(" Role=").append(occupant.getRole())
                .append(" Affiliation=").append(occupant.getAffiliation())
                .append("]")
                .toString();
    }

    public static String userItemToString(MUCUser.Item item) {
        return new StringBuilder()
                .append("MUCUser.Item[")
                .append(" JID=").append(item.getJid())
                .append(" Nick=").append(item.getNick())
                .append(" Role=").append(item.getRole())
                .append(" Affiliation=").append(item.getAffiliation())
                .append(" Reason=").append(item.getReason())
                .append(" Actor=").append(item.getActor())
                .append("]")
                .toString();
    }
}
