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
// $Id: ChatClient.java,v 1.13 2007/10/28 07:32:12 matvey Exp $
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.xmpp;

import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;
import ru.ifmo.neerc.chat.client.AbstractChatClient;
import ru.ifmo.neerc.chat.message.Message;

import java.awt.*;
import java.io.FileNotFoundException;

/**
 * Fork of {@link ru.ifmo.neerc.chat.client.ChatClient}.
 *
 * @author Evgeny Mandrikov
 */
public class XmppChatClient extends AbstractChatClient {

    public XmppChatClient() throws HeadlessException, FileNotFoundException {
        final String name = System.getProperty("username");

        user = new UserEntry(0, name, false); // TODO power
        UserRegistry.getInstance().register(user);
        UserRegistry.getInstance().putOnline(user, true);

        chat = new XmppChat(name, new ChatMessageListener());

        setupUI();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new XmppChatClient().setVisible(true);
    }

    private class ChatMessageListener extends XmppAdapter {
        @Override
        public void processMessage(Message message) {
            XmppChatClient.this.processMessage(message);
        }
    }
}