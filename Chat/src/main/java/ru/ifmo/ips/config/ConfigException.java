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
/**
 * Date: Sep 13, 2002
 */
package ru.ifmo.ips.config;

import ru.ifmo.ips.IpsRuntimeException;

/**
 * <code>ConfigException</code> class
 *
 * @author Matvey Kazakov
 *
 * <code>ConfigException</code> class is a generic exception thrown by
 * methods of <code>Config</code> interface
 */
public class ConfigException extends IpsRuntimeException
{
    /**
     * Creates <code>ConfigException</code> without any parameners
     */
    public ConfigException()
    {
        super();
    }

    /**
     * Creates <code>ConfigException</code> with specified p_message
     *
     * @param p_message exception message
     */
    public ConfigException(String p_message)
    {
        super(p_message);
    }
}
