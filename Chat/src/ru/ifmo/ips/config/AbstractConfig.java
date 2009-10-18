// $Id$
/**
 * Date: 26.10.2004
 */
package ru.ifmo.ips.config;

/**
 * @author Matvey Kazakov
 */
public abstract class AbstractConfig implements Config{

    public int getInt(String name, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(name, ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getInt(String name) {
        return Integer.parseInt(getProperty(name));
    }
    
    public String getString(String name) {
        return getProperty(name);
    }
    
    public String getString(String name, String defaultValue) {
        return getProperty(name, defaultValue);
    }
    
    public Config createNode(String name) {
        setProperty(name, "");
        return getNode(name);
    }

}

