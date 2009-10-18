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
 * Date: Nov 27, 2007
 *
 * $Id$
 */
package ru.ifmo.neerc.chat.plugin.crisscross;

import ru.ifmo.neerc.chat.plugin.crisscross.CrissCrossData;
import ru.ifmo.ips.config.Config;

/**
 * <code>CrissCrossTurnData</code> class
 *
 * @author Matvey Kazakov
 */
public class CrissCrossTurnData extends CrissCrossData {

    private static final String ATT_X = "@x";
    private static final String ATT_Y = "@y";
    
    public int x;
    public int y;
    
    public static CrissCrossData createTurnData(int from, int x, int y) {
           CrissCrossTurnData data = new CrissCrossTurnData();
           data.type = TURN;
           data.from = from;
           data.x = x;
           data.y = y;
           return data;
       }


    public void serialize(Config cfg) {
        super.serialize(cfg);
        cfg.setProperty(ATT_X, String.valueOf(x));
        cfg.setProperty(ATT_Y, String.valueOf(y));
    }

    public String getDataType() {
        return CrissCrossPlugin.DATATYPE_TURN;
    }

    public void deserialize(Config cfg) {
        super.deserialize(cfg);
        x = cfg.getInt(ATT_X);
        y = cfg.getInt(ATT_Y);
    }

    public String asString() {
        return super.asString() + String.format("(%d,%d)", x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

