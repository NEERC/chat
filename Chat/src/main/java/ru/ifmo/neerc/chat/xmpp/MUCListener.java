package ru.ifmo.neerc.chat.xmpp;

import java.util.Date;

import org.jivesoftware.smackx.muc.MUCRole;

/**
 * @author Evgeny Mandrikov
 */
public interface MUCListener {
    void connected(XmppChat chat);

    void roleChanged(String jid, MUCRole role);

    void joined(String participant);

    void left(String participant);

    void messageReceived(String jid, String message, Date timestamp);

    void historyMessageReceived(String jid, String message, Date timestamp);
}
