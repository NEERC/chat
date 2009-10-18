/*
 * Date: Nov 18, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin;

import ru.ifmo.neerc.chat.MessageListener;

import javax.swing.*;

/**
 * <code>ChatPlugin</code> interface
 *
 * @author Matvey Kazakov
 */
public interface ChatPlugin {
    
    void init(MessageListener listener, int userId, JComponent parent);
    
    Class<CustomMessageData> getCustomDataClass();

    boolean accept(Class<? extends CustomMessageData> aClass);
    
    /**
     * Method is called when message is received.
     * @param message message received
     */
    void processMessage(CustomMessage message);
    
    Icon getIcon();
    
    void start();
    
}

