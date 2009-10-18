/*
 * Date: Nov 18, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin;

import ru.ifmo.ips.config.Config;

/**
 * <code>CustomMessageData</code> interface
 *
 * @author Matvey Kazakov
 */
public interface CustomMessageData {
    
    String getDataType();
    
    void serialize(Config cfg);
    
    void deserialize(Config cfg);
    
    String asString();
}

