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

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.*;
import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.service.query.*;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.utils.XmlUtils;

import java.util.*;

/**
 * @author Dmitriy Trofimov
 */
public class NEERCComponent implements Component {

    private String myName;
    private ComponentManager componentManager = null;
    private UserRegistry users = UserRegistry.getInstance();
    private TaskRegistry tasks = TaskRegistry.getInstance();

    private HashMap<String, QueryHandler> handlers = new HashMap<String, QueryHandler>();

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
        XMPPServer server = XMPPServer.getInstance(); 
        MultiUserChatService service = server.getMultiUserChatManager().getMultiUserChatServices().get(0);
        MUCRoom room = service.getChatRoom("neerc");
        if (room == null) {
           componentManager.getLog().error("no neerc room in MUC");
           return;
        }

        for (String jid: room.getOwners()) {
            String username = getUsernameFromJID(jid);
            UserEntry user = users.findOrRegister(username);
            user.setPower(true);
            user.setGroup("Admins");
        }
        for (String jid: room.getAdmins()) {
            String username = getUsernameFromJID(jid);
            UserEntry user = users.findOrRegister(username);
            user.setPower(true);
            user.setGroup("Admins");
        }
        for (String jid: room.getMembers()) {
            String username = getUsernameFromJID(jid);
            UserEntry user = users.findOrRegister(username);
            user.setGroup("Users");
        }
    }

    private String getUsernameFromJID(String jid) {
        int pos = jid.indexOf('@');
        return pos == -1 ? jid : jid.substring(0, pos);
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
        // TODO: moar of 'em
    }

    public UserEntry getSender(Packet packet) {
        // TODO: identify sender and get his UserEntry
        return null;
    }

    public void initialize(JID jid, ComponentManager componentManager) {
        initUsers();
        initHandlers();
        tasks.addListener(new MyTaskRegistryListener());
    }

    public void start() {
        Message message = new Message();
        message.setFrom(myName);
        message.setTo("admin@" + componentManager.getServerName());
        message.setBody("NEERC Service start");
        sendPacket(message);
    }

    public void shutdown() {
    }

    // Component Interface

    public void processPacket(Packet packet) {
        componentManager.getLog().debug("neerc got packet: " + packet.toXML());
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
        PacketExtension extension = message.getExtension("x", XmlUtils.NAMESPACE_TASKS);
        if (extension != null) {
            tasks.update(XmlUtils.taskFromXml(extension.getElement()));
        }
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
        } else if (namespace.startsWith(NAMESPACE + '#')) {
            String query = namespace.substring(NAMESPACE.length() + 1);
            if (!handlers.containsKey(query)) {
                componentManager.getLog().info("neerc got unknown query " + query);
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
        componentManager.getLog().debug("neerc sent packet: " + reply.toXML());
    }


    public String getDescription() {
        return "NEERC service";
    }

    public String getName() {
        return "NEERC";
    }

    private void sendPacket(Packet packet) {
        try {
            componentManager.sendPacket(this, packet);
        } catch (ComponentException e) {
            componentManager.getLog().error(e);
        }
    }

    private class MyTaskRegistryListener implements TaskRegistryListener {
        private Message createMessage(String to, Task task) {
            Message message = new Message();
            message.setFrom(myName);
            message.setBody("Task '" + task.getTitle() + "' (" + task.getId() + ") changed");
            message.setTo(to);
            PacketExtension extension = new PacketExtension("x", XmlUtils.NAMESPACE_TASKS);
            XmlUtils.taskToXml(extension.getElement(), task);
            message.addExtension(extension);
            return message;
        }

        @Override
        public void taskChanged(Task task) {
            // Broadcast
            for (UserEntry user : users.getUsers()) {
                sendPacket(createMessage(user.getName() + "@" + componentManager.getServerName(), task));
            }
        }
    }

}
