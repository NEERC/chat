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
package ru.ifmo.neerc.service.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.service.NEERCComponent;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class TasksQueryHandler implements QueryHandler {
    
    public void processQuery(NEERCComponent component, IQ iq, IQ reply, UserEntry sender, String roomName) {
		TaskRegistry taskRegistry = TaskRegistry.getInstanceFor(roomName);
        Element childElement = reply.getChildElement();
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(taskRegistry.getTasks());
        Collections.sort(tasks);
        for (Task task : tasks) {
            XmlUtils.taskToXml(childElement, task);
        }
    }
}
