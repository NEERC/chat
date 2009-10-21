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

import ru.ifmo.neerc.chat.*;
import ru.ifmo.neerc.chat.message.*;

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
import java.util.Date;

/**
 * TODO: Log file
 * TODO: List instead of Text area for chat log
 *
 * @author Matvey Kazakov
 */
public abstract class AbstractChatClient extends JFrame implements MessageListener {

    public static final String DEFAULT_CONFIG_FILE = "client.xml";
    public ChatArea outputArea;
    public ChatArea outputAreaJury;
    public JTextArea inputArea;
    public JLabel neercTimer = new JLabel();
    protected JLabel connectionStatus = new JLabel();
    protected TaskRegistry taskRegistry = TaskRegistry.getInstance();
    protected UserEntry user;
    protected int localHistorySize;
    private static final int MAX_MESSAGE_LENGTH = 200;

    private TimerTicker ticker = new TimerTicker(neercTimer);
    private JPanel mainPanel;
    private JSplitPane powerSplitter;

//    private PluginManager pluginManager;

    protected Chat chat;

    public AbstractChatClient() {
    }

    protected void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel = createMainPanel();
        if (!user.isPower()) {
            setContentPane(mainPanel);
            setSize(800, 600);
        } else {
            powerSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            powerSplitter.setTopComponent(mainPanel);
            powerSplitter.setBottomComponent(new AdminTaskPanel(this, taskRegistry, chat));
            powerSplitter.setResizeWeight(1.0);
            powerSplitter.setDividerLocation(600);
            setContentPane(powerSplitter);
            setSize(800, 800);
        }
        setLocationRelativeTo(null);
    }

    private JPanel createMainPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        outputArea = new ChatArea();
        outputAreaJury = new ChatArea();
        inputArea = createInputArea();
        JScrollPane outputAreaScroller = new JScrollPane(outputArea);
        JScrollPane outputAreaScrollerJury = new JScrollPane(outputAreaJury);
        JSplitPane outputSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputAreaScrollerJury,
                outputAreaScroller);
        setupSplitter(outputSplitter);
        outputAreaScrollerJury.setMinimumSize(new Dimension(300, 200));
        JSplitPane chatSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, outputSplitter,
                new JScrollPane(inputArea));
        setupSplitter(chatSplitter);
        chatSplitter.setResizeWeight(1);
        chatSplitter.setDividerLocation(450);
        chatPanel.add(chatSplitter, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new BorderLayout());
        UsersPanel users = new UsersPanel(user);
        TaskPanel personalTasks = new TaskPanel(taskRegistry, user, chat);
        JSplitPane controlSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, users, personalTasks);
        setupSplitter(controlSplitter);
        controlSplitter.setResizeWeight(1);
        controlSplitter.setDividerLocation(300);
        controlPanel.add(controlSplitter, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        JSplitPane mainSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, controlPanel, chatPanel);
        setupSplitter(mainSplitter);
        mainSplitter.setDividerLocation(100);
        topPanel.add(mainSplitter);
        topPanel.add(createToolBar(), BorderLayout.NORTH);
        setTitle("NEERC chat: " + user.getName());
        return topPanel;
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        if (user.isPower()) {
            JButton tasks = new JButton(new ImageIcon(AbstractChatClient.class.getResource("res/btn_tasks.gif")));
            tasks.setFocusable(false);
            tasks.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    powerSplitter.setOrientation(1 - powerSplitter.getOrientation());
                }
            });

            toolBar.add(tasks);
        }
        JButton about = new JButton(new ImageIcon(AbstractChatClient.class.getResource("res/btn_about.gif")));
        about.setFocusable(false);
        about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AboutBox(AbstractChatClient.this).setVisible(true);
            }
        });


        toolBar.add(about);
