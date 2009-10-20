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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows different components of chat log different messages into different log files.
 *
 * @author Matvey Kazakov
 */
public final class ChatLogger {
    private static final Logger LOG = LoggerFactory.getLogger(ChatLogger.class);
    private static final Logger CHAT = LoggerFactory.getLogger("Chat");

    /**
     * Log informational message. This is system mesasge, but usually does not mean a problem.
     *
     * @param message message to print
     */
    public static void logInfo(String message) {
        LOG.info(message);
    }

    /**
     * Log debug message. This is inner debug messages.
     *
     * @param message message to print
     */
    public static void logDebug(String message) {
        LOG.debug(message);
    }

    /**
     * Log chat message. This is just chat message from someone. This method allows to trace all chat activity.
     *
     * @param message message to print
     */
    public static void logChat(String message) {
        CHAT.info(message);
    }

    /**
     * Log error message. Ths message means serious problem like esception or whatever.
     *
     * @param message message to print
     */
    public static void logError(String message) {
        LOG.error(message);
    }
}
