// $Id$
/**
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat;

import java.util.*;

/**
 * @author Matvey Kazakov
 */
public class UserRegistry {

    private Map<Integer, UserEntry> userById = new HashMap<Integer, UserEntry>();
    private Map<String, UserEntry> userByName = new HashMap<String, UserEntry>();

    private Collection<UserRegistryListener> listeners = new ArrayList<UserRegistryListener>();
    
    private static UserRegistry instance = new UserRegistry();

    /**
     * Returns user registry instance
     */
    public static UserRegistry getInstance() {
        return instance;
    }

    /**
     * Hide default constructor
     */
    private UserRegistry() {}

    public synchronized boolean connectUser(UserEntry user) {
        if (userExists(user.getName())) {
            UserEntry oldUserEntry = userByName.get(user.getName());
            if (oldUserEntry.isOnline()) {
                return false;
            }
            oldUserEntry.setOnline(true);
            user.setId(oldUserEntry.getId());
            oldUserEntry.setPower(user.isPower());
            return true;
        } 
        return false;
    }

    public Collection<UserEntry> getUsers() {
        return Collections.unmodifiableCollection(userById.values());
    }

    public synchronized boolean register(UserEntry user) {
        UserEntry oldEntry = userById.put(user.getId(), user);
        if (oldEntry != null) {
            user.setOnline(oldEntry.isOnline());
        }
        userByName.put(user.getName(), user);
        for (Iterator<UserRegistryListener> iterator = listeners.iterator(); iterator.hasNext();) {
            iterator.next().userAdded(user);
        }
        return true;
    }

    public synchronized void putOnline(UserEntry user, boolean online) {
        UserEntry userEntry = search(user.getId());
        if (userEntry == null) {
            return;
        }
        userEntry.setOnline(online);
        for (Iterator<UserRegistryListener> iterator = listeners.iterator(); iterator.hasNext();) {
            iterator.next().userChanged(user);
        }
    }

    public synchronized void init(UserEntry[] list) {
        UserEntry[] entries = serialize();
        Set<Integer> oldEntries = new HashSet<Integer>();
        for (int i = 0; i < entries.length; i++) {
            oldEntries.add(entries[i].getId());
        }
        for (int i = 0; i < list.length; i++) {
            register(list[i]);
            oldEntries.remove(list[i].getId());
        }
        for (Integer entry : oldEntries) {
            unregister(entry);
        }
    }

    private void unregister(int id) {
        UserEntry user = userById.remove(id);
        if (user != null) {
            userByName.remove(user.getName());
        }
        for (Iterator<UserRegistryListener> iterator = listeners.iterator(); iterator.hasNext();) {
            iterator.next().userRemoved(user);
        }
    }

    public int getUserNumber() {
        return userById.size();
    }

    public synchronized UserEntry[] serialize() {
        return userById.values().toArray(new UserEntry[userById.size()]);
    }

    public synchronized UserEntry search(int id) {
        return userById.get(id);
    }

    private boolean userExists(String name) {
        return userByName.get(name) != null;
    }

    public synchronized void addListener(UserRegistryListener listenerUser) {
        listeners.add(listenerUser);
    }

    public synchronized void removeListener(UserRegistryListener listenerUser) {
        listeners.remove(listenerUser);
    }

    public UserEntry findByName(String userName) {
        return userByName.get(userName);
    }

    public UserEntry[] findByGroupName(String groupName) {
        if (groupName == null) {
            return new UserEntry[0];
        }
        List<UserEntry> users = new ArrayList<UserEntry>();
        for (UserEntry entry : userById.values()) {
            if (groupName.equals(entry.getGroup())) {
                users.add(entry);
            }
        }
        return users.toArray(new UserEntry[users.size()]);
    }
}

