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
package ru.ifmo.neerc.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.ifmo.neerc.chat.Chat;
import ru.ifmo.neerc.chat.ChatListener;
import ru.ifmo.neerc.chat.ChatMessage;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.chat.user.UserRegistryListener;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskActions;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.task.TaskStatus;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Log file
 * TODO: List instead of Text area for chat log
 *
 * @author Matvey Kazakov
 */
public abstract class AbstractChatClient extends JFrame implements ChatListener, UserRegistryListener, TaskRegistryListener {

    private static final Logger LOG = LoggerFactory.getLogger("Chat");

    public ChatArea outputArea;
    public ChatArea outputAreaJury;
    public ChatInputArea inputArea;
    public JLabel neercTimer = new JLabel();
    protected JLabel connectionStatus = new JLabel();
    protected JButton resetButton;
    protected TaskRegistry taskRegistry = TaskRegistry.getInstance();
    protected UserEntry user;
    UsersPanel usersPanel;
    private static final int MAX_MESSAGE_LENGTH = 500;

    ChannelList channelsSubscription = new ChannelList(this);

    private JSplitPane powerSplitter;

    protected ToggleIconButton beepSwitch;
    protected ToggleIconButton sendModeSwitch;

    protected TimerTicker ticker = new TimerTicker(neercTimer);
    protected NameColorizer colorizer = new NameColorizer();

    protected Chat chat;

    private boolean alertPending;
    private HashSet<String> newTaskIds = new HashSet<String>();

    public AbstractChatClient() {
        UserRegistry.getInstance().addListener(this);
        TaskRegistry.getInstance().addListener(this);
    }

    protected AdminTaskPanel taskPanel;

    protected void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = createMainPanel();
        taskPanel = new AdminTaskPanel(this, taskRegistry, chat, user.getName());

        powerSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        powerSplitter.setTopComponent(mainPanel);
        powerSplitter.setBottomComponent(taskPanel);
        powerSplitter.setResizeWeight(1.0);
        powerSplitter.setDividerLocation(600);
        setContentPane(powerSplitter);
        setTitle("NEERC chat: " + user.getName());
        setSize(800, 800);
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        outputArea = new ChatArea(user, colorizer, channelsSubscription);
        outputAreaJury = new ChatArea();
        inputArea = new ChatInputArea(this, null);
        JScrollPane outputAreaScroller = new JScrollPane(outputArea);
        JScrollPane outputAreaScrollerJury = new JScrollPane(outputAreaJury);
        JSplitPane outputSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputAreaScrollerJury,
                outputAreaScroller);
        setupSplitter(outputSplitter);
        outputAreaScrollerJury.setMinimumSize(new Dimension(300, 100));
        JSplitPane chatSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputSplitter,
                new JScrollPane(inputArea));
        setupSplitter(chatSplitter);
        chatSplitter.setResizeWeight(1);
        chatSplitter.setDividerLocation(526);
        chatPanel.add(chatSplitter, BorderLayout.CENTER);

        UserPickListener setPrivateAddresseesListener = new UserPickListener() {
            public void userPicked(UserEntry user) {
                inputArea.setPrivateAddressees(user.getName());
            }
        };

        usersPanel = new UsersPanel(user, colorizer);
        usersPanel.addListener(setPrivateAddresseesListener);

        ChannelsPanel channelsPanel = new ChannelsPanel(channelsSubscription);

        JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, usersPanel, channelsPanel);
        leftPane.setResizeWeight(0.8);

        outputArea.addUserPickListener(setPrivateAddresseesListener);

        JPanel topPanel = new JPanel(new BorderLayout());
        JSplitPane mainSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, chatPanel);
        setupSplitter(mainSplitter);
        mainSplitter.setDividerLocation(100);
        topPanel.add(mainSplitter);
        topPanel.add(createToolBar(), BorderLayout.NORTH);
        return topPanel;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        JButton tasks = new JButton(new ImageIcon(AbstractChatClient.class.getResource("res/btn_tasks.gif")));
        tasks.setToolTipText("Change task list position");
        tasks.setFocusable(false);
        tasks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                powerSplitter.setOrientation(1 - powerSplitter.getOrientation());
            }
        });
        toolBar.add(tasks);

        JButton about = new JButton(new ImageIcon(AbstractChatClient.class.getResource("res/btn_about.gif")));
        about.setToolTipText("About");
        about.setFocusable(false);
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AboutBox(AbstractChatClient.this, "res/help.html").setVisible(true);
            }
        });
        toolBar.add(about);

        beepSwitch = new ToggleIconButton(
            "res/btn_beep_off.png", "Turn beep on",
            "res/btn_beep_on.png", "Turn beep off"
        );
        toolBar.add(beepSwitch);

        sendModeSwitch = new ToggleIconButton(
            "res/btn_ctrl_enter.png", "Messages are sent on Ctrl+Enter",
            "res/btn_enter.png", "Messages are sent on Enter"
        );
        toolBar.add(sendModeSwitch);

        final ToggleIconButton chatColorSwitch = new ToggleIconButton(
            "res/btn_black_and_white.png", "Black names in chat",
            "res/btn_colored.png", "Colored names in chat"
        );
        chatColorSwitch.setSelected(true);
        chatColorSwitch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colorizer.setColored(!colorizer.isColored());
                outputArea.repaint();
                usersPanel.repaint();
            }
        });
        toolBar.add(chatColorSwitch);

        final ToggleIconButton separateChannelsSwitch = new ToggleIconButton(
            "res/channels_together.png", "Channel messages in main window",
            "res/channels_separated.png", "Channel messages in separate window"
        );
        separateChannelsSwitch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                channelsSubscription.setSeparated(!channelsSubscription.isSeparated());
            }
        });
        toolBar.add(separateChannelsSwitch);

        resetButton = new JButton("Reconnect");
        resetButton.setFocusable(false);

        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(connectionStatus);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(neercTimer);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(resetButton);
        return toolBar;
    }

    private void setupSplitter(JSplitPane chatSplitter) {
        chatSplitter.setDividerSize(2);
        chatSplitter.setOneTouchExpandable(false);
    }

    public boolean isBeepOn() {
        return beepSwitch.isSelected();
    }

    public boolean sendOnEnter() {
        return sendModeSwitch.isSelected();
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

    protected void setConnectionStatus(String status) {
        setConnectionStatus(status, false);
    }

    protected void setConnectionError(String error) {
        setConnectionStatus(error, true);
    }

    protected void send(String text) {
        // ensure that null won't be here
        text = String.valueOf(text);
        String pattern = "^@(todo|todofail|task|confirm|ok|okfail|reason|question|q)( [\\w,]+)?( (start|end)([+-]\\d+)?(!)?)? (.*)$";
        Matcher matcher = Pattern.compile(pattern, Pattern.MULTILINE).matcher(text);
        while (matcher.find()) {
            String type = TaskActions.getTypeByAlias(matcher.group(1));
            String to = matcher.group(2) == null ? "" : matcher.group(2).substring(1);
            String title = matcher.group(7);
            Task task = new Task(type, title);

            if (matcher.group(3) != null) {
                Task.ScheduleType scheduleType = Task.ScheduleType.NONE;

                switch (matcher.group(4)) {
                    case "start":
                        scheduleType = Task.ScheduleType.CONTEST_START;
                        break;
                    case "end":
                        scheduleType = Task.ScheduleType.CONTEST_END;
                        break;
                }

                long time = matcher.group(5) == null ? 0 : Integer.parseInt(matcher.group(5)) * 60000;
                boolean needsConfirmation = matcher.group(6) == null;

                task.schedule(scheduleType, time);
                task.setNeedsConfirmation(needsConfirmation);
            }

            for (UserEntry user : UserRegistry.getInstance().findMatchingUsers(to)) {
                String username = user.getName();
                if (task.getStatus(username) == null) {
                    task.setStatus(username, "none", "");
                }
            }
            chat.sendTask(task);
        }

        // channels support
        Matcher channelMatches = Pattern.compile("^/s\\s+(%\\w+)\\s*$", Pattern.DOTALL).matcher(text);
        if (channelMatches.find()) {
            channelsSubscription.subscribeTo(channelMatches.group(1));
        }

        channelMatches = Pattern.compile("^/d\\s+(%\\w+)\\s*$", Pattern.DOTALL).matcher(text);
        if (channelMatches.find()) {
            channelsSubscription.unsubscribeFrom(channelMatches.group(1));
        }

        // do not echo commands (including mistyped) to chat
        if (!Pattern.compile("^(@|/)\\w+ .*", Pattern.DOTALL).matcher(text).matches()) {
            chat.sendMessage(new ChatMessage(text, user));
        }
    }

    protected void showMessage(Message message) {
        if (message.getChannel() != null)
            channelsSubscription.showMessage(message);

        outputArea.addMessage(message);
        if (message.isImportant()) {
            outputAreaJury.addMessage(message);
        }

        LOG.info(message.toString());
    }

    protected synchronized void alertNewTasks() {
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
                        showMessage(new TaskMessage(task));
                    }
                    if (!hasNew) {
                        alertPending = false;
                        return;
                    }

                    if (isBeepOn()) {
                        System.out.print('\u0007'); // PC-speaker beep
                    }
                    setAlwaysOnTop(true);
                    JOptionPane.showMessageDialog(
                            AbstractChatClient.this,
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

    @Override
    public void processMessage(ChatMessage message) {
        showMessage(new UserMessage(message));
    }

    @Override
    public void userPresenceChanged(UserEntry userEntry) {
        final String status = userEntry.isOnline() ? "online" : "offline";
        showMessage(new StatusMessage(userEntry.getName() + " " + status));
    }

    @Override
    public void userChanged(UserEntry userEntry) {
        if (user != null && userEntry.getName().equals(user.getName())) {
            taskPanel.adminToolBar.setVisible(userEntry.isPower());
        }
    }

    @Override
    public void taskChanged(Task task) {
        TaskStatus status = task.getStatus(user.getName());
        if (status != null && TaskActions.STATUS_NEW.equals(status.getType())) {
            alertNewTasks();
        }
    }

    @Override
    public void tasksReset() {
        newTaskIds.clear();
    }
}
