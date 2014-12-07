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
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.neerc.chat.client.AbstractChatClient;
import ru.ifmo.neerc.chat.client.Chat;
import ru.ifmo.neerc.chat.client.ChatMessage;
import ru.ifmo.neerc.chat.message.ServerMessage;
import ru.ifmo.neerc.chat.message.UserMessage;
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
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Evgeny Mandrikov
 */
public class XmppChatClient extends AbstractChatClient {
    private static final Logger LOG = LoggerFactory.getLogger(XmppChatClient.class);
    private boolean alertPending;

    private XmppChat xmppChat;
    private HashSet<String> newTaskIds = new HashSet<String>();

    public XmppChatClient() {
        final String name = System.getProperty("username");

        UserRegistry userRegistry = UserRegistry.getInstance();
        user = userRegistry.findOrRegister(name);
        userRegistry.putOnline(name);
        userRegistry.setRole(name, "moderator");

        MyListener listener = new MyListener();
        taskRegistry.addListener(listener);

        chat = xmppChat = new XmppChat(name, listener);

        setupUI();

        xmppChat.connect();

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

    private synchronized void alertNewTasks() {
        if (alertPending) {
            return;
        }
        alertPending = true;
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                    StringBuilder description = new StringBuilder("New tasks:\n");
                    boolean hasNew = false;
                    for (Task task : TaskRegistry.getInstance().getTasks()) {
                        TaskStatus status = task.getStatus(user.getName());
                        if (status == null || !TaskActions.STATUS_NEW.equals(status.getType())) {
                            continue;
                        }
                        if (newTaskIds.contains(task.getId())) continue;
                        newTaskIds.add(task.getId());
                        hasNew = true;
                        LOG.debug("got new task " + task.getTitle());
                        description.append(task.getTitle()).append("\n");
                        ChatMessage chatMessage = ChatMessage.createTaskMessage(
                                "!!! New task '" + task.getTitle() + "' has been assigned to you !!!",
                                task.getDate()
                        );
                        processMessage(chatMessage);
                    }
                    if (!hasNew) {
                        alertPending = false;
                        return;
                    }

                    if (isBeepOn) {
                        System.out.print('\u0007'); // PC-speaker beep
                    }
                    setAlwaysOnTop(true);
                    JOptionPane.showMessageDialog(
                            XmppChatClient.this,
                            description.toString(),
                            "New tasks",
                            JOptionPane.WARNING_MESSAGE
                    );
                    setAlwaysOnTop(false);
                } catch (InterruptedException e) {

                } finally {
                    alertPending = false;
                }
            }
        }).start();
    }

    private void setConnectionStatus(String status, boolean isError) {
        if (status.equals(connectionStatus.getText())) {
            return;
        }
        if (!isError) {
            LOG.info("Connection status: " + status);
            connectionStatus.setForeground(Color.BLUE);
        } else {
            LOG.error("Connection status: " + status);
            connectionStatus.setForeground(Color.RED);
        }
        connectionStatus.setText(status);
    }

    private void setConnectionStatus(String status) {
        setConnectionStatus(status, false);
    }

    private void setConnectionError(String error) {
        setConnectionStatus(error, true);
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
                chat.write(task);
        }
    }

    private class MyListener implements MUCListener, ConnectionListener, TaskRegistryListener {
        @Override
        public void connected(XmppChat chat) {
            chat.getConnection().addConnectionListener(this);
            chat.getConnection().addPacketListener(new ClockPacketListener(),
                    new PacketExtensionFilter("x", XmlUtils.NAMESPACE_CLOCK));
            if (chat.getMultiUserChat().isJoined()) {
                final String message = "Connected";
                setConnectionStatus(message);
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
            processMessage(new ServerMessage(message));
            for (UserEntry user : UserRegistry.getInstance().getUsers()) {
                UserRegistry.getInstance().putOffline(user.getName());
            }
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            final String message = "Connection closed on error";
            setConnectionError(message);
            processMessage(new ServerMessage(message));
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
            processMessage(new ServerMessage(message));
            resetButton.setEnabled(true);
        }

        @Override
        public void reconnectionFailed(Exception e) {
            setConnectionError("Reconnection failed");
            resetButton.setEnabled(true);
        }

        @Override
        public void joined(String participant) {
            UserRegistry.getInstance().putOnline(participant);
            processMessage(new ServerMessage(
                    getNick(participant) + " online"
            ));
        }

        @Override
        public void left(String participant) {
            final String username = getNick(participant);
            if (UserRegistry.getInstance().findByName(username).isOnline()) {
                UserRegistry.getInstance().putOffline(participant);
                processMessage(new ServerMessage(username + " offline"));
            }
        }

        @Override
        public void roleChanged(String jid, String role) {
            if ("none".equals(role)) {
                return;
            }
            UserRegistry.getInstance().setRole(jid, role);
            String nick = getNick(jid);
//            processMessage(new ServerMessage(
//                    "User " + nick + " now " + role
//            ));
            if (nick.equals(user.getName())) {
                taskPanel.adminToolBar.setVisible("moderator".equals(role));
            }
        }

        @Override
        public void messageReceived(String jid, String message, Date timestamp) {
            processMessage(new UserMessage(jid, message, timestamp));
        }

        @Override
        public void historyMessageReceived(String jid, String message, Date timestamp) {
            processMessage(new UserMessage(jid, message, timestamp));
        }

        @Override
        public void taskChanged(Task task) {
            TaskStatus status = task.getStatus(user.getName());
            if (status == null || !TaskActions.STATUS_NEW.equals(status.getType())) {
                return;
            }
            alertNewTasks();
        }

        @Override
        public void tasksReset() {
        }
    }

    private class ClockPacketListener implements PacketListener {
        @Override
        public void processPacket(Packet packet) {
            Message message = (Message) packet;
            NeercClockPacketExtension extension = (NeercClockPacketExtension) message.getExtension("x", XmlUtils.NAMESPACE_CLOCK);
            Clock clock = extension.getClock();
            ticker.updateStatus(clock.getTotal(), clock.getTime(), clock.getStatus());
            if (clock.getStatus() != 1)
                updateScheduledTasks(clock.getTime(), clock.getTotal());
        }
    }
}
