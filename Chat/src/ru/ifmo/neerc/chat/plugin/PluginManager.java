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
/*
 * Date: Nov 18, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin;

import ru.ifmo.ips.config.Config;
import ru.ifmo.neerc.chat.ChatLogger;
import ru.ifmo.neerc.chat.MessageListener;
import ru.ifmo.neerc.chat.message.Message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <code>PluginManager</code> class
 *
 * @author Matvey Kazakov
 */
public class PluginManager implements MessageListener {

    private List<ChatPlugin> plugins = new ArrayList<ChatPlugin>();


    public PluginManager(Config config, MessageListener listener, int userId, JComponent parent) {
        try {
            Config[] pluginCfgs = config.getNodeList("plugin");
            for (int i = 0; i < pluginCfgs.length; i++) {
                Config pluginCfg = pluginCfgs[i];
                String className = pluginCfg.getProperty("@class");
                Class<ChatPlugin> aClass = (Class<ChatPlugin>)Class.forName(className);
                ChatPlugin chatPlugin = aClass.newInstance();
                chatPlugin.init(listener, userId, parent);
                plugins.add(chatPlugin);
            }
        } catch (Exception e) {
            ChatLogger.logError("Could not load plugins: " + e.getMessage());
        }
    }

    public PluginManager(Config config) {
        try {
            Config[] pluginCfgs = config.getNodeList("plugin");
            for (int i = 0; i < pluginCfgs.length; i++) {
                Config pluginCfg = pluginCfgs[i];
                String className = pluginCfg.getProperty("@class");
                Class<ChatPlugin> aClass = (Class<ChatPlugin>)Class.forName(className);
                ChatPlugin chatPlugin = aClass.newInstance();
                plugins.add(chatPlugin);
            }
        } catch (Exception e) {
            ChatLogger.logError("Could not load plugins: " + e.getMessage());
        }
    }

    public List<ChatPlugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public void processMessage(Message message) {

        if (!(message instanceof CustomMessage) || ((CustomMessage)message).getData() == null) {
            // skip this message
            return;
        }
        CustomMessage customMessage = (CustomMessage)message;
        CustomMessageData data = customMessage.getData();
        for (int i = 0; i < plugins.size(); i++) {
            ChatPlugin plugin = plugins.get(i);
            if (plugin.accept(data.getClass())) {
                plugin.processMessage(customMessage);
            }
        }
    }
}

