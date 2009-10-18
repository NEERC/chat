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
// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.ips;

/**
 * @author Matvey Kazakov
 */
public class Utils {
    /**
     * Replaces special symbols ('&lt;', '&gt;', '&amp;', '&apos;', '&quot;') with appropriate sequences.
     * @param s string to be converted
     * @return converted string
     */
    public static String convertToXML(String s) {
        StringBuffer dst = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '<') {
                dst.append("&lt;");
            } else if (ch == '>') {
                dst.append("&gt;");
            } else if (ch == '&') {
                dst.append("&amp;");
            } else if (ch == '\'') {
                dst.append("&apos;");
            } else if (ch == '\"') {
                dst.append("&quot;");
            } else {
                dst.append(ch);
            }
        }
        return dst.toString();
    }
}

