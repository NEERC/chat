package ru.ifmo.neerc.chat.client;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatInputArea extends JTextArea {
    private AbstractChatClient client;
    private String prefix;

    private final MessageLocalHistory messageLocalHistory = new MessageLocalHistory(0);

    private class KeyListener extends KeyAdapter {

        public void keyTyped(KeyEvent e1) {
            if (e1.getKeyChar() == KeyEvent.VK_ENTER) {
                boolean hasCtrl = e1.isControlDown();
                if (hasCtrl != client.sendOnEnter()) {
                    String text = getText().trim();
                    if (text.isEmpty())
                        return;

                    Matcher privateMatcher = Pattern.compile(ChatMessage.PRIVATE_FIND_REGEX, Pattern.DOTALL).matcher(text);
                    if (privateMatcher.find() && privateMatcher.groupCount() > 0) {
                        messageLocalHistory.setLastPrivateAddressees(privateMatcher.group(1));
                    }

                    messageLocalHistory.add(text);
                    client.send(prefix + text);
                    setText("");
                } else {
                    if (hasCtrl)
                        append("\n");
                }
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP && !e.isControlDown()) {
                try {
                    int caretPosition = getCaretPosition();
                    int lineNum = getLineOfOffset(caretPosition);
                    if (lineNum == 0) {
                        setPrivateAddressees(messageLocalHistory.getLastPrivateAddressees());
                    }

                } catch (BadLocationException e1) {
                    // it's not like anything can be done here
                }
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.isControlDown()) {
                String message = messageLocalHistory.moveDown();
                if (message != null) {
                    setText(message);
                }
            } else if (e.getKeyCode() == KeyEvent.VK_UP && e.isControlDown()) {
                String message = messageLocalHistory.moveUp();
                if (message != null) {
                    setText(message);
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                setText("");
            }
        }
    }

    public ChatInputArea(AbstractChatClient client, String user) {
        super(2, 45);
        setLineWrap(true);
        setWrapStyleWord(true);
        addKeyListener(new KeyListener());

        this.client = client;
        if (user == null || user.isEmpty())
            this.prefix = "";
        else
            this.prefix = user + "> ";
    }

    public void setPrivateAddressees(String addressees) {
        if (!"".equals(addressees)) {
            String text = getText();
            text = text.replaceAll("\\A[a-zA-Z0-9%]+>\\s*", "");
            text = addressees + "> " + text;
            setText(text);
        }
        requestFocus();
    }
}
