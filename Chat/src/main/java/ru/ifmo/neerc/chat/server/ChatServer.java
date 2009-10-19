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
// $Id: ChatServer.java,v 1.11 2007/10/28 07:32:13 matvey Exp $
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.server;

import ru.ifmo.ips.IpsRuntimeException;
import ru.ifmo.ips.config.*;
import ru.ifmo.neerc.chat.*;
import ru.ifmo.neerc.chat.plugin.PluginManager;
import ru.ifmo.neerc.chat.client.UserUnregisteredException;
import ru.ifmo.neerc.chat.message.UserListUpdateMessage;
import ru.ifmo.neerc.chat.message.UserMessage;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

/**
 * @author Matvey Kazakov
 */
public class ChatServer extends Thread implements ConfigListener {

    public final static int DEFAULT_PORT = 6001;
    public static final int DEFAULT_TIMEOUT = 5000;
    private static final String DEFAULT_CONFIG_FILE = "server.xml";

    protected int port;
    protected ServerSocket server_port;
    protected JList userList;
    protected JList taskList;
    protected int timeout;
    protected Vector<Connection> watcher;
    /**
     * Server's task registry
     */
    protected TaskRegistry taskRegistry = TaskRegistry.getInstance();

    protected Map<String, String> userHosts = new HashMap<String, String>();
    protected Map<String, String> userGroups = new HashMap<String, String>();
    protected Map<String, Boolean> userPowers = new HashMap<String, Boolean>();

    private static final String DUMP_FILE = "dump.xml";
    private static final String NODE_USER = "u";
    private static final String NODE_TASK = "t";
    private static final String NODE_MESSAGE = "m";


    /**
     * Exit with an error message, when an exception occurs.
     */
    public static void fail(Exception e, String msg) {
        System.err.println(msg + ": " + e);
        System.exit(1);
    }

    // Create a ServerSocket to listen for connections on;  start the thread.
    public ChatServer() {
        // Create our server thread with a name.
        super("Server");

        Config config = ConfigFactory.getMainConfig();
        ConfigFactory.addListener(this);
        port = config.getInt("port", DEFAULT_PORT);
        timeout = config.getInt("ping", DEFAULT_TIMEOUT);

        try {
            server_port = new ServerSocket(port);
        } catch (IOException e) {
            fail(e, "Exception creating server socket");
        }

        // Create a ConnectionsKeeper thread to wait for other threads to die.
        // It starts itself automatically.
        MessageMulticaster.getInstance().addMessageListener(taskRegistry);
        MessageMulticaster.getInstance().addMessageListener(MessageCache.getInstance());
        watcher = new Vector<Connection>();

        // Create a window to display our connections in
        JFrame frame = new JFrame("Server Status");
        JPanel mainPanel = new JPanel(new GridBagLayout());
        UserListModel userListModel = new UserListModel();
        UserRegistry.getInstance().addListener(userListModel);
        userList = new JList(userListModel);
        TaskListModel taskListModel = new TaskListModel(taskRegistry);
        taskRegistry.addListener(taskListModel);
        MessageMulticaster.getInstance().addMessageListener(new ChatServerMessageLogger());
        taskList = new JList(taskListModel);

        loadDump();
        readUsersList();

        mainPanel.add(new JLabel("Users: "), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));
        mainPanel.add(new JLabel("Tasks: "), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
        mainPanel.add(new JScrollPane(userList), new GridBagConstraints(0, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 10), 0, 0));
        mainPanel.add(new JScrollPane(taskList), new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        frame.setContentPane(mainPanel);
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        Runtime.getRuntime().addShutdownHook(new StateDumper());

        setDaemon(true);
        // Start the server listening for connections
        this.start();
        
        new PluginManager(config);
        new TimerService().start();
    }

    public void configChanged(String configName) {
        ChatLogger.logDebug("Config is updated - re-reading users list");
        readUsersList();
        if (watcher != null) {
            for (Connection connection : watcher) {
                UserEntry user = connection.getUser();
                if (UserRegistry.getInstance().search(user.getId()) == null) {
                    ChatLogger.logDebug("User " + user.getName() + " is disconnected");
                    connection.close();
                }
            }
            UserEntry[] entries = UserRegistry.getInstance().serialize();
            MessageMulticaster.getInstance().sendMessage(new UserListUpdateMessage(entries));
        }
    }

    public synchronized Boolean allowUserToConnect(String user, String host) {
        if (host != null && host.equals(userHosts.get(user))) {
            ChatLogger.logDebug("Allowed " + user + " from " + host);
            return (Boolean)userPowers.get(user);
        } else {
            ChatLogger.logDebug("Could not allow connect " + user + " from " + host);
            return null;
        }
    }

    private void readUsersList() {
        Config config = ConfigFactory.getMainConfig();
        Config users = config.getNode("users");
        Config[] userList = users.getNodeList("user");
        ArrayList<UserEntry> entries = new ArrayList<UserEntry>();
        for (Config user : userList) {
            String userName = user.getProperty("@name", null);
            String host = user.getProperty("@host", null);
            String group = user.getProperty("@group", "default");
            String powerStr = user.getProperty("@power", "no");
            boolean power = "yes".equals(powerStr);
            if (userName != null && host != null) {
                userHosts.put(userName, host);
                userPowers.put(userName, power);
                userGroups.put(userName, group);
                UserEntry entry = UserRegistry.getInstance().findByName(userName);
                if (entry == null) {
                    entry = new UserEntry();
                    entry.genId();
                    entry.setOnline(false);
                }
                entry.setGroup(group);
                entry.setName(userName);
                entry.setPower(power);
                entries.add(entry);
            }
        }
        UserRegistry.getInstance().init(entries.toArray(new UserEntry[entries.size()]));
    }

