package ru.ifmo.neerc.service;

import org.dom4j.Element;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;

import java.util.Map;

/**
 * @author Evgeny Mandrikov
 */
public final class XmlUtils {
    public static final String NAMESPACE = "http://neerc.ifmo.ru/protocol/neerc";
    public static final String NAMESPACE_TASKS = NAMESPACE + "#tasks";

    /**
     * Hide utility class contructor.
     */
    private XmlUtils() {
    }

    public static void taskToXml(Element parent, Task task) {
        Element taskElement = parent.addElement("task");
        taskElement.addAttribute("id", task.getId());
        taskElement.addAttribute("title", task.getTitle());
        taskElement.addAttribute("type", task.getType());
        for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
            TaskStatus status = entry.getValue();
            Element statusElement = taskElement.addElement("status");
            statusElement.addAttribute("from", entry.getKey());
            statusElement.addAttribute("type", status.getType());
            statusElement.addAttribute("value", status.getValue());
        }
    }

    public static void userToXml(Element parent, UserEntry user) {
        Element userElement = parent.addElement("user");
        userElement.addAttribute("name", user.getName());
        userElement.addAttribute("group", user.getGroup());
        userElement.addAttribute("power", user.isPower() ? "yes" : "no");
    }

    public static Task taskFromXml(Element element) {
        Element taskElement = element.element("task");

        String id = taskElement.attributeValue("id");
        String type = taskElement.attributeValue("type");
        String title = taskElement.attributeValue("title");
        Task task = new Task(id, type, title);

        for (Object childElement : taskElement.elements()) {
            Element child = (Element) childElement;
            if ("status".equals(child.getName())) {
                String from = child.attributeValue("from");
                type = child.attributeValue("type");
                String value = child.attributeValue("value");
                task.setStatus(from, type, value);
            }
        }
        return task;
    }
}
