#!/bin/sh

java -Dserver.host=hostname -Dusername=username -Dpassword=password -Dping=5 -Dconsole.encoding=UTF-8 -cp chat-client.jar ru.ifmo.neerc.chat.xmpp.XmppChatClient
