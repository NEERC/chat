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
/*
 * Date: Oct 24, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.config.Config;

/**
 * <code>EOFMessage</code> class
 *
 * @author Matvey Kazakov
 */
public class EOFMessage extends Message{
    
    public static final EOFMessage instance = new EOFMessage();

    public EOFMessage() {
        super(EOF_MESSAGE);
    }

    protected void serialize(Config message) {
    }

    protected void deserialize(Config message) {
    }

    public String asString() {
        return "EOD Message";
    }
}

