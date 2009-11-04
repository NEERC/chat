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

import org.dom4j.Element;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.service.NEERCComponent;
import ru.ifmo.neerc.task.*;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class TaskStatusQueryHandler implements QueryHandler {
    
	public void processQuery(NEERCComponent component, IQ iq, IQ reply, UserEntry sender) {
		TaskRegistry tasks = TaskRegistry.getInstance();
		Element childElement = iq.getChildElement();
		String name = sender.getName();
		String id = childElement.attributeValue("id");
		String type = childElement.attributeValue("type");
		String value = childElement.attributeValue("value");
		Task task = tasks.getById(id);
		if (task == null) {
			reply.setError(PacketError.Condition.item_not_found);
			return;
		}
		TaskStatus status = task.getStatus(name);
		if (status == null) {
			reply.setError(PacketError.Condition.forbidden);
			return;
		}
		task.setStatus(name, type, value);
		tasks.update(task);
	}
}
