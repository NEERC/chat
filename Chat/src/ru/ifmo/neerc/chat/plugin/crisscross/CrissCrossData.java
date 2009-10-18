/*
 * Date: Nov 18, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin.crisscross;

import ru.ifmo.neerc.chat.plugin.CustomMessageData;
import ru.ifmo.ips.config.Config;

/**
 * <code>CrissCrossData</code> class
 *
 * @author Matvey Kazakov
 */
public class CrissCrossData implements CustomMessageData {
    
    protected int from;
    protected int type;
    private static final String ATT_FROM = "@from";
    private static final String ATT_TYPE = "@type";
    public static final int WELCOME = 0;
    public static final int ACCEPT = 1;
    public static final int DENY = 2;
    public static final int QUIT = 3;
    public static final int BUSY = 4;
    public static final int TURN = 5;

    public static CrissCrossData createData(int from, int type) {
        CrissCrossData data = new CrissCrossData();
        data.type = type;
        data.from = from;
        return data;
    }

    public String getDataType() {
        return CrissCrossPlugin.DATATYPE;
    }

    public void serialize(Config cfg) {
        cfg.setProperty(ATT_FROM, String.valueOf(from));
        cfg.setProperty(ATT_TYPE, String.valueOf(type));
    }

    public void deserialize(Config cfg) {
        from = cfg.getInt(ATT_FROM);
        type = cfg.getInt(ATT_TYPE);
    }

    public String asString() {
        return "From " + from + "  - " + getType();
    }

    public int getFrom() {
        return from;
    }

    public int getType() {
        return type;
    }
}

