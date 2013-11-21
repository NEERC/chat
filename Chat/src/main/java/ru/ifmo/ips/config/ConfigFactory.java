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
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.ips.config;

import java.io.FileNotFoundException;

/**
 * Stores global configuration instance
 *
 * @author Matvey Kazakov
 */
public final class ConfigFactory{
    
    private static ConfigKeeper globalConfig;

    /**
     * Returns global config instance
     * @return config that is previously set
     * @throws IllegalStateException if config is not set
     * @see #setConfig(Config) 
     */
    public static Config getMainConfig() {
        if (globalConfig == null) {
            throw new IllegalStateException("Config is not initialized");
        }
        return globalConfig.getConfig();
    }

    public static void setConfig(String newConfig) throws FileNotFoundException {
        setConfig(newConfig, false);
    }
    
    public static void setConfig(String newConfig, boolean notify) throws FileNotFoundException {
        globalConfig = new ConfigKeeper(newConfig, notify);
    }

    public static void addListener(ConfigListener listener) {
        globalConfig.addListener(listener);
    }

    public static void removeListener(ConfigListener listener) {
        globalConfig.removeListener(listener);
    }

}
