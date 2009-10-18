/*
 * Date: Nov 17, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin;

import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.ips.config.Config;

/**
 * <code>CustomMessage</code> class
 *
 * @author Matvey Kazakov
 */
public class CustomMessage extends Message {
    
    public CustomMessageData data;
    private static final String ATT_DATATYPE = "@datatype";
    private static final String NODE_DATA = "data";

    public CustomMessage(int destination, CustomMessageData data) {
        super(CUSTOM_MESSAGE, destination);        
        this.data = data;
    }

    public CustomMessage() {
        super(CUSTOM_MESSAGE);
    }

    protected void serialize(Config message) {
        message.setProperty(ATT_DATATYPE, data.getDataType());
        if (data != null) {
            data.serialize(message.createNode(NODE_DATA));
        }
    }

    public String asString() {
        return "Custom message of type " + (data == null ? "<unknown>" : data.getDataType()) + " to " + getDestination() + ": " + data.asString();
    }

    protected void deserialize(Config message) {
        String dataType = message.getProperty(ATT_DATATYPE);
        data = CustomDataFactory.createCustomData(dataType);
        if (data != null) {
            data.deserialize(message.getNode(NODE_DATA));
        }
    }

    public CustomMessageData getData() {
        return data;
    }
}

