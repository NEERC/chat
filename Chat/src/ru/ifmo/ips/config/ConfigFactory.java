/*
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.ips.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.ArrayList;

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

