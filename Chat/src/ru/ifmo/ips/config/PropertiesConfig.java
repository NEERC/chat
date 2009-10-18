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

