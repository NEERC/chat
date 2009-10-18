/*
 * Date: Nov 18, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin.crisscross;

import ru.ifmo.neerc.chat.MessageListener;
import ru.ifmo.neerc.chat.UserEntry;
import ru.ifmo.neerc.chat.UserRegistry;
import ru.ifmo.neerc.chat.plugin.ChatPlugin;
import ru.ifmo.neerc.chat.plugin.CustomMessage;
import ru.ifmo.neerc.chat.plugin.CustomMessageData;
import ru.ifmo.neerc.chat.plugin.CustomDataFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.Collection;

/**
 * <code>CrissCrossPlugin</code> class
 *
 * @author Matvey Kazakov
 */
public class CrissCrossPlugin implements ChatPlugin {

    public static final String DATATYPE = "CrissCross";
    public static final String DATATYPE_TURN = "CrissCrossTurn";

    static {
        CustomDataFactory.registerCustomData(DATATYPE, CrissCrossData.class);
        CustomDataFactory.registerCustomData(DATATYPE_TURN, CrissCrossTurnData.class);
    }

    public static final Icon icon = new ImageIcon(CrissCrossPlugin.class.getResource("cc.gif"));
    private JComponent panel;
    private MessageListener listener;
    private CCBoard board = new CCBoard();
    private JButton btnInvite;
    private JButton btnQuit;
    private UserEntry user;
    private UserEntry opponent;
    private JComponent parent;
    
    public static final int STATE_INITIAL = 0;
    public static final int STATE_OPEN = 1;
    public static final int STATE_GAME = 2;
    
    private int state = STATE_INITIAL;
    private JFrame frame;

    public void init(MessageListener listener, int userId, JComponent parent) {
        this.listener = listener;
        this.parent = parent;
        panel = createMainPanel();
        user = UserRegistry.getInstance().search(userId);
    }

