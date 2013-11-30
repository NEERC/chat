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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matvey Kazakov
 */
public class ChatMessage implements Comparable<ChatMessage> {
    public static final String LOG_TIME_FORMAT = "HH:mm:ss";
    public static final String PRIVATE_FIND_REGEX = "^([a-zA-Z0-9%]+)>";
    public static final String CHANNEL_MATCH_REGEX = "(%\\w+)";

    public static enum Type {
        SERVER_MESSAGE,
        USER_MESSAGE,
        TASK_MESSAGE,
    }

    private Type type;
    private String text;
    private UserEntry user;
    private boolean priv;
    private String to;
    private Date timestamp;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private boolean special;

    private ChatMessage(Type type, String text, UserEntry user, String to, boolean special, boolean priv, Date timestamp) {
        this.special = special;
        this.type = type;
        this.text = text;
        this.user = user;
        this.priv = priv;
        this.to = to;
        this.timestamp = timestamp == null ? new Date() : timestamp;
    }

    public static ChatMessage createServerMessage(String text) {
        return new ChatMessage(Type.SERVER_MESSAGE, text, null, "", false, false, null);
    }

    public static ChatMessage createTaskMessage(String text, Date timestamp) {
        return new ChatMessage(Type.TASK_MESSAGE, text, null, "", true, false, timestamp);
    }

    public static ChatMessage createUserMessage(UserMessage userMessage) {
        UserEntry user = UserRegistry.getInstance().findOrRegister(userMessage.getJid());
        String text = userMessage.getText();

        // check if private
        String to = "";
        boolean priv = false;
        Matcher matcher = Pattern.compile(PRIVATE_FIND_REGEX + ".*", Pattern.DOTALL).matcher(text);
        if (matcher.matches()) {
            to = matcher.group(1);
            priv = true;
        }

        return new ChatMessage(
                Type.USER_MESSAGE,
                text,
                user,
                to,
                userMessage.isImportant(),
                priv,
                userMessage.getTimestamp()
        );
    }

    public Type getType() {
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

    public String getTo() {
        return to;
    }

    public long getTimestamp() {
        return timestamp.getTime();
    }

    public synchronized String getTime() {
        return dateFormat.format(timestamp);
    }

    public String log() {
        StringBuilder result = new StringBuilder(new SimpleDateFormat(LOG_TIME_FORMAT).format(timestamp)).append(": ");
        String line = text.replaceAll("\r?\n", "\t");
        switch (type) {
            case SERVER_MESSAGE:
                result.append(">>>>>>>>>>>>").append(line).append("<<<<<<<<<<<<<");
                break;
            case TASK_MESSAGE:
                result.append("!!!!!!!!!!!!").append(line);
                break;
            case USER_MESSAGE:
                result.append(user).append("> ").append(line);
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

    public boolean equals(Object o) {
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage msg = (ChatMessage) o;
        return text.equals(msg.getText()) && getTimestamp() == msg.getTimestamp();
    }

    public int compareTo(ChatMessage msg) {
        long t = getTimestamp() - msg.getTimestamp();
        if (t == 0) return 0;
        return t > 0 ? 1 : -1;
    }
    
    public String toString() {
        return text;
    }
}
