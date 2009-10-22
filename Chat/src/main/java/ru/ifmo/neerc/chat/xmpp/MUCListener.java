package ru.ifmo.neerc.chat.xmpp;

import java.util.Date;

/**
 * @author Evgeny Mandrikov
 */
public interface MUCListener {
    void roleChanged(String jid, String role);

    void joined(String participant);

    void left(String participant);

    void messageReceived(String jid, String message, Date timestamp);

    void historyMessageReceived(String jid, String message, Date timestamp);

    void taskReceived(byte[] bytes, Date timestamp);
}
