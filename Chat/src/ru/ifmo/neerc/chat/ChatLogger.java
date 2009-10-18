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
 * Date: 17.09.2005
 */
package ru.ifmo.neerc.chat;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Allows different components of chat log different messages into different log files.
 *
 * @author Matvey Kazakov
 */
public final class ChatLogger {

    public static final String dateFormat = "yyyy.MM.dd HH:mm:ss: ";

    private static File logFile = new File("info.txt");
    private static File chatFile = new File("log.txt");
    private static File errorFile = new File("error.txt");
    private static File debugFile = new File("debug.txt");
    
    private static final String INFO = "INF ";
    private static final String DEBUG = "DBG ";
    private static final String ERROR = "ERR ";
    private static final String CHAT = "LOG ";

    /**
     * Log informational message. This is system mesasge, but usually does not mean a problem.
     *
     * @param message message to print
     */
    public static final void logInfo(String message) {
        writeMessage(message, logFile, INFO);
    }

    /**
     * Log debug message. This is inner debug messages.
     *
     * @param message message to print
     */
    public static final void logDebug(String message) {
        writeMessage(message, debugFile, DEBUG);
    }

    /**
     * Log chat message. This is just chat message from someone. This method allows to trace all chat activity.
     *
     * @param message message to print
     */
    public static final void logChat(String message) {
        writeMessage(message, chatFile, CHAT);
    }

    /**
     * Log error message. Ths messagemeans serious problem like esception or whatever.
     *
     * @param message message to print
     */
    public static final void logError(String message) {
        writeMessage(message, errorFile, ERROR);
    }

    private static String getDate() {
        return new SimpleDateFormat(dateFormat).format(new Date());
    }

    private static void writeMessage(String message, File file, String type) {
        synchronized (file) {
            String msgToPrint = getDate() + message;
            try {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
                writer.println(getDate() + msgToPrint);
                writer.flush();
                writer.close();
            } catch (IOException e) {
            }
            System.out.println(getDate() + type + message);
        }
    }
}

