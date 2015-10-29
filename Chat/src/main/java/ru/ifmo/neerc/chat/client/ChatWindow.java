package ru.ifmo.neerc.chat.client;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import java.util.List;
import java.util.ArrayList;

public class ChatWindow extends JFrame {
    private ChatArea outputArea;
    private ChatInputArea inputArea;

    public ChatWindow(AbstractChatClient client, String user) {
        outputArea = new ChatArea();
        inputArea = new ChatInputArea(client, user);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                             new JScrollPane(outputArea),
                                             new JScrollPane(inputArea));
        splitter.setResizeWeight(1);
        splitter.setDividerSize(2);

        setContentPane(splitter);
        setTitle(user);
        setSize(400, 400);
        setLocationRelativeTo(client);
    }

    public void addMessage(ChatMessage chatMessage) {
        outputArea.addMessage(chatMessage);
    }
}
