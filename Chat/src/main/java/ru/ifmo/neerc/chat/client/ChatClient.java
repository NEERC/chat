package ru.ifmo.neerc.chat.client;

import ru.ifmo.ips.config.Config;
import ru.ifmo.ips.config.XMLConfig;
import ru.ifmo.neerc.chat.UserRegistry;

import javax.swing.*;
import java.io.FileNotFoundException;

/**
 * @author Evgeny Mandrikov
 */
public class ChatClient extends AbstractChatClient {
    public ChatClient() throws FileNotFoundException {
        Config config = new XMLConfig(DEFAULT_CONFIG_FILE);
        localHistorySize = config.getInt("history", -1);
        ClientReader clientReader = new ClientReader(this);
        int userId;
        try {
            userId = clientReader.connect(taskRegistry, config.getProperty("host"),
                    config.getInt("port"), config.getProperty("user"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error connecting to server",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }
        user = UserRegistry.getInstance().search(userId);

        chat = clientReader;

        setupUI();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new ChatClient().setVisible(true);
    }
}
