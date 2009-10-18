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
// $Id: TimerService.java,v 1.2 2007/10/28 07:32:13 matvey Exp $
/**
 * Date: 28.10.2005
 */
package ru.ifmo.neerc.chat.server;

import ru.ifmo.ips.config.Config;
import ru.ifmo.ips.config.XMLConfig;
import ru.ifmo.ips.config.ConfigListener;
import ru.ifmo.ips.config.ConfigFactory;
import ru.ifmo.neerc.chat.message.TimerMessage;
import ru.ifmo.neerc.chat.ChatLogger;

import java.io.File;

/**
 * @author Matvey Kazakov
 */
class TimerService extends Thread implements ConfigListener{
    private static final String PROPERTY_TOTAL = "@length";
    private static final String PROPERTY_TIME = "@time";
    private static final String PROPERTY_STATUS = "@status";
    private static final String TAG_NEERC_CLOCK = "neerc-clock";

    private File file;
    private long lastModified;
    private long time = 0;
    private long lastMillis = 0;
    private int status;
    private long total;

    public TimerService() {
        super("NEERC timer service");
        ConfigFactory.addListener(this);
        setDaemon(true);
        readConfig();
    }

    public void configChanged(String configName) {
        readConfig();
    }

    private void readConfig() {
        String clockFileName;
        try {
            clockFileName = ConfigFactory.getMainConfig().getProperty(TAG_NEERC_CLOCK);
        } catch(Exception e) {
            clockFileName = "clock.xml";
        }
        if (file== null || !file.getName().equals(clockFileName) ) {
            ChatLogger.logInfo("New clock file: " + clockFileName);
        }
        file = new File(clockFileName);
        lastModified = 0;
    }

    public void run() {
        while (true) {
            try {
                try {                    
                    long modified = file.lastModified();
                    if (modified > lastModified) {
                        lastModified = modified;
                        Config clock = new XMLConfig(file.getCanonicalPath());
                        time = Long.parseLong(clock.getProperty(PROPERTY_TIME));
                        lastMillis = System.currentTimeMillis();
                        status = Integer.parseInt(clock.getProperty(PROPERTY_STATUS));
                        total = Long.parseLong(clock.getProperty(PROPERTY_TOTAL));
                        MessageMulticaster.getInstance().sendMessage(new TimerMessage(total, time, status));
                    } else if (status == 2) {
                        long newLastMillis = System.currentTimeMillis();
                        time += newLastMillis - lastMillis;
                        lastMillis = newLastMillis;
                        if (time <= total) {
//                            MessageMulticaster.getInstance().sendMessage(new TimerMessage(total, time, status));
                        }
                    }
                } catch (Exception e) {
                }
                sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

