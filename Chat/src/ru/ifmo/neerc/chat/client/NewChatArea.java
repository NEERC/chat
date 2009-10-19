/*
   Copyright 2009 NEERC team

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
// $Id$
/**
 * Date: 24.10.2005
 */
package ru.ifmo.neerc.chat.client;

import java.util.ArrayList;

/**
 * @author Matvey Kazakov
 */
public class NewChatArea extends MyJTable<ChatMessage> {
//    private TableCellRenderer cellRenderer;

    public NewChatArea() {
        super(new ArrayList<ChatMessage>());
        setAppendToTheEnd(true);
        setRenderer(new ChatMessageRenderer());
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
