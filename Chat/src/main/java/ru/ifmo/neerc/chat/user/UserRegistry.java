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
 * Date: 25.10.2004
 */
package ru.ifmo.neerc.chat.user;

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
     *
     * @return user registry instance
     */
    public static UserRegistry getInstance() {
        return instance;
    }

    /**
     * Hide default constructor
     */
    private UserRegistry() {
    }

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
        for (UserRegistryListener listener : listeners) {
            listener.userAdded(user);
        }
        return true;
    }

    public synchronized void putOnline(UserEntry user, boolean online) {
        UserEntry userEntry = search(user.getId());
        if (userEntry == null) {
            return;
        }
        userEntry.setOnline(online);
        userChanged(user);
    }

    public synchronized void putOffline(UserEntry user) {
        putOnline(user, false);
    }

    public synchronized void putOnline(UserEntry user) {
        putOnline(user, true);
    }

    public synchronized void setRole(UserEntry user, String role) {
        user.setPower("moderator".equalsIgnoreCase(role));
        userChanged(user);
    }

    private void userChanged(UserEntry user) {
        for (UserRegistryListener listener : listeners) {
            listener.userChanged(user);
        }
    }

    public synchronized void init(UserEntry[] list) {
        UserEntry[] entries = serialize();
        Set<Integer> oldEntries = new HashSet<Integer>();
        for (UserEntry entry1 : entries) {
            oldEntries.add(entry1.getId());
        }
        for (UserEntry aList : list) {
            register(aList);
            oldEntries.remove(aList.getId());
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
        for (UserRegistryListener listener : listeners) {
            listener.userRemoved(user);
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
