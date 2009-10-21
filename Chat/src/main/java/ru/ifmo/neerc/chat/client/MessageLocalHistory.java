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
 * Date: 26.11.2004
 */
package ru.ifmo.neerc.chat.client;

import java.util.ArrayList;

/**
 * @author Matvey Kazakov
 */
public class MessageLocalHistory extends ArrayList<String> {
    
    public static final int DEFAULT_MAX_SIZE=20;
    
    private int maxSize;
    private int cursor;

    public MessageLocalHistory(int maxSize) {
        super(checkSize(maxSize));
        this.maxSize = checkSize(maxSize);
        this.cursor = this.maxSize;
    }

    private static int checkSize(int maxSize) {
        return maxSize > 0 ? maxSize : DEFAULT_MAX_SIZE;
    }
    
    public boolean add(String message) {
        if (size() >= maxSize) {
            remove(0); // remove latest message
        }
        boolean b = super.add(message);
        reset();
        return b;
    }
    
    public String moveUp() {
        if (cursor > 0) {
            cursor--;
            return getMessage();
        } else {
            return null;
        }
    }
    public String moveDown() {
        if (cursor < size() - 1) {
            cursor++;
            return getMessage();
        } else {
            return null;
        }
    }
    
    public String getMessage() {
        if (cursor < size()) {
            return (String)get(cursor);
        } else {
            return null;
        }
    }
    
    public void reset() {
        cursor = size();
    }

}
