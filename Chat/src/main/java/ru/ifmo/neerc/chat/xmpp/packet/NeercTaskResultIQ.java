package ru.ifmo.neerc.chat.xmpp.packet;

import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;

/**
 * @author Dmitriy Trofimov
 */
public class NeercTaskResultIQ extends NeercIQ {
	private Task task;
	private TaskStatus result;
	
	public NeercTaskResultIQ(Task task, TaskStatus result) {
		super("taskstatus", "taskstatus");
		this.task = task;
		this.result = result;
	}

	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName());
		buf.append(" xmlns=\"").append(getNamespace()).append("\"");
		buf.append(" id=\"").append(escape(task.getId())).append("\"");
		buf.append(" type=\"").append(escape(result.getType())).append("\"");
		buf.append(" value=\"").append(escape(result.getValue())).append("\"");
		buf.append(" />");
		return buf.toString();
	}
	
}
