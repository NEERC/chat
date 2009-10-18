// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat;

/**
 * @author Matvey Kazakov
 */
public interface UserRegistryListener {
    void userAdded(UserEntry userEntry);
    void userRemoved(UserEntry userEntry);
    void userChanged(UserEntry userEntry);
}

