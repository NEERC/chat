// $Id: ClientReader.java,v 1.7 2007/10/28 07:32:11 matvey Exp $
/**
 * Date: 24.10.2004
 */
package ru.ifmo.neerc.chat.client;

import ru.ifmo.ips.IpsRuntimeException;
import ru.ifmo.neerc.chat.MessageUtils;
import ru.ifmo.neerc.chat.TaskRegistry;
import ru.ifmo.neerc.chat.UserRegistry;
import ru.ifmo.neerc.chat.ChatLogger;
import ru.ifmo.neerc.chat.message.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Matvey Kazakov
 */
class ClientReader extends Thread {
    private Socket socket;
    private ChatClient chatClient;
    private OutputStream out;
    
    private final Pinger pinger = new Pinger();

    public ClientReader(ChatClient listener) {
        super("chatclient Reader");
        this.chatClient = listener;
        setDaemon(true);
    }

    public void run() {
        InputStream in = null;
        try {
            in = socket.getInputStream();
            while (true) {
                Message message = MessageUtils.getMessage(in);
                if (message instanceof EOFMessage) {
                    break;
                }
                if (message instanceof PingMessage) {
                    write(message);
                } else if (message != null) {
                    chatClient.processMessage(message);
                }
            }
        } catch (IOException e) {
            System.out.println("Reader: " + e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {}
            chatClient.connectionLost();
        }
    }


    public int connect(TaskRegistry taskRegistry, String host, int port, String user) {
        int userId = -1;
        try {
            socket = new Socket(host, port);
            socket.setSoTimeout(1000);
            socket.setKeepAlive(true);
            out = socket.getOutputStream();
            out.write(MessageFactory.getInstance().serialize(new LoginMessage(user)));
            Message message = MessageUtils.getMessage(socket.getInputStream());
            if (message instanceof WelcomeMessage) {
                WelcomeMessage welcomeMessage = ((WelcomeMessage)message);
                UserRegistry.getInstance().init(welcomeMessage.getEntries());
                taskRegistry.init(welcomeMessage.getTasks());
                userId = welcomeMessage.getUserId(); 

                // Give the reader a higher priority to work around
                // a problem with shared access to the console.
                setPriority(3);

                start();
                pinger.start();
            } else {
                socket.close();
                throw new IpsRuntimeException("Connection refused");
            }
        } catch (Exception e) {
            throw new IpsRuntimeException("Could not connect: " + e.getMessage());
        }
        return userId;
    }

    public void write(Message message) {
        try {
            out.write(MessageFactory.getInstance().serialize(message));
        } catch (IOException e) {
            ChatLogger.logError(e.getMessage());
            e.printStackTrace();
            chatClient.connectionLost();
        }

    }

    private class Pinger extends Thread{

        public Pinger() {
            super("Server ping thread");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                write(new PingMessage());
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}