//        java.util.List<ChatPlugin> plugins = pluginManager.getPlugins();
//        if (plugins.size() > 0) {
//            toolBar.addSeparator();
//            for (final ChatPlugin plugin : plugins) {
//                JButton button = new JButton(plugin.getIcon());
//                button.addActionListener(new ActionListener()  {
//                    public void actionPerformed(ActionEvent e) {
//                        plugin.start();
//                    }
//                });
//                toolBar.add(button);
//            }
//        }
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(connectionStatus);
        toolBar.add(neercTimer);
        return toolBar;
    }

    private void setupSplitter(JSplitPane chatSplitter) {
        chatSplitter.setDividerSize(2);
        chatSplitter.setOneTouchExpandable(false);
    }

    private JTextArea createInputArea() {
        final JTextArea inputArea = new JTextArea(2, 45);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        if (!user.isPower()) {
            inputArea.setDocument(new PlainDocument() {
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    if (getLength() + str.length() > MAX_MESSAGE_LENGTH) {
                        str = str.substring(0, MAX_MESSAGE_LENGTH - getLength());
                    }
                    super.insertString(offs, str, a);
                }
            });
        }
        final MessageLocalHistory messageLocalHistory = new MessageLocalHistory(localHistorySize);
        inputArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e1) {
                if (e1.getKeyChar() == KeyEvent.VK_ENTER && (e1.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
                    String text = inputArea.getText();
                    messageLocalHistory.add(text);
                    send(text);
                }
            }

            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && (e.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
                    String message = messageLocalHistory.moveDown();
                    if (message != null) {
                        inputArea.setText(message);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_UP && (e.getModifiers() & KeyEvent.CTRL_MASK) > 0) {
                    String message = messageLocalHistory.moveUp();
                    if (message != null) {
                        inputArea.setText(message);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    inputArea.setText("");
                }
            }
        });
        return inputArea;
    }

    private void send(String text) {
        // ansure that null won't be here
        text = String.valueOf(text);

        int toPos = text.indexOf(">");
        int destination = -1;
        if (toPos >= 0) {
            String to = text.substring(0, toPos).trim();
            UserEntry toUser = UserRegistry.getInstance().findByName(to);
            if (toUser != null) {
                destination = toUser.getId();
//                text = text.substring(toPos + 1);
            }
        }

        chat.write(new UserMessage(user.getId(), destination, new UserText(text)));
        inputArea.setText("");
    }

    public void processMessage(Message message) {
        taskRegistry.processMessage(message);
        ChatMessage chatMessage = null;
        if (message instanceof ServerMessage) {
            ServerMessage serverMessage = (ServerMessage) message;

            switch (serverMessage.getEventType()) {
                case ServerMessage.USER_JOINED:
                    chatMessage = ChatMessage.createServerMessage(
                            "User " + serverMessage.getUser().getName() + " has joined chat"
                    );
                    UserRegistry.getInstance().putOnline(serverMessage.getUser(), true);
                    break;
                case ServerMessage.USER_LEFT:
                    chatMessage = ChatMessage.createServerMessage(
                            "User " + serverMessage.getUser().getName() + " has left chat"
                    );
                    UserRegistry.getInstance().putOnline(serverMessage.getUser(), false);
                    break;
            }
        } else if (message instanceof UserMessage) {
            chatMessage = ChatMessage.createUserMessage((UserMessage) message);
        } else if (message instanceof TaskMessage) {
            final TaskMessage taskMessage = (TaskMessage) message;
            switch (taskMessage.getTaskMsgType()) {
                case TaskMessage.ASSIGN:
                    if (taskMessage.getUser() == user.getId()) {
                        final String description = taskRegistry.findTask(taskMessage.getTaskId()).getDescription();
                        Date timestamp = taskMessage.getTimestamp();
                        if ((timestamp == null) || (DateUtils.getTimeDifference(timestamp) < DateUtils.MINUTE)) {
                            new Thread(new Runnable() {
                                public void run() {
                                    JOptionPane.showMessageDialog(
                                            AbstractChatClient.this,
                                            "New task: " + description,
                                            "New Task",
                                            JOptionPane.WARNING_MESSAGE
                                    );
                                }
                            }).start();
                        }
                        chatMessage = ChatMessage.createTaskMessage(
                                "!!! New task '" + description + "' has been assigned to you !!!",
                                timestamp
                        );
                    }
                    break;
            }
        }

        if (chatMessage != null) {
            if (outputArea == null || outputAreaJury == null) {
                addMessage(chatMessage);
            } else {
                synchronized (messagesToShow) {
                    for (ChatMessage chatMessage1 : messagesToShow) {
                        showMessage(chatMessage1);
                    }
                    messagesToShow.clear();
                    showMessage(chatMessage);
                }
            }
        }
    }

    private void showMessage(ChatMessage chatMessage) {
        outputArea.addMessage(chatMessage);
        if (chatMessage.isSpecial()) {
            outputAreaJury.addMessage(chatMessage);
        }

        ChatLogger.logChat(chatMessage.log());
    }

    private final ArrayList<ChatMessage> messagesToShow = new ArrayList<ChatMessage>();

    protected void addMessage(ChatMessage msg) {
        synchronized (messagesToShow) {
            messagesToShow.add(msg);
        }
    }

    public void connectionLost() {
        JOptionPane.showMessageDialog(this, "Connection lost. Exiting...");
        System.exit(1);
    }
}
