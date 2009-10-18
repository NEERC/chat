// $Id$
/**
 * Date: 27.10.2004
 */
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.Base64;

import java.io.UnsupportedEncodingException;

/**
 * @author Matvey Kazakov
 */
public class UserText {
    
    private String text;

    public UserText(String text) {
        this.text = text;
    }

    public UserText() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String asString() {
        try {
            return Base64.encode(text.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
    
    public void fromString(String str) {
        try {
            text = new String(Base64.decode(new StringBuffer(str)), "UTF8");
        } catch (UnsupportedEncodingException e) {
        }
    }

}

