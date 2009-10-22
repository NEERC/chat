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
import org.xmpp.component.Component;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

import ru.ifmo.neerc.chat.user.UserEntry;
import ru.ifmo.neerc.chat.user.UserRegistry;

import java.sql.SQLException;

/**
 * @author Dmitriy Trofimov
 */
public class NEERCComponent implements Component {

    ComponentManager componentManager = null;
    UserRegistry users = UserRegistry.getInstance();

    /**
     * Namespace of the packet extension.
     */
    public static final String NAMESPACE = "http://neerc.ifmo.ru/protocol/neerc";
    public static final String NAMESPACE_USERS = NAMESPACE + "#users";
    public static final String NAME = "neerc";

    public NEERCComponent() {
        this.componentManager = ComponentManagerFactory.getComponentManager();
    }
    
    private void initUsers() {
        // TODO: get from MUC or own database
        users.findOrRegister("admin").setPower(true);
        users.findOrRegister("matvey").setPower(true);
        users.findOrRegister("admin").setGroup("Admins");
        users.findOrRegister("matvey").setGroup("Admins");
        users.findOrRegister("hall1").setGroup("Halls");
        users.findOrRegister("hall2").setGroup("Halls");
        users.findOrRegister("hall3").setGroup("Halls");
        users.findOrRegister("hall4").setGroup("Halls");
        users.findOrRegister("hall5").setGroup("Halls");
        users.findOrRegister("hall6").setGroup("Halls");
        users.findOrRegister("hall7").setGroup("Halls");
    }
    

    public void initialize(JID jid, ComponentManager componentManager) {
        initUsers();
    }

    public void start() {
    }

    public void shutdown() {
    }

    // Component Interface

    public void processPacket(Packet packet) {
        componentManager.getLog().debug("neerc got packet: " + packet.toXML());
        if (packet instanceof IQ) {
            // Handle disco packets
            IQ iq = (IQ)packet;
            // Ignore IQs of type ERROR or RESULT
            if (IQ.Type.error == iq.getType() || IQ.Type.result == iq.getType()) {
                return;
            }
            processIQ(iq);
        }
    }

    private void processIQ(IQ iq) {
        IQ reply = IQ.createResultIQ(iq);
        // TODO: identify sender and get his UserEntry

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
                childElement.addElement("feature").addAttribute("var", NAMESPACE_USERS);

            }
        } else if (NAMESPACE_USERS.equals(namespace)) {
            for (UserEntry user: users.getUsers()) {
                Element userElement = childElement.addElement("user");
                userElement.addAttribute("name", user.getName());
                userElement.addAttribute("group", user.getGroup());
                userElement.addAttribute("power", user.isPower() ? "yes" : "no");
            }
        // TODO: add other actions
        } else {
            // Answer an error since the server can't handle the requested
            // namespace
            reply.setError(PacketError.Condition.service_unavailable);
        }
        
        try {
            componentManager.sendPacket(this, reply);
        }
        catch (Exception e) {
            componentManager.getLog().error(e);
        }
        componentManager.getLog().debug("neerc sent packet: " + reply.toXML());
    }


    public String getDescription() {
        return "NEERC service";
    }

    public String getName() {
        return "NEERC";
    }
}
