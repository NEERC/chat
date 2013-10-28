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
package ru.ifmo.neerc.service;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.util.*;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.*;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.clock.ClockListener;
import ru.ifmo.neerc.service.query.*;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.utils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author Dmitriy Trofimov
 */
public class NEERCComponent implements Component {

    private String myName;
	private static final Logger Log = LoggerFactory.getLogger(NEERCComponent.class);
    private ComponentManager componentManager = null;
    private UserRegistry users = UserRegistry.getInstance();
    private TaskRegistry tasks = TaskRegistry.getInstance();

    private HashMap<String, QueryHandler> handlers = new HashMap<String, QueryHandler>();

	private DocumentFactory docFactory = DocumentFactory.getInstance();
    /**
     * Namespace of the packet extension.
     */
    public static final String NAMESPACE = "http://neerc.ifmo.ru/protocol/neerc";
    public static final String NAME = "neerc";

    public NEERCComponent() {
        this.componentManager = ComponentManagerFactory.getComponentManager();
        myName = NAME + "." + componentManager.getServerName();
    }

    private void initUsers() {
		Log.debug("init start");
        XMPPServer server = XMPPServer.getInstance(); 
        MultiUserChatService service = server.getMultiUserChatManager().getMultiUserChatServices().get(0);
        MUCRoom room = service.getChatRoom("neerc");
        if (room == null) {
           Log.error("no neerc room in MUC");
           return;
        }

        for (JID jid: room.getOwners()) {
            String username = jid.getNode() == null ? jid.toString() : jid.getNode();
            UserEntry user = users.findOrRegister(username);
            user.setPower(true);
            user.setGroup("Admins");
        }

        for (JID jid: room.getAdmins()) {
            String username = jid.getNode() == null ? jid.toString() : jid.getNode();
            UserEntry user = users.findOrRegister(username);
            user.setPower(true);
            user.setGroup("Admins");
        }

        for (JID jid: room.getMembers()) {
            String username = jid.getNode() == null ? jid.toString() : jid.getNode();
            UserEntry user = users.findOrRegister(username);
            user.setGroup("Users");
        }
    }
    
    public Collection<UserEntry> getUsers() {
        return users.getUsers();
    }
    
    public Collection<Task> getTasks() {
        return tasks.getTasks();
    }

    private void initHandlers() {
        handlers.put("users", new UsersQueryHandler());
        handlers.put("tasks", new TasksQueryHandler());
        handlers.put("task", new TaskQueryHandler());
        handlers.put("taskstatus", new TaskStatusQueryHandler());
        handlers.put("ping", new PingQueryHandler());
    }

    public UserEntry getSender(Packet packet) {
        String jid = packet.getFrom().toBareJID();
        if (jid.equals("component." + componentManager.getServerName())) return null;
        return users.findOrRegister(jid);
    }

    public void initialize(JID jid, ComponentManager componentManager) {
        initUsers();
        initHandlers();
    }

    public void start() {
        Message message = new Message();
        message.setFrom(myName);
        message.setTo("admin@" + componentManager.getServerName());
        message.setBody("NEERC Service start");
        sendPacket(message);

        MyListener listener = new MyListener();
        tasks.addListener(listener);
        ClockService clockservice = new ClockService();
        clockservice.addListener(listener);
        clockservice.start();
    }

    public void shutdown() {
    }

    // Component Interface

    public void processPacket(Packet packet) {
        Log.debug("neerc got packet: " + packet.toXML());
        if (packet instanceof IQ) {
            // Handle disco packets
            IQ iq = (IQ) packet;
            // Ignore IQs of type ERROR or RESULT
            if (IQ.Type.error == iq.getType() || IQ.Type.result == iq.getType()) {
                return;
            }
            processIQ(iq);
        } else if (packet instanceof Message) {
            Message message = (Message) packet;
            processMessage(message);
        }
    }

    private void processMessage(Message message) {
/*
        PacketExtension extension = message.getExtension("x", XmlUtils.NAMESPACE_TASKS);
        if (extension != null) {
            tasks.update(XmlUtils.taskFromXml(extension.getElement()));
        }
*/
    }

    private void processIQ(IQ iq) {
        IQ reply = IQ.createResultIQ(iq);
        UserEntry sender = getSender(iq);

        String namespace = iq.getChildElement().getNamespaceURI();
        Element childElement = iq.getChildElement().createCopy();
        reply.setChildElement(childElement);

	    if ("http://jabber.org/protocol/disco#info".equals(namespace)) {
            if (iq.getTo().getNode() == null) {
                // Return service identity and features
                Element identity = childElement.addElement("identity");
                identity.addAttribute("category", "component");
                identity.addAttribute("type", "generic");
                identity.addAttribute("name", "NEERC service");
                childElement.addElement("feature").addAttribute("var", "http://jabber.org/protocol/disco#info");
                for (String key: handlers.keySet()) {
                    childElement.addElement("feature").addAttribute("var", NAMESPACE + "#" + key);
                }
            }
        } else if (sender == null) {
	        reply.setError(PacketError.Condition.forbidden);
	    } else if (namespace.startsWith(NAMESPACE + '#')) {
            String query = namespace.substring(NAMESPACE.length() + 1);
            if (!handlers.containsKey(query)) {
                Log.info("neerc got unknown query " + query);
                reply.setError(PacketError.Condition.service_unavailable);
            } else {
                QueryHandler handler = handlers.get(query);
                handler.processQuery(this, iq, reply, sender);
            }
        } else {
            // Answer an error since the server can't handle the requested
            // namespace
            reply.setError(PacketError.Condition.service_unavailable);
        }
        sendPacket(reply);
    }


    public String getDescription() {
        return "NEERC service";
    }

    public String getName() {
        return "NEERC";
    }

    public void sendPacket(Packet packet) {
        try {
            componentManager.sendPacket(this, packet);
            Log.debug("neerc sent packet: " + packet.toXML());
        } catch (ComponentException e) {
            Log.error(e.getLocalizedMessage());
        }
    }
    
    public void broadcastMessage(String body, PacketExtension extension) {
        for (UserEntry user : users.getUsers()) {
            Message message = new Message();
            message.setFrom(myName);
            message.setBody(body);
            message.setTo(user.getName() + "@" + componentManager.getServerName());
            if (extension != null) {
                message.addExtension(new PacketExtension(extension.getElement().createCopy()));
            }
            sendPacket(message);
        }
    }

    private class MyListener implements TaskRegistryListener, ClockListener {
        @Override
        public void taskChanged(Task task) {
            PacketExtension extension = new PacketExtension("x", XmlUtils.NAMESPACE_TASKS);
            XmlUtils.taskToXml(extension.getElement(), task);
            String body = "Task '" + task.getTitle() + "' (" + task.getId() + ") changed";
            broadcastMessage(body, extension);
        }

        @Override
        public void clockChanged(Clock clock) {
            PacketExtension extension = new PacketExtension("x", XmlUtils.NAMESPACE_CLOCK);
            XmlUtils.clockToXml(extension.getElement(), clock);
            String body = "The clock is ticking";
            broadcastMessage(body, extension);
        }

        @Override
        public void tasksReset() {
        }
    }

}
