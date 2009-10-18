/*
 * Date: Nov 18, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin;

import ru.ifmo.neerc.chat.ChatLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * <code>CustomDataFactory</code> class
 *
 * @author Matvey Kazakov
 */
public class CustomDataFactory {
    
    private static Map<String, Class<? extends CustomMessageData>> customDataTypes = new HashMap<String, Class<? extends CustomMessageData>>();
    
    public static void registerCustomData(String id, Class<? extends CustomMessageData> dataType) {
        customDataTypes.put(id, dataType);
    }
    
    public static void unregisterCustomData(String id) {
        customDataTypes.remove(id);
    }
    
    public static CustomMessageData createCustomData(String id) {
        Class<? extends CustomMessageData> messageDataClass = customDataTypes.get(id);
        if (messageDataClass != null) {
            try {
                return messageDataClass.newInstance();
            } catch (Exception e) {
                ChatLogger.logError("Error creating custom message data: " + e.getMessage());
            }
        }
        throw new RuntimeException("Could not create data of type  " + id);
    }
    
}

