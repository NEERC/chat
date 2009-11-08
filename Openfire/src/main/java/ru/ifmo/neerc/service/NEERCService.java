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
package ru.ifmo.neerc.service;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.util.*;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import ru.ifmo.neerc.service.NEERCComponent;

import java.io.File;

/**
 * @author Dmitriy Trofimov
 */
public class NEERCService implements Plugin {

    private String serviceName = "neerc";
    private ComponentManager componentManager;
    private NEERCComponent component;

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        componentManager = ComponentManagerFactory.getComponentManager();
        component = new NEERCComponent();
        try {
            componentManager.addComponent(serviceName, component);
        } catch (Exception e) {
            componentManager.getLog().error(e);
        }
        componentManager.getLog().debug("neerc service started");
    }

    public void destroyPlugin() {
        if (componentManager != null) {
            try {
                componentManager.removeComponent(serviceName);
            } catch (Exception e) {
                componentManager.getLog().error(e);
            }
	        componentManager.getLog().debug("neerc service stopped");
        }
        component = null;
        componentManager = null;
    }
}
