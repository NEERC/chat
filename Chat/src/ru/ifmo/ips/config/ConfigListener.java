/*
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.ips.config;

/**
 * <code>ConfigListener</code> interface
 *
 * @author Matvey Kazakov
 */
public interface ConfigListener {
    
    void configChanged(String configName);
}

