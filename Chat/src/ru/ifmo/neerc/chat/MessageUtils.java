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
package ru.ifmo.neerc.chat;

import ru.ifmo.neerc.chat.message.Message;
import ru.ifmo.neerc.chat.message.MessageFactory;
import ru.ifmo.neerc.chat.message.EOFMessage;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * @author Matvey Kazakov
 */
public class MessageUtils {
    public static Message getMessage(InputStream in) {
        try {
            ByteArrayOutputStream messageBuffer = new ByteArrayOutputStream();
            while (true) {
                int i = in.read();
                if (i == -1) {
                    return EOFMessage.instance;
                }
                if (i == 0) {
                    break;
                }
                messageBuffer.write(i);
            }
            return MessageFactory.getInstance().deserialize(messageBuffer.toByteArray());
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            return EOFMessage.instance;
        }
    }
}

