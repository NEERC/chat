package ru.ifmo.neerc.task;

import org.dom4j.Element;
import org.testng.annotations.Test;
import org.xmpp.packet.IQ;

import java.util.Map;

/**
 * @author Evgeny Mandrikov
 */
public class TaskTest {

    @Test
    public void test() {
        Task task = new Task();
        task.setTitle("Do some work");

        task.setStatus("godin", "fail", ":(");
        task.setStatus("hall3", "success", ":)");

        IQ iq = new IQ();
        Element taskElement = iq.getElement().addElement("task");
        taskElement.addAttribute("title", task.getTitle());
        taskElement.addAttribute("type", task.getType());

        for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
            TaskStatus status = entry.getValue();
            Element statusElement = taskElement.addElement("status");
            statusElement.addAttribute("from", entry.getKey());
            statusElement.addAttribute("type", status.getType());
            statusElement.addAttribute("type", status.getValue());
        }

        System.out.println(iq.toXML());
    }

}
