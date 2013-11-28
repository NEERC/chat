package ru.ifmo.neerc.chat.xmpp.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

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
			buf.append(" id=\"").append(escape(task.getId())).append("\"");
			buf.append(" type=\"").append(escape(task.getType())).append("\"");
			buf.append(" title=\"").append(escape(task.getTitle())).append("\">");
			for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
				TaskStatus status = entry.getValue();
				buf.append("<status");
				buf.append(" for=\"").append(escape(entry.getKey())).append("\"");
				buf.append(" type=\"").append(escape(status.getType())).append("\"");
				buf.append(" value=\"").append(escape(status.getValue())).append("\" />");
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
		Date date = new Date();
    	String timestamp = parser.getAttributeValue("", "timestamp");
    	if (timestamp != null) {
    		date = new Date(Long.parseLong(timestamp));
    	}
		Task task = new Task(
			parser.getAttributeValue("", "id"),
			parser.getAttributeValue("", "type"),
			parser.getAttributeValue("", "title"),
			date
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
