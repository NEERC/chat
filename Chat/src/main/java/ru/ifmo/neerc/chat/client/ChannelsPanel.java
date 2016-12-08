package ru.ifmo.neerc.chat.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ChannelsPanel extends JPanel {

    private ChannelList channelList;

    public ChannelsPanel(ChannelList channelList) {
        this.channelList = channelList;

        setLayout(new BorderLayout());
        add(new JScrollPane(createList()), BorderLayout.CENTER);
    }

    private JList createList() {
        final JList<String> list = new JList<String>();
        ChannelListData model = new ChannelListData();
        list.setModel(model);
        list.setCellRenderer(new ChannelListCellRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        channelList.addListener(model); 
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() % 2 != 0)
                    return;

                String channel = (String)list.getSelectedValue();
                if (channelList.isSubscribed(channel))
                    channelList.unsubscribeFrom(channel);
                else
                    channelList.subscribeTo(channel);
            }
        });

        return list;
    }

    private class ChannelListData extends AbstractListModel<String> implements SubscriptionListener {

        private String[] channels;

        public ChannelListData() {
            update();
        }

        @Override
        public synchronized int getSize() {
            return channels.length;
        }

        @Override
        public synchronized String getElementAt(int index) {
            return channels[index];
        }

        @Override
        public void subscriptionChanged() {
            update();
        }

        public void update() {
            channels = channelList.getChannels().toArray(new String[0]);
            Arrays.sort(channels);
            fireContentsChanged(this, 0, getSize());
        }
    }

    private class ChannelListCellRenderer extends JCheckBox implements ListCellRenderer {

        protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            final String channel = (String) value;

            Font font = list.getFont();
            if (channelList.isSubscribed(channel)) {
                font = font.deriveFont(Font.BOLD);
                setSelected(true);
            } else {
                font = font.deriveFont(Font.PLAIN);
                setSelected(false);
            }
            setFont(font);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(channel);
            setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

            return this;
        }
    }
}
