/*
 * Date: Oct 24, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.client;

import ru.ifmo.ips.IpsRuntimeException;

/**
 * <code>UserUnregisteredException</code> class
 *
 * @author Matvey Kazakov
 */
public class UserUnregisteredException extends IpsRuntimeException{

    public UserUnregisteredException(String message) {
        super(message);
    }

    public UserUnregisteredException() {
    }

    public UserUnregisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserUnregisteredException(Throwable cause) {
        super(cause);
    }
}

