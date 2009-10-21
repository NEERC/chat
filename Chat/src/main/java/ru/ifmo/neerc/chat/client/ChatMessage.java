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
// $Id: ChatMessage.java,v 1.6 2007/10/28 07:32:12 matvey Exp $
/**
 * Date: 29.10.2004
 */
package ru.ifmo.neerc.chat.client;

import ru.ifmo.neerc.chat.message.UserMessage;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;

/**
 * @author Matvey Kazakov
 */
public class ChatMessage {
    public static final String LOG_TIME_FORMAT = "yyyy.MM.dd HH:mm:ss";

    public static final int SERVER_MESSAGE = 0;
    public static final int USER_MESSAGE = 1;
    public static final int TASK_MESSAGE = 2;

    private int type;
    private String text;
    private UserEntry user;
    private boolean priv;
    private Date timestamp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private boolean special;

    private ChatMessage(int type, String text, UserEntry user, boolean special, boolean priv, Date timestamp) {
        this.special = special;
        this.type = type;
        this.text = text;
        this.user = user;
        this.priv = priv;
        this.timestamp = timestamp == null ? new Date() : timestamp;
    }

    public static ChatMessage createServerMessage(String text) {
        return new ChatMessage(SERVER_MESSAGE, text, null, false, false, null);
    }

    public static ChatMessage createTaskMessage(String text, Date timestamp) {
        return new ChatMessage(TASK_MESSAGE, text, null, true, false, timestamp);
    }

    public static ChatMessage createUserMessage(UserMessage userMessage) {
        UserEntry user = UserRegistry.getInstance().search(userMessage.getFrom());
        String text = userMessage.getText();
        return new ChatMessage(
                USER_MESSAGE,
                text,
                user,
                userMessage.isImportant(),
                userMessage.isPrivate(),
                userMessage.getTimestamp()
        );
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public UserEntry getUser() {
        return user;
    }

    public boolean isPrivate() {
        return priv;
    }

    public synchronized String getTime() {
        return dateFormat.format(timestamp);
    }

    public String log() {
        StringBuilder result = new StringBuilder(new SimpleDateFormat(LOG_TIME_FORMAT).format(new Date())).append(": ");
        switch (type) {
            case SERVER_MESSAGE:
                result.append(">>>>>>>>>>>>").append(text).append("<<<<<<<<<<<<<");
                break;
            case TASK_MESSAGE:
                result.append("!!!!!!!!!!!!").append(text);
                break;
            case USER_MESSAGE:
                result.append(user).append("> ").append(text);
                break;
        }
        return result.toString();
    }

    public String getConvertedMessage() {
        String src = getText().trim();
        if (user.isPower()) {
            if (src.startsWith("#")) {
                return "<html><b><font color=\"green\">" + replaceEnters(src.substring(1)) + "</font></b></html>";
            } else if (src.startsWith("!!!")) {
                return "<html><b><font color=\"red\" size=\"+10\">" + replaceEnters(src.substring(3)) + "</font></b></html>";
            } else if (src.startsWith("!!")) {
                return "<html><b><font color=\"red\" size=\"+5\">" + replaceEnters(src.substring(2)) + "</font></b></html>";
            } else if (src.startsWith("!")) {
                return "<html><b><font color=\"red\">" + replaceEnters(src.substring(1)) + "</font></b></html>";
            }
        }
        return "<html>" + replaceEnters(replaceHTML(src)) + "</html>";
    }

    private static String replaceHTML(String src) {
        StringCharacterIterator iterator = new StringCharacterIterator(src);
        StringBuilder result = new StringBuilder();
        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(c);
                    break;
            }
        }
        return result.toString();
    }

    private static String replaceEnters(String src) {
        StringCharacterIterator iterator = new StringCharacterIterator(src);
        StringBuilder result = new StringBuilder();
        for (char c = iterator.first(); c != CharacterIterator.DONE; c = iterator.next()) {
            if (c == '\n') {
                result.append("<br>");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public boolean isSpecial() {
        return special;
    }
}
