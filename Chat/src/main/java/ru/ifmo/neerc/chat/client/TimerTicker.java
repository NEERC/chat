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
/*
 * Date: Nov 28, 2006
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.client;

import javax.swing.*;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

/**
 * <code>TimerTicker</code> class
 *
 * @author Matvey Kazakov
 */
public class TimerTicker extends Thread {
    private JLabel neercTimer;
    
    private long time, total;
    private long savedTimeStart, savedTime;
    private int status;


    public TimerTicker(JLabel neercTimer) {
        super("Timer Ticker");
        this.neercTimer = neercTimer;
        setDaemon(true);
    }

    public void updateStatus(long total, long time, int status) {
        this.savedTimeStart = this.time = time;
        savedTime = System.currentTimeMillis();
        this.total = total;
        this.status = status;
        updateLabel();
        if (!isAlive()) {
            start();
        }
    }


    public void run() {
        while(true) {
            // special case for BEFORE and PAUSE
            if (status != 1 && status != 3) {
                time = Math.min(savedTimeStart + System.currentTimeMillis() - savedTime, total);
            }
            updateLabel();
            try {
                sleep(250);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void updateLabel() {
        final String status = this.status > 0 ? (" Time: " + convertToHMS(time) + " of "
                + convertToHMS(total) + " Status: " + convertStatus(TimerTicker.this.status)) : "";
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                neercTimer.setText(status);
            }
        });
        
    }
    
    private String convertStatus(int status) {
        switch (status) {
            case 1:
                return "BEFORE";
            case 2:
                return "RUNNING";
            case 3:
                return "PAUSE";
            case 4:
                return "OVER";
            default:
                return "UNKNOWN";
        }
    }

    private String convertToHMS(long left) {
        left /= 1000;
        int h = (int)(left / 60 / 60);
        int m = (int)((left - h * 60 * 60) / 60);
        int s = (int)(left - h * 60 * 60 - m * 60);
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        new PrintWriter(charArrayWriter).printf("%d:%02d:%02d", h, m, s);
        return charArrayWriter.toString();
    }

}
