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
 * Date: Nov 4, 2006
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.task;

import ru.ifmo.ips.config.Config;

/**
 * <code>TaskResult</code> class
 *
 * @author Matvey Kazakov
 */
@Deprecated
public abstract class TaskResult {

    public abstract String toString();

    public abstract void serialize(Config node);

    public abstract void deserialize(Config node);


    public abstract boolean actionSupported(int action);

    public abstract void performAction(int action, Object... param);

    public abstract int getVisualState();

}
