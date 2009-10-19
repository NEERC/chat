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
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.server;

import ru.ifmo.neerc.chat.*;
import ru.ifmo.neerc.chat.plugin.CustomMessage;
import ru.ifmo.neerc.chat.message.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

/**
 * Represents connection behaviour
 * @author Matvey Kazakov
 */
class Connection extends Thread implements MessageListener {
    
    // Priority of the connection thread
    private static final int CLIENT_CONNECTION_PRIORITY = (MIN_PRIORITY + NORM_PRIORITY) / 2;

    // client connection socket
    protected Socket clientSocket;
    // client input stream (from client to server)
    private InputStream inStream;
    // client output stream (from server to client)
    private OutputStream outStream;

    // user entry corresponding this connection
    private UserEntry user;
    // remember server for callbacks
    private ChatServer server;
    private TaskRegistry taskRegistry;
    private int timeout;
    private boolean closed = false;

    /**
     * Time last ping message received from client
     */
    private boolean replied;
                         

    // Initialize the streams and start the thread
    public Connection(ChatServer server, Socket clientSocket, TaskRegistry taskRegistry, int timeout) {
        this.server = server;
        this.taskRegistry = taskRegistry;
        this.timeout = timeout;

        // create new user upon connection
        user = new UserEntry();
        // initialize with ID
        user.genId();
        // Name thread after user.
        setName("Connection #" + user.getId());
        // set priority
        setPriority(CLIENT_CONNECTION_PRIORITY);
        // remember client socket
        this.clientSocket = clientSocket;
        try {
            clientSocket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Create the streams
        try {
            inStream = new BufferedInputStream(this.clientSocket.getInputStream());
            outStream = new BufferedOutputStream(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            try {
                this.clientSocket.close();
            } catch (IOException e2) {}
            System.err.println("Exception while getting socket streams: " + e);
            return;
        }
        // And start the thread up
        setDaemon(true);
        this.start();
    }


    public void processMessage(Message message) {
        try {
            if (message.shouldBeSentTo(user.getId())) {
                outStream.write(MessageFactory.getInstance().serialize(message));
                outStream.flush();
            }
        } catch (IOException e) {
            ChatLogger.logError("Error forwarding message: " + e.getMessage());
        }
    }

    public UserEntry getUser() {
        return user;
    }
    
    // Provide the service.
    // Read a line, reverse it, send it back.
    public void run() {

        if (login()) {
            MessageMulticaster multicaster = MessageMulticaster.getInstance();
            multicaster.sendMessage(new ServerMessage(ServerMessage.USER_JOINED, user));
            multicaster.addMessageListener(this);
            new PingThread().start();
          
            try {
// Loop forever, or until the connection is broken!
                while (!closed) {
                    Message message = MessageUtils.getMessage(inStream);
                    if (message instanceof UserMessage) {
                        ((UserMessage)message).setFrom(user.getId());
                        multicaster.sendMessage(message);
                    } else if (message instanceof TaskMessage) {
                        if (((TaskMessage)message).allowed(user)) {
                            multicaster.sendMessage(message);
                        }
                    } else if (message instanceof PingMessage) {
                        replied = true;
                    } else if (message instanceof CustomMessage) {
                        multicaster.sendMessage(message);
                    } else if (message != null) {
                        break;
                    }
                }
                
            } finally {
                multicaster.removeMessageListener(this);
                UserRegistry.getInstance().putOnline(user, false);
                multicaster.sendMessage(new ServerMessage(ServerMessage.USER_LEFT, user));
                // When we're done, for whatever reason, be sure to close
                // the socket, and to notify the ConnectionsKeeper object.  Note that
                // we have to use synchronized first to lock the watcher
                // object before we can call notify() for it.
                try {
                    clientSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                server.removeConnection(this);
                ChatLogger.logDebug("Connection from " + user.getName() + " is dead");
            }
        } else {
            ChatLogger.logDebug("Connection from " + user.getName() + " was not authorized");
        }
    }

    private boolean login() {
        Message message = MessageUtils.getMessage(inStream);
        if (message instanceof LoginMessage) {
            LoginMessage loginMessage = (LoginMessage)message;
            // user/connection host IP on the other side.
            String clientHostIP = clientSocket.getInetAddress().getHostAddress();
            String userName = loginMessage.getUser();
            Boolean power = server.allowUserToConnect(userName, clientHostIP);
            if (power == null) {
                return false;
            }
            user.setName(userName);
            user.setPower(power);
            user.setGroup(server.getUserGroup(userName));
            UserRegistry userRegistry = UserRegistry.getInstance();
            if (!userRegistry.connectUser(user)) {                  
                ChatLogger.logDebug("User " + user + " could not log in from " + clientHostIP);
                return false;
            }
            ChatLogger.logInfo("User " + user.getName() + "(" + user.getId() + ") logged in from " + clientHostIP);
            processMessage(new WelcomeMessage(userRegistry.serialize(), taskRegistry.serialize(), user.getId()));
            List<UserMessage> list = MessageCache.getInstance().getMessages();
            for (UserMessage userMessage : list) {
                processMessage(userMessage);
            }
            return true;
        } else {
            return false;
        }
    }

    public void close() {
        closed = true;
    }
    
    private class PingThread extends Thread {
        private PingThread() {
            super("PingThread " + user.getId());
            setDaemon(true);
        }

        public void run() {
            while(true) {
                replied = false;
//                processMessage(new PingMessage());
                try {
                    sleep(timeout);
                } catch (InterruptedException e) {}
                if (!replied) {
                    ChatLogger.logDebug("Reply is not received - disconnecting client " + user.getName());
                    close();
                    break;
                }
            }
        }
    }
}
