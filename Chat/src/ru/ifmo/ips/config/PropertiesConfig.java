/*
 * Date: Mar 20, 2003
 *
 * $Id$
 */
package ru.ifmo.ips.config;

import ru.ifmo.ips.config.AbstractPropertiesConfig;
import ru.ifmo.ips.config.ConfigException;

import java.io.*;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>PropertiesConfig</code> class
 *
 * @author Matvey Kazakov
 */
public class PropertiesConfig extends AbstractPropertiesConfig {

    public static final String ROOT_NAME= "ROOT";

    public void writeConfig(Writer target) throws ConfigException {
        PrintWriter writer = new PrintWriter(target);
        int rootlen = (ROOT_NAME + PATH_DELIMETER).length();
        Iterator itProperty = properties.entrySet().iterator();
        while (itProperty.hasNext()) {
            Map.Entry property = (Map.Entry)itProperty.next();
            writer.println(((String)property.getKey()).substring(rootlen) + "=" + property.getValue());
        }
    }

    public void readConfigFrom(Reader source) throws ConfigException {
        innerConfig = new InnerConfig(ROOT_NAME);
        elementRoot = ROOT_NAME;
        LineNumberReader reader = new LineNumberReader(source);
        String property = null;
        try {
            while ((property  = reader.readLine()) != null) {
                int pos = property.indexOf("=");
                if (pos >= 0) {
                    setProperty(property.substring(0, pos), property.substring(pos+1));
                }
            }
        } catch (IOException e) {
            throw new ConfigException("Error reading properties");
        }
    }
}

