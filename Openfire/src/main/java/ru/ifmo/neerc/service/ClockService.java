package ru.ifmo.neerc.service;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jivesoftware.util.Log;
import org.xmpp.component.Component;
import ru.ifmo.neerc.clock.Clock;
import ru.ifmo.neerc.clock.ClockListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Dmitriy Trofimov
 */
public class ClockService extends Thread {
    private File clockFile;
    private long lastModified;
    private long timeStarted;
    private final Collection<ClockListener> listeners = new ArrayList<ClockListener>();
    private Clock clock = new Clock();
    
    public ClockService(File clockFile) {
        this.clockFile = clockFile;
    }

    public void run() {
        while (true) {
            try {
                checkUpdate();
                sleep(4000 + Math.round(1000 * Math.random()));
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                Log.error("Error updating clock", e);
            }

        }
    }
    
    private void checkUpdate() throws Exception {
        long modified = clockFile.lastModified();
        if (modified == 0) {
            if (lastModified != 0) {
                lastModified = 0;
                clock.setStatus(0);
                notifyListeners();
            }
            return;
        }
        if (modified <= lastModified) {
            return;
        }
        lastModified = modified;

        SAXReader xmlReader = new SAXReader();
        xmlReader.setEncoding("UTF-8");
        Document document = xmlReader.read(clockFile);
        Element root = document.getRootElement();
        long time = Long.parseLong(root.attributeValue("time"));
        long total = Long.parseLong(root.attributeValue("length"));
        int status = Integer.parseInt(root.attributeValue("status"));

        long newTimeStarted = System.currentTimeMillis() - time;
        if (clock.getStatus() != 2 || status != 2 || newTimeStarted < timeStarted || newTimeStarted > timeStarted + 60000) {
            timeStarted = newTimeStarted;
        } else {
            time = System.currentTimeMillis() - timeStarted;
        }
        
        clock.setTime(time);
        clock.setTotal(total);
        clock.setStatus(status);
        notifyListeners();
    }

    public void addListener(ClockListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        synchronized (listeners) {
            for (ClockListener listener : listeners) {
                listener.clockChanged(clock);
            }
        }
    }

}