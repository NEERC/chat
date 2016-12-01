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

import java.io.Serializable;

/**
 * @author Matvey Kazakov
 */
public class UserEntry implements Comparable<UserEntry>, Serializable {
    @Deprecated
    private int id;

    private String jid;

    private String name;
    private boolean power;
    private String group;
    private boolean online = false;

    public UserEntry(String jid, int id, String name, boolean power) {
        this.jid = jid;
        this.id = id;
        this.name = name;
        this.power = power;
    }

    public int getId() {
        return id;
    }

    public String getJid() {
        return jid;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        if (group == null) {
            return power ? "Admins" : "Users";
        }
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isPower() {
        return power;
    }

    public void setPower(boolean power) {
        this.power = power;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserEntry)) {
            return false;
        }

        final UserEntry userEntry = (UserEntry) o;

        return id == userEntry.id;
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        String result = name;
        if (!online) {
            result = "-" + result;
        }
        if (power) {
            result += "(!)";
        }
        return result;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int compareTo(UserEntry o) {
        UserEntry userEntry = (UserEntry) o;
        String name = getName();
        if (name == null) {
            name = "";
        }
        String name1 = userEntry.getName();
        if (name1 == null) {
            name1 = "";
        }
        if (power && !userEntry.isPower()) return -1;
        if (!power && userEntry.isPower()) return 1;
        return name.compareTo(name1);
    }
}
