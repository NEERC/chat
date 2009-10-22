package ru.ifmo.neerc.utils;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;
import ru.ifmo.neerc.utils.XmlUtils;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskStatus;

import java.util.Map;

/**
 * @author Evgeny Mandrikov
 */
public final class XmlUtilsTest {

    @Test
    public void test() {
        Task task = new Task("0", "todo", "Do some work");
        task.setStatus("godin", "fail", ":(");
        task.setStatus("hall3", "success", ":)");

        Message message = new Message();
        PacketExtension extension = new PacketExtension("x", XmlUtils.NAMESPACE_TASKS);
        message.addExtension(extension);
        XmlUtils.taskToXml(extension.getElement(), task);
        System.out.println(message.toXML());

        Task d = XmlUtils.taskFromXml(extension.getElement());
        Assert.assertEquals(d.getId(), task.getId());
        Assert.assertEquals(d.getType(), task.getType());
        Assert.assertEquals(d.getTitle(), task.getTitle());

        Assert.assertEquals(d.getStatuses().size(), task.getStatuses().size());
        for (Map.Entry<String, TaskStatus> entry : task.getStatuses().entrySet()) {
            TaskStatus ds = d.getStatuses().get(entry.getKey());
            TaskStatus status = entry.getValue();
            Assert.assertEquals(ds.getType(), status.getType());
            Assert.assertEquals(ds.getValue(), status.getValue());
        }
    }

}
