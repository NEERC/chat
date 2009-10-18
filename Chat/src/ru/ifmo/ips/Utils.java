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

