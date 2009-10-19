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
