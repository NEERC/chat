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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupJID;
import org.jivesoftware.openfire.group.GroupManager;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.muc.MUCRole;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketExtension;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;
import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.clock.ClockListener;
import ru.ifmo.neerc.service.query.PingQueryHandler;
import ru.ifmo.neerc.service.query.QueryHandler;
import ru.ifmo.neerc.service.query.TaskQueryHandler;
import ru.ifmo.neerc.service.query.TaskStatusQueryHandler;
import ru.ifmo.neerc.service.query.TasksQueryHandler;
import ru.ifmo.neerc.service.query.UsersQueryHandler;
import ru.ifmo.neerc.task.Task;
import ru.ifmo.neerc.task.TaskRegistry;
import ru.ifmo.neerc.task.TaskRegistryListener;
import ru.ifmo.neerc.utils.XmlUtils;

/**
 * @author Dmitriy Trofimov
 */
public class NEERCComponent implements Component {

    private String myName;
	private static final Logger Log = LoggerFactory.getLogger(NEERCComponent.class);
    private ComponentManager componentManager = null;
    private final MultiUserChatService mucService;

    private HashMap<String, QueryHandler> handlers = new HashMap<String, QueryHandler>();

    /**
     * Namespace of the packet extension.
     */
    public static final String NAMESPACE = "http://neerc.ifmo.ru/protocol/neerc";
    public static final String NAME = "neerc";
    public static final String DEFAULT_ROOM_NAME = "neerc";

    public NEERCComponent() {
        this.componentManager = ComponentManagerFactory.getComponentManager();
        myName = NAME + "." + componentManager.getServerName();
        XMPPServer server = XMPPServer.getInstance();
        mucService = server.getMultiUserChatManager().getMultiUserChatServices().get(0);
    }

    private void initUsers() {
		Log.debug("init start");

        for (MUCRoom room : mucService.getChatRooms()) {
            UserRegistry users = UserRegistry.getInstanceFor(room.getName());

            addUsers(users, room.getOwners(), true, null);
            addUsers(users, room.getAdmins(), true, null);
            addUsers(users, room.getMembers(), false, null);
        }
    }

    private void addUser(UserRegistry users, JID jid, boolean power, String groupName) {
        if (GroupJID.isGroup(jid)) {
            try {
                Group group = GroupManager.getInstance().getGroup(jid);
                addUsers(users, group.getAll(), power, group.getName());
            } catch (GroupNotFoundException e) {
                Log.error("Can't find group " + jid, e);
            }
        } else {
            String username = (jid.getNode() == null) ? jid.toString() : jid.getNode();
            UserEntry user = users.findOrRegister(username);
            user.setPower(power);
            user.setGroup(groupName);
        }
    }

    private void addUsers(UserRegistry users, Collection<JID> jids, boolean power, String groupName) {
        for (JID jid : jids) {
            addUser(users, jid, power, groupName);
        }
    }
    
    private void initHandlers() {
        handlers.put("users", new UsersQueryHandler());
        handlers.put("tasks", new TasksQueryHandler());
        handlers.put("task", new TaskQueryHandler());
        handlers.put("taskstatus", new TaskStatusQueryHandler());
        handlers.put("ping", new PingQueryHandler());
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

        for (MUCRoom room : mucService.getChatRooms()) {
            TaskRegistry tasks = TaskRegistry.getInstanceFor(room.getName());
            TaskRegistryListener taskListener = new MyTaskListener(room);
            tasks.addListener(taskListener);
        }

        ClockService clockservice = new ClockService();
        clockservice.addListener(new MyClockListener());
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
            processQuery(iq, reply);
        } else {
            // Answer an error since the server can't handle the requested
            // namespace
            reply.setError(PacketError.Condition.service_unavailable);
        }
        sendPacket(reply);
    }

    private void processQuery(IQ iq, IQ reply) {
        String roomName = iq.getTo().getNode();
        if (roomName == null) {
            roomName = DEFAULT_ROOM_NAME;
        }

        MUCRoom room = mucService.getChatRoom(roomName);
        if (room == null) {
            reply.setError(PacketError.Condition.service_unavailable);
            return;
        }

        MUCRole.Affiliation affiliation = room.getAffiliation(iq.getFrom());
        if (affiliation == MUCRole.Affiliation.none || affiliation == MUCRole.Affiliation.outcast) {
            reply.setError(PacketError.Condition.forbidden);
            return;
        }

        String namespace = iq.getChildElement().getNamespaceURI();
        String query = namespace.substring(NAMESPACE.length() + 1);
        if (!handlers.containsKey(query)) {
            Log.info("neerc got unknown query " + query);
            reply.setError(PacketError.Condition.service_unavailable);
        } else {
            String jid = iq.getFrom().toBareJID();
            UserRegistry users = UserRegistry.getInstanceFor(roomName);
            UserEntry sender = users.findOrRegister(jid);

            QueryHandler handler = handlers.get(query);
            handler.processQuery(this, iq, reply, sender, roomName);
        }
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
        for (MUCRoom room : mucService.getChatRooms()) {
            broadcastMessage(room, body, extension);
        }
    }

    public void broadcastMessage(MUCRoom room, String body, PacketExtension extension) {
        Message message = new Message();
        message.setFrom(myName);
        message.setBody(body);
        if (extension != null) {
            message.addExtension(new PacketExtension(extension.getElement().createCopy()));
        }

        for (MUCRole occupant : room.getOccupants()) {
            occupant.send(message);
        }
    }

    private class MyTaskListener implements TaskRegistryListener {

        private final MUCRoom room;

        public MyTaskListener(MUCRoom room) {
            this.room = room;
        }

        @Override
        public void taskChanged(Task task) {
            PacketExtension extension = new PacketExtension("x", XmlUtils.NAMESPACE_TASKS);
            XmlUtils.taskToXml(extension.getElement(), task);
            String body = "Task '" + task.getTitle() + "' (" + task.getId() + ") changed";
            broadcastMessage(room, body, extension);
        }

        @Override
        public void tasksReset() {
        }
    }

    private class MyClockListener implements ClockListener {

        @Override
        public void clockChanged(Clock clock) {
            PacketExtension extension = new PacketExtension("x", XmlUtils.NAMESPACE_CLOCK);
            XmlUtils.clockToXml(extension.getElement(), clock);
            String body = "The clock is ticking";
            broadcastMessage(body, extension);
        }
    }
}
