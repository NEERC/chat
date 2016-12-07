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
    private static final Map<String, UserRegistry> INSTANCES = new HashMap<String, UserRegistry>();

    private Map<Integer, UserEntry> userById = new HashMap<Integer, UserEntry>();
    private Map<String, UserEntry> userByName = new HashMap<String, UserEntry>();

    private Collection<UserRegistryListener> listeners = new ArrayList<UserRegistryListener>();

    /**
     * Returns user registry instance.
     *
     * @return user registry instance
     */
    public static UserRegistry getInstance() {
        return getInstanceFor(null);
    }

    public static UserRegistry getInstanceFor(String roomName) {
        UserRegistry userRegistry = INSTANCES.get(roomName);
        if (userRegistry == null) {
            userRegistry = new UserRegistry();
            INSTANCES.put(roomName, userRegistry);
        }
        return userRegistry;
    }

    /**
     * Hide default constructor.
     */
    private UserRegistry() {
    }

    public Collection<UserEntry> getUsers() {
        return Collections.unmodifiableCollection(userById.values());
    }

    private String getNick(String jid) {
        int pos = jid.indexOf('/');
        if (pos != -1) {
            return jid.substring(pos + 1);
        }
        pos = jid.indexOf('@');
        if (pos != -1) {
            return jid.substring(0, pos);
        }
        return jid;
    }

    public synchronized UserEntry findOrRegister(String jid) {
        final String nick = getNick(jid);
        UserEntry user = findByName(nick);
        if (user == null) {
            user = new UserEntry(
                    jid,
                    getUserNumber() + 1,
                    nick,
                    false
            );
            userByName.put(user.getName(), user);
            userById.put(user.getId(), user);
            notifyListeners(user);
        }
        return user;
    }

    private synchronized void putOnline(UserEntry user, boolean online) {
        if (online != user.isOnline()) {
            user.setOnline(online);
            notifyPresenceListeners(user);
        }
    }

    public void putOnline(String jid) {
        putOnline(findOrRegister(jid), true);
    }

    public void putOffline(String jid) {
        putOnline(findOrRegister(jid), false);
    }

    public synchronized void setPower(String jid, boolean power) {
        UserEntry user = findOrRegister(jid);
        if (power != user.isPower()) {
            user.setPower(power);
            notifyListeners(user);
        }
    }

    private void notifyListeners(UserEntry user) {
        for (UserRegistryListener listener : listeners) {
            listener.userChanged(user);
        }
    }

    private void notifyPresenceListeners(UserEntry user) {
        for (UserRegistryListener listener : listeners) {
            listener.userPresenceChanged(user);
        }
    }

    public int getUserNumber() {
        return userById.size();
    }

    
    public synchronized UserEntry[] findMatchingUsers(String list) {
        String[] search = list.split(",");
        ArrayList<UserEntry> res = new ArrayList<UserEntry>();
        for (UserEntry user : userByName.values()) {
            String name = user.getName();
            String group = user.getGroup();
            for (String s : search) {
                if (s.equalsIgnoreCase(name) || s.equalsIgnoreCase(group)) {
                    res.add(user);
                    break;
                }
            }
        }
        return res.toArray(new UserEntry[res.size()]);
    }
    
    public synchronized UserEntry[] serialize() {
        return userById.values().toArray(new UserEntry[userById.size()]);
    }

    /**
     * Searches user by id.
     *
     * @param id user id
     * @return user
     * @deprecated use JIDs
     */
    @Deprecated
    public synchronized UserEntry search(int id) {
        return userById.get(id);
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
