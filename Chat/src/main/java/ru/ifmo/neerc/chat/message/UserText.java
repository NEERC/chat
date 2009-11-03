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
package ru.ifmo.neerc.chat.message;

import ru.ifmo.ips.Base64;

import java.io.UnsupportedEncodingException;

/**
 * @author Matvey Kazakov
 */
@Deprecated
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
