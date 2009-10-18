/*
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.ips.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>ConfigKeeper</code> class
 *
 * @author Matvey Kazakov
 */
public class ConfigKeeper {

    private List<ConfigListener> listeners = new ArrayList<ConfigListener>();
    private ConfigChecker checkerThread;
    private Config config;

    public ConfigKeeper(String configName, boolean notify) throws FileNotFoundException {

        if (notify) {
            checkerThread = new ConfigChecker(configName);
            checkerThread.start();
        }
        config = new XMLConfig(configName);
    }

    public void addListener(ConfigListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(ConfigListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public Config getConfig() {
        return config;
    }

    private class ConfigChecker extends Thread {
        private File configFile;
        private long configLastModified;
        private String fileName;

        private ConfigChecker(String fileName) {
            super("Config checker for " + fileName);
            this.fileName = fileName;
            this.configFile = new File(this.fileName);
            setDaemon(true);
        }

        public void run() {

            while (true) {
                synchronized (this) {
                    long modified = configFile.lastModified();
                    if (modified > configLastModified) {
                        configLastModified = modified;
                        try {
                            config = new XMLConfig(configFile.getName());
                            synchronized (listeners) {
                                for (ConfigListener listener : listeners) {
                                    listener.configChanged(fileName);
                                }
                            }
                        } catch (Exception e) {
                            // skip - old config remains
                        }
                    }
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }

    }
}

