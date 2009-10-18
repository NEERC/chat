// $Id$
/**
 * Date: 24.10.2005
 */
package ru.ifmo.neerc.chat.client;

import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;

/**
 * @author Matvey Kazakov
 */
public class NewChatArea extends MyJTable<ChatMessage> {
    private TableCellRenderer cellRenderer;

    public NewChatArea() {
        super(new ArrayList<ChatMessage>());
        setAppendToTheEnd(true);
        cellRenderer = new ChatMessageRenderer();
        setRenderer(cellRenderer);
    }


    public void addMessage(final ChatMessage message) {
        addElement(message);
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                Component tableCellRendererComponent = cellRenderer.getTableCellRendererComponent(
//                        getTable(), message, false, false, index, 0);
////                tableCellRendererComponent.setSize(getWidth(), tableCellRendererComponent.getHeight());
//                int height = tableCellRendererComponent.getPreferredSize().height;
//                synchronized (ChatArea.this) {
//                    setRowHeight(index, height);
//                    Rectangle cellRectPrev = getCellRect(index - 1, 0, false);
//                    Rectangle cellRect = getCellRect(index, 0, false);
//                    Rectangle visibleRect = getVisibleRect();
//                    if (visibleRect.y + visibleRect.height >= cellRectPrev.y + cellRectPrev.height) {
//                        scrollRectToVisible(cellRect);
//                    }
//                }
//            }
//        });
    }


}

