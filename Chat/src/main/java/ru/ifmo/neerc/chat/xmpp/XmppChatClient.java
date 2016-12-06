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
package ru.ifmo.neerc.chat.xmpp;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaExtensionFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MUCRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.ChatMessage;
import ru.ifmo.neerc.chat.client.AbstractChatClient;
import ru.ifmo.neerc.chat.client.StatusMessage;
import ru.ifmo.neerc.chat.client.TaskMessage;
import ru.ifmo.neerc.chat.client.UserMessage;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.chat.xmpp.provider.NeercClockPacketExtension;
import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.task.*;
import ru.ifmo.neerc.utils.XmlUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChatClient extends AbstractChatClient {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChatClient.class);

    private XmppChat xmppChat;

    public XmppChatClient() {
        final String name = System.getProperty("username");

        UserRegistry userRegistry = UserRegistry.getInstance();
        user = userRegistry.findOrRegister(name);

        chat = xmppChat = new XmppChat(name, new MyListener());
        chat.addListener(this);

        setupUI();

        new Thread(new Runnable() {
            @Override
            public void run() {
                xmppChat.connect();
            }
        }).start();

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        xmppChat.connect();
                    }
                }).start();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new XmppChatClient().setVisible(true);
            }
        });
    }

    private String getNick(String participant) {
        return UserRegistry.getInstance().findOrRegister(participant).getName();
    }

    protected void send(String text) {
        if (text.equals("/dc")) {
            new Thread(new Runnable() {
                public void run() {
                    xmppChat.disconnect();
                }
            }).start();
            return;
        }
        if (text.equals("/rc")) {
            new Thread(new Runnable() {
                public void run() {
                    xmppChat.connect();
                }
            }).start();
            return;
        }
        super.send(text);
    }

    public void updateScheduledTasks(long time, long total) {
        long start = time;
        long end = time - total;

        List<Task> activatedTasks = new ArrayList<Task>();

        for (Task task : TaskRegistry.getInstance().getTasks()) {
            Task.ScheduleType type = task.getScheduleType();
            long scheduleTime = task.getScheduleTime();

            if (type == Task.ScheduleType.NONE)
                continue;

            if ((type == Task.ScheduleType.CONTEST_START && scheduleTime <= start) ||
                (type == Task.ScheduleType.CONTEST_END && scheduleTime <= end)) {
                activatedTasks.add(task);
            }
        }

        for (Task task : activatedTasks) {
            TaskRegistry.getInstance().update(new Task(task.getId(), "remove", ""));
            task.schedule(Task.ScheduleType.NONE, 0);
            task.setId(null);
            new Thread(new ScheduledTaskConfirmation(task)).start();
        }
    }

    private class ScheduledTaskConfirmation implements Runnable {
        private Task task;

        ScheduledTaskConfirmation(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (!task.getNeedsConfirmation()) {
                chat.sendTask(task);
                return;
            }

            final Runnable sound = (Runnable)Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation");
            if (sound != null)
                sound.run();

            int result = JOptionPane.showConfirmDialog(
                    XmppChatClient.this,
                    "Create task '" + task.getTitle() + "'?",
                    "Scheduled task",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION)
                chat.sendTask(task);
        }
    }

    private class MyListener implements ConnectionListener {
        @Override
        public void connected(XMPPConnection connection) {
        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            connection.addAsyncStanzaListener(new ClockPacketListener(),
                    new StanzaExtensionFilter(new NeercClockPacketExtension()));

            if (xmppChat.isConnected()) {
                setConnectionStatus("Connected");
            } else {
                setConnectionError("Unable to connect");
            }

            alertNewTasks();
            resetButton.setEnabled(true);
        }

        @Override
        public void connectionClosed() {
            final String message = "Connection closed";
            setConnectionError(message);
            showMessage(new StatusMessage(message));
            for (UserEntry user : UserRegistry.getInstance().getUsers()) {
                UserRegistry.getInstance().putOffline(user.getName());
            }
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            final String message = "Connection closed on error";
            setConnectionError(message);
            showMessage(new StatusMessage(message));
            for (UserEntry user : UserRegistry.getInstance().getUsers()) {
                UserRegistry.getInstance().putOffline(user.getName());
            }
            resetButton.setEnabled(true);
        }

        @Override
        public void reconnectingIn(int i) {
            if (i == 0) {
                setConnectionStatus("Reconnecting...");
                resetButton.setEnabled(false);
                return;
            }
            setConnectionError("Reconnecting in " + i);
        }

        @Override
        public void reconnectionSuccessful() {
            final String message = "Reconnected";
            setConnectionStatus(message);
            showMessage(new StatusMessage(message));
            resetButton.setEnabled(true);
        }

        @Override
        public void reconnectionFailed(Exception e) {
            setConnectionError("Reconnection failed");
            resetButton.setEnabled(true);
        }
    }

    private class ClockPacketListener implements StanzaListener {
        @Override
        public void processPacket(Stanza packet) {
            Message message = (Message) packet;
            NeercClockPacketExtension extension = (NeercClockPacketExtension) message.getExtension("x", XmlUtils.NAMESPACE_CLOCK);
            Clock clock = extension.getClock();
            ticker.updateStatus(clock.getTotal(), clock.getTime(), clock.getStatus());
            if (clock.getStatus() != 1)
                updateScheduledTasks(clock.getTime(), clock.getTotal());
        }
    }
}
