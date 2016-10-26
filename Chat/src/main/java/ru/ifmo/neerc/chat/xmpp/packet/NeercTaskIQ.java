package ru.ifmo.neerc.chat.xmpp.packet;

import java.util.Map;

import org.jivesoftware.smack.packet.IQ;

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

    @Override
    protected IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.attribute("title", task.getTitle());
        xml.attribute("type", task.getType());
        xml.optAttribute("id", task.getId());
        xml.rightAngleBracket();

        for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
            xml.halfOpenElement("status");
            xml.attribute("for", entry.getKey());
            xml.attribute("type", entry.getValue().getType());
            xml.optAttribute("value", entry.getValue().getValue());
            xml.closeEmptyElement();
        }

        return xml;
    }
}
