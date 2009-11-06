package ru.ifmo.neerc.chat.xmpp.packet;

import java.util.*;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;


/**
 * @author Dmitriy Trofimov
 */
public class NeercTaskIQ extends NeercIQ {
	private Task task;
	
	public NeercTaskIQ(Task task) {
		super("task", "task");
		this.task = task;
	}

	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName());
		buf.append(" xmlns=\"").append(getNamespace()).append("\"");
		if (task.getId() != null) {
			buf.append(" id=\"").append(task.getId()).append("\"");
		}
		buf.append(" type=\"").append(task.getType()).append("\"");
		buf.append(" title=\"").append(task.getTitle()).append("\"");
		buf.append(">");

		Map<String, TaskStatus> statuses = task.getStatuses();
		for (String user: statuses.keySet()) {
			TaskStatus status = statuses.get(user);
			buf.append("<status ");
			buf.append(" for=\"").append(user).append("\"");
			buf.append(" type=\"").append(status.getType()).append("\"");
			buf.append(" value=\"").append(status.getValue()).append("\"");
			buf.append(" />");
		}
		buf.append("</").append(getElementName()).append(">");
		return buf.toString();
	}
	
}
