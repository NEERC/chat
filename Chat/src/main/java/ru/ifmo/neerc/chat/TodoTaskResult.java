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
package ru.ifmo.neerc.chat;

import ru.ifmo.ips.config.Config;

/**
 * <code>TodoTaskResult</code> class
 *
 * @author Matvey Kazakov
 */
public class TodoTaskResult extends TaskResult{
    
    public static final int STATE_NEW = 0;
    public static final int STATE_INPROGRESS = 1;
    public static final int STATE_DONE = 2;
    
    private int state = STATE_NEW;
    private static final String ATT_TODOVAL = "@todoval";

    public String toString() {
        return null;
    }

    public void serialize(Config node) {
        node.setProperty(ATT_TODOVAL, String.valueOf(state));
    }

    public void deserialize(Config node) {
        state = node.getInt(ATT_TODOVAL);
    }

    public boolean actionSupported(int action) {
        return (state == STATE_NEW || state == STATE_DONE) && action == TaskFactory.ACTION_START || 
                state == STATE_INPROGRESS && action == TaskFactory.ACTION_DONE;
    }

    public void performAction(int action, Object... param) {
        if (action == TaskFactory.ACTION_START) {
            state = STATE_INPROGRESS;
        } else if (action == TaskFactory.ACTION_DONE) {
            state = STATE_DONE;
        }
    }

    public int getVisualState() {
        return state == STATE_NEW ? TaskFactory.VSTATE_NEW : 
                state == STATE_INPROGRESS ? TaskFactory.VSTATE_INPROGRESS :
                TaskFactory.VSTATE_DONE;
    }
}
