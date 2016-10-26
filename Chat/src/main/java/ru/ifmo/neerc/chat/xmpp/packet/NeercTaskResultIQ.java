package ru.ifmo.neerc.chat.xmpp.packet;

import org.jivesoftware.smack.packet.IQ;

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

    @Override
    protected IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.attribute("id", task.getId());
        xml.attribute("type", result.getType());
        xml.attribute("value", result.getValue());
        xml.rightAngleBracket();

        return xml;
    }
}
