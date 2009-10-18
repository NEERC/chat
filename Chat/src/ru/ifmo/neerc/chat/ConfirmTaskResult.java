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
public class ConfirmTaskResult extends TaskResult{
    
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