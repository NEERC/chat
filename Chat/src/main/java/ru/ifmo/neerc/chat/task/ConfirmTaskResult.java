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
/*
 * Date: Oct 22, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.task;

import ru.ifmo.ips.config.Config;

/**
 * <code>TodoTaskResult</code> class
 *
 * @author Matvey Kazakov
 */
public class ConfirmTaskResult extends TaskResult {

    private int done = 0;
    private static final String ATT_TODOVAL = "@todoval";

    public String toString() {
        return null;
    }

    public void serialize(Config node) {
        node.setProperty(ATT_TODOVAL, String.valueOf(done));
    }

    public void deserialize(Config node) {
        done = node.getInt(ATT_TODOVAL);
    }

    public boolean actionSupported(int action) {
        return action == TaskFactory.ACTION_DONE;
    }

    public void performAction(int action, Object... param) {
        if (action == TaskFactory.ACTION_DONE) {
            done = 1 - done;
        }
    }

    public int getVisualState() {
        return done == 0 ? TaskFactory.VSTATE_NEW : TaskFactory.VSTATE_DONE;
    }
}