    private JComponent createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(board, new GridBagConstraints(0, 0, 1, 3, 1, 1, GridBagConstraints.NORTHWEST, 
                GridBagConstraints.BOTH, new Insets(10, 10, 10, 0), 0, 0));
        btnInvite = new JButton("Invite");
        btnQuit = new JButton("Quit");
        btnQuit.setEnabled(false);
        btnQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        
        btnInvite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnInvite.setEnabled(false);
                int userId = chooseOpponent();
                if (userId >= 0) {
                    listener.processMessage(new CustomMessage(userId, CrissCrossData.createData(user.getId(),
                            CrissCrossData.WELCOME)));
                } else {
                    btnInvite.setEnabled(true);
                }
            }
        });
        panel.add(btnInvite, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
        panel.add(btnQuit, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 10), 0, 0));
        panel.add(Box.createVerticalGlue(), new GridBagConstraints(1, 2, 1, 1, 0, 1, GridBagConstraints.NORTH,
                GridBagConstraints.VERTICAL, new Insets(0, 10, 10, 10), 0, 0));
        return panel;
    }

    private int chooseOpponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(Box.createHorizontalGlue(), new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JDialog dialog = new JDialog(frame, "Choose opponent:", true);
        final JButton btnOK = new JButton("OK");
        final int[] result = new int[]{-1};
        Collection<UserEntry> users = UserRegistry.getInstance().getUsers();
        Vector<UserEntry> userEntryVector = new Vector<UserEntry>();
        for (UserEntry userEntry : users) {
            if (userEntry.getId() != user.getId() && userEntry.isOnline()) {
                userEntryVector.add(userEntry);
            }
        }
        final JList list = new JList(userEntryVector);
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                UserEntry selectedUser = (UserEntry)list.getSelectedValue();
                if (selectedUser != null) {
                    result[0] = selectedUser.getId();
                }
                dialog.dispose();
            }
        });
        btnOK.setEnabled(false);
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                btnOK.setEnabled(list.getSelectedValue() != null);
            }
        });
        
        panel.add(new JScrollPane(list), new GridBagConstraints(0, 0, 3, 1, 1, 1, GridBagConstraints.NORTHWEST, 
                GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
        panel.add(btnOK, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, 
                GridBagConstraints.NONE, new Insets(0, 10, 10, 10), 0, 0));
        panel.add(btnCancel, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, 
                GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));
        dialog.setContentPane(panel);
        
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    public Class<CustomMessageData> getCustomDataClass() {
        return null;
    }

    public Icon getIcon() {
        return icon;
    }

    public void start() {
        start1();
    }

    private void start1(boolean accepted) {
        if (state == STATE_INITIAL) {
            frame = new JFrame("Network Criss Cross");
            frame.setContentPane(panel);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(parent);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    frame.dispose();
                    quit();
                }
            });
            frame.setVisible(true);
        } else if (state == STATE_OPEN) {
            btnInvite.setEnabled(false);
            btnQuit.setEnabled(true);
        }
        if (accepted) {
            btnQuit.setEnabled(true);
            btnInvite.setEnabled(false);
        }
        state = STATE_GAME;
    }

    private void quit() {
        state = STATE_INITIAL;
        if (opponent != null) {
            listener.processMessage(new CustomMessage(opponent.getId(), 
                CrissCrossData.createData(user.getId(), CrissCrossData.QUIT))); 
        }
        btnInvite.setEnabled(true);
    }

    public boolean accept(Class<? extends CustomMessageData> aClass) {
        return aClass == CrissCrossData.class;
    }

    public void processMessage(CustomMessage message) {
        CrissCrossData crossData = (CrissCrossData)message.getData();
        int from = crossData.getFrom();
        if (crossData.getType() == CrissCrossData.WELCOME) {
            if (state == STATE_INITIAL || state == STATE_OPEN) {
                int answer = JOptionPane.showConfirmDialog(panel,
                        "User " + UserRegistry.getInstance().search(from).getName() + " wants to play Criss Cross with you. Are you agree?", "Welcome to game",
                        JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    opponent = UserRegistry.getInstance().search(from);
                    start1();            
                    listener.processMessage(new CustomMessage(from, CrissCrossData.createData(user.getId(), CrissCrossData.ACCEPT)));
                } else {
                    listener.processMessage(new CustomMessage(from, CrissCrossData.createData(user.getId(), CrissCrossData.DENY)));
                }
            } else {
                listener.processMessage(new CustomMessage(from, CrissCrossData.createData(user.getId(), CrissCrossData.BUSY)));
            }
        } else if  (crossData.getType() == CrissCrossData.ACCEPT) {
            JOptionPane.showMessageDialog(panel,
                    "User " + UserRegistry.getInstance().search(from).getName() + " accepted your invitation. The game is started.", 
                    "Welcome to game", JOptionPane.INFORMATION_MESSAGE);
            state = STATE_GAME;
            btnQuit.setEnabled(true);
        } else if  (crossData.getType() == CrissCrossData.DENY) {
            JOptionPane.showMessageDialog(panel,
                    "User " + UserRegistry.getInstance().search(from).getName() + " denied your invitation. Sorry.", 
                    "Welcome to game", JOptionPane.INFORMATION_MESSAGE);
            state = STATE_OPEN;
            btnInvite.setEnabled(true);
        } else if  (crossData.getType() == CrissCrossData.BUSY) {
            JOptionPane.showMessageDialog(panel,
                    "User " + UserRegistry.getInstance().search(from).getName() + " is now busy wih playing with other user. Sorry.", 
                    "Welcome to game", JOptionPane.INFORMATION_MESSAGE);
            state = STATE_OPEN;
            btnInvite.setEnabled(true);
        } else if  (crossData.getType() == CrissCrossData.TURN) {
            CrissCrossTurnData crossTurnData = (CrissCrossTurnData)crossData;
            board.makeOppositeTurn(crossTurnData.getX(), crossTurnData.getY());
        }
    }

}

