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

