/*
   Copyright 2009 NEERC team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
// $Id$
/**
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.ConfigException;
import ru.ifmo.ips.config.XMLConfig;
import ru.ifmo.neerc.chat.ChatLogger;

import java.io.*;

/**
 * @author Matvey Kazakov
 */
public class MessageFactory {
    private static MessageFactory instance = new MessageFactory();
    private static final String ATTR_TYPE = "@type";
    private static final String ATTR_DEST = "@dest";

    public MessageFactory() {
    }

    public byte[] serialize(Message message) {
        byte[] serialized = message.getSerialized();
        try {
            if (serialized == null) {
                XMLConfig messageXml = XMLConfig.createEmptyConfig("message");
                messageXml.setProperty(ATTR_TYPE, String.valueOf(message.getType()));
                messageXml.setProperty(ATTR_DEST, String.valueOf(message.getDestination()));
                message.serialize(messageXml);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(out);
                messageXml.writeCompactConfig(writer);
                writer.close();
                out.write(0);
                serialized = out.toByteArray();
                message.setSerialized(serialized);
            }
        } catch (Exception e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(0);
            serialized = out.toByteArray();
        }
        return serialized;
    }

    public Message deserialize(byte[] in) {
        XMLConfig messageXml;
        try {
            messageXml = new XMLConfig(new InputStreamReader(new ByteArrayInputStream(in)));
        } catch (ConfigException e) {
            ChatLogger.LOG.error("Error parsing message: " + new String(in), e);
            throw e;
        }
        int messageType = messageXml.getInt(ATTR_TYPE, 0);
        int destination = messageXml.getInt(ATTR_DEST, 0);
        Message message = createMessage(messageType);
        message.setDestination(destination);
        message.deserialize(messageXml);
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(in);
            byteArrayOutputStream.write(0);
            message.setSerialized(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Creates instance of message.
     *
     * @param messageType type of message to be created
     * @return newly created instance or <code>NULL</code> if message type not supported.
     */
    private Message createMessage(int messageType) {
        Message message = null;
        switch (messageType) {
            case Message.SERVER_MESSAGE:
                message = new ServerMessage();
                break;
            case Message.USER_MESSAGE:
                message = new UserMessage();
                break;
            case Message.TASK_MESSAGE:
                message = new TaskMessage();
                break;
        }
        return message;
    }

    public static MessageFactory getInstance() {
        return instance;
    }

}
