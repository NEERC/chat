// $Id$
/**
 * Date: 26.10.2004
 */
package ru.ifmo.ips;

/**
 * @author Matvey Kazakov
 */
public class IpsRuntimeException extends RuntimeException {
    public IpsRuntimeException(String message) {
        super(message);
    }

    public IpsRuntimeException() {
    }

    public IpsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IpsRuntimeException(Throwable cause) {
        super(cause);
    }

}

