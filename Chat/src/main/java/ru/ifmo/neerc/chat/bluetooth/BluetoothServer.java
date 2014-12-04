package ru.ifmo.neerc.chat.bluetooth;

import ru.ifmo.neerc.chat.client.ChatMessage;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.bluetooth.*;
import javax.microedition.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BluetoothServer implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(BluetoothServer.class);

    public final UUID uuid = new UUID("5c98193c68f07c749292aa74e2387fba", false);
    public final String name = "NEERC";
    public final String url = "btspp://localhost:" + uuid + ";name=" + name;

    private Map<StreamConnection, ObjectOutputStream> clients;
    private LinkedList<ChatMessage> history;

    public BluetoothServer() {
        clients = new HashMap<StreamConnection, ObjectOutputStream>();
        history = new LinkedList<ChatMessage>();
    }

    public void broadcastMessage(ChatMessage chatMessage) {
        LOG.debug("broadcasting message");

        history.add(chatMessage);

        if (history.size() > 10)
            history.removeFirst();

        Iterator<Map.Entry<StreamConnection, ObjectOutputStream>> iter = clients.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<StreamConnection, ObjectOutputStream> entry = iter.next();

            try {
                entry.getValue().writeObject(chatMessage);
            } catch (IOException e) {
                iter.remove();
            }
        }
    }

    public void run() {
        LocalDevice device = null;

        while (true) {
            try {
                device = LocalDevice.getLocalDevice();
            } catch (BluetoothStateException e) {}

            if (device != null && device.isPowerOn()) {
                try {
                    StreamConnectionNotifier server = (StreamConnectionNotifier)Connector.open(url);

                    LOG.info("RFCOMM server started");

                    while (true) {
                        StreamConnection conn = server.acceptAndOpen();

                        RemoteDevice remote = RemoteDevice.getRemoteDevice(conn);
                        String name = remote.getFriendlyName(true);
                        if (name != null)
                            name = " (" + name + ")";
                        else
                            name = "";
                        LOG.info("Device " + remote.getBluetoothAddress() + name + " connected");

                        ObjectOutputStream oos = new ObjectOutputStream(conn.openOutputStream());

                        for (ChatMessage chatMessage : history) {
                            try {
                                oos.writeObject(chatMessage);
                            } catch (IOException e) {
                                e.printStackTrace();
                                continue;
                            }
                        }

                        clients.put(conn, oos);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {}
        }
    }
}
