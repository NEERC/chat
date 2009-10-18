/*
 * Date: Oct 24, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat;

import ru.ifmo.ips.config.Config;
import ru.ifmo.ips.config.ConfigException;
import ru.ifmo.neerc.chat.message.UserText;

/**
 * <code>QuestionTaskResult</code> class
 *
 * @author Matvey Kazakov
 */
public class QuestionTaskResult extends TaskResult{

    private boolean done = false;
    private UserText message = new UserText();
    
    public static final String NODE_MSG = "msg";
    public static final String ATT_DONE = "msg@done";

    public String toString() {
        return done ? message.getText() : "";
    }

    public void serialize(Config node) {
        if (message.getText() != null) {
            node.setProperty(NODE_MSG, message.asString());
        }
        node.setProperty(ATT_DONE, String.valueOf(done ? 1 : 0));
    }

    public void deserialize(Config node) {
        try {
            message.fromString(node.getProperty(NODE_MSG)); 
        } catch (ConfigException e) {}
        done = node.getInt(ATT_DONE) == 1;
    }

    public boolean actionSupported(int action) {
        return action == TaskFactory.ACTION_DONE;
    }

    public void performAction(int action, Object... param) {
        if (action == TaskFactory.ACTION_DONE) {        
            done = true;
            message.setText((String)param[0]);
        }
    }

    public int getVisualState() {
        return done ? TaskFactory.VSTATE_DONE : TaskFactory.VSTATE_NEW;
    }
}