    // The body of the server thread.  Loop forever, listening for and
    // accepting connections from clients.  For each connection,
    // create a Connection object to handle communication through the
    // new Socket.  When we create a new connection, add it to the
    // Vector of connections, and display it in the List.  Note that we
    // use synchronized to lock the Vector of connections.  The ConnectionsKeeper
    // class does the same, so the watcher won't be removing dead
    // connections while we're adding fresh ones.
    public void run() {
        try {
            while (true) {
                Socket client_socket = server_port.accept();
                Connection connection = new Connection(this, client_socket, taskRegistry, timeout);
                addConnection(connection);
            }
        } catch (IOException e) {
            fail(e, "Exception while listening for connections");
        }
    }

    private void addConnection(Connection connection) {
        watcher.add(connection);
    }

    protected void removeConnection(Connection connection) {
        watcher.remove(connection);
    }

    // Start the server up, listening on an optionally specified port
    public static void main(String[] args) {
        String configFile = null;
        if (args.length == 1) {
            configFile = args[0];
        }
        if (configFile == null) {
            configFile = DEFAULT_CONFIG_FILE;
        }
        try {
            ConfigFactory.setConfig(configFile, true);
        } catch (FileNotFoundException e) {
            throw new IpsRuntimeException(e);
        }
        new ChatServer();
    }

    public String getUserGroup(String userName) {
        return userGroups.get(userName);
    }

    private class UserListModel extends AbstractListModel implements UserRegistryListener {
        private UserEntry[] userEntries;

        public UserListModel() {
            update();
        }

        private void update() {
            userEntries = UserRegistry.getInstance().serialize();
            Arrays.sort(userEntries, new Comparator<UserEntry>() {
                public int compare(UserEntry o1, UserEntry o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            fireContentsChanged(this, 0, userEntries.length);
        }

        public int getSize() {
            return userEntries.length;
        }

        public Object getElementAt(int index) {
            if (index < userEntries.length) {
                return userEntries[index];
            } else {
                return null;
            }
        }

        public void userAdded(UserEntry userEntry) {
            update();
        }

        public void userRemoved(UserEntry userEntry) {
            update();
        }

        public void userChanged(UserEntry userEntry) {
            update();
        }
    }

    private class TaskListModel extends AbstractListModel implements TaskRegistryListener {
        private TaskRegistry registry;
        private Task[] tasks;

        public TaskListModel(TaskRegistry registry) {
            this.registry = registry;
            update();
        }

        private void update() {
            tasks = registry.serialize();
            Arrays.sort(tasks, new Comparator<Task>() {
                public int compare(Task o1, Task o2) {
                    return o1.getDescription().compareTo(o2.getDescription());
                }
            });
            fireContentsChanged(this, 0, tasks.length);
        }

        public int getSize() {
            return tasks.length;
        }

        public Object getElementAt(int index) {
            return tasks[index];
        }

        public void taskAdded(Task task) {
            update();
        }

        public void taskDeleted(Task task) {
            update();
        }

        public void taskChanged(Task taskId) {
            update();
        }
    }

    private class StateDumper extends Thread {

        public void run() {
            dumpServer();
        }

    }

    protected static void dumpServer() {
        XMLConfig config = XMLConfig.createEmptyConfig("dump");

        int i = 0;
        Set<Task> tasks = TaskRegistry.getInstance().getTasks();
        for (Task task : tasks) {
            task.serialize(config.createNode(NODE_TASK + "#" + (i++)));
        }

        i = 0;
        Collection<UserEntry> users = UserRegistry.getInstance().getUsers();
        for (UserEntry user : users) {
            user.serialize(config.createNode(NODE_USER + "#" + (i++)));
        }
        i = 0;
        List<UserMessage> userMessageList = MessageCache.getInstance().getMessages();
        for (UserMessage message : userMessageList) {
            message.serialize(config.createNode(NODE_MESSAGE + "#" + (i++)));
        }
        try {
            FileWriter out = new FileWriter(DUMP_FILE);
            config.writeConfig(out);
            out.close();
        } catch (IOException e) {
        }
    }

    protected static boolean loadDump() {
        try {
            XMLConfig dump = new XMLConfig(DUMP_FILE);

            Config[] taskNodes;
            try {
                taskNodes = dump.getNodeList(NODE_TASK);
            } catch (ConfigException e) {
                taskNodes = new Config[0];
            }
            int i = 0;
            Task[] tasks = new Task[taskNodes.length];
            for (Config taskNode : taskNodes) {
                Task task = new Task();
                task.deserialize(taskNode);
                tasks[i++] = task;
            }
            TaskRegistry.getInstance().init(tasks);

            Config[] userNodes = new Config[0];
            try {
                userNodes = dump.getNodeList(NODE_USER);
            } catch (ConfigException e) {
                userNodes = new Config[0];
            }
            for (Config userNode : userNodes) {
                UserEntry user = new UserEntry();
                user.deserialize(userNode);
                user.setOnline(false);
                UserRegistry.getInstance().register(user);
            }
            
            Config[] msgNodes = new Config[0];
            try {
                msgNodes = dump.getNodeList(NODE_MESSAGE);
            } catch (ConfigException e) {
                msgNodes = new Config[0];
            }
            for (Config msgNode : msgNodes) {
                UserMessage msg = new UserMessage();
                msg.deserialize(msgNode);
                MessageCache.getInstance().queueMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
