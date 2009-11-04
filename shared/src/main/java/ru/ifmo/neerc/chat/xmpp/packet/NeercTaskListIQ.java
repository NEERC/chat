package ru.ifmo.neerc.chat.xmpp.packet;

import java.util.*;
import org.jivesoftware.smack.packet.IQ;
import org.xmlpull.v1.XmlPullParser;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NeercTaskListIQ extends NeercIQ {
	private Collection<Task> tasks = new ArrayList<Task>();

	public NeercTaskListIQ() {
		super("tasks");
	}
	public String getElementName() {
		return "query";
	}

	public String getNamespace() {
		return XmlUtils.NAMESPACE_TASKS;
	}

	public Collection<Task> getTasks() {
		return Collections.unmodifiableCollection(tasks);
	}
 
	public void addTask(Task task) {
		tasks.add(task);
	}

	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
		for (Task task: tasks) {
			buf.append("<task");
			buf.append(" id=\"").append(task.getId()).append("\"");
			buf.append(" type=\"").append(task.getType()).append("\"");
			buf.append(" title=\"").append(task.getTitle()).append("\">");
			for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
				TaskStatus status = entry.getValue();
				buf.append("<status");
				buf.append(" for=\"").append(entry.getKey()).append("\"");
				buf.append(" type=\"").append(status.getType()).append("\"");
				buf.append(" value=\"").append(status.getValue()).append("\" />");
			}
			buf.append("</task>");
		}
		buf.append("</").append(getElementName()).append(">");
		return buf.toString();
	}

	public void parse(XmlPullParser parser) throws Exception {
		boolean done = false;
		while (!done) {
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("task")) {
					addTask(parseTask(parser));
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().equals("query")) {
					done = true;
				}
			}
		}
    }

	public static Task parseTask(XmlPullParser parser) throws Exception {
		Task task = new Task(
			parser.getAttributeValue("", "id"),
			parser.getAttributeValue("", "type"),
			parser.getAttributeValue("", "title")
		);
		boolean done = false;
		while (!done) {
			int eventType = parser.next();
			if (eventType == XmlPullParser.START_TAG) {
				if (parser.getName().equals("status")) {
					task.setStatus(
						parser.getAttributeValue("", "for"),
						parser.getAttributeValue("", "type"),
						parser.getAttributeValue("", "value")
					);
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (parser.getName().equals("task")) {
					done = true;
				}
			}
		}
		return task;
	}
}
