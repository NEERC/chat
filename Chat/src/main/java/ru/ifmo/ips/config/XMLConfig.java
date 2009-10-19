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
/**
 * Date: Sep 13, 2002
 */
package ru.ifmo.ips.config;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import java.util.*;
import java.io.*;

import ru.ifmo.ips.config.AbstractPropertiesConfig;
import ru.ifmo.ips.config.ConfigException;

/**
 * Implements config reading from XML file using SAX parser.
 * <p>
 *   This class uses hashmap to store the following:
 *   <ol>
 *     <li>
 *       properties in the form:<br>
 *       key: &lt;node&gt;[#&lt;id&gt;](.&lt;node&gt;[#&lt;id&gt;])*[@&lt;attr&gt;] <br>
 *       value: value of the property
 *     </li><li>
 *       for each node special property in the form:<br>
 *       key: ...@ids<br>
 *       value: comma separated values of attribute &quot;id&quot;
 *     </li>
 *   </ol>
 * </p>
 *
 * @author Matvey Kazakov
 */
public class XMLConfig extends AbstractPropertiesConfig {
    public static final String INDENTSTEP = "  ";

    /* ==================== Constructors ====================================== */
    
    /**
     * Constructs empty Config from XML file.
     * @throws ConfigException in case loading failed
     * @throws FileNotFoundException in case specified file not found
     */
    public static XMLConfig createEmptyConfig(String elementRoot) {
        String fakeXml = "<" + elementRoot + "/>";
        return new XMLConfig(new StringReader(fakeXml));
    }
    
    /**
     * Constructs Config from XML file.
     * @param fname name of the file
     * @throws ConfigException in case loading failed
     * @throws FileNotFoundException in case specified file not found
     */
    public XMLConfig(String fname) throws ConfigException, FileNotFoundException {
        this(new BufferedReader(new FileReader(fname)));
    }

    /**
     * Constructs Config from stream. Stream should contain XML file.
     * @param source stream to read configuration from
     * @throws ConfigException in case loading failed
     */
    public XMLConfig(Reader source) throws ConfigException {
        readConfig(source);
    }

/* ===================== Interface methods implementation ================= */

/* ===================== Special methods ================================== */

    public void readConfigFrom(Reader source) throws ConfigException {
        DefaultHandler handler = new ConfigHandler();
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(source), handler);
        } catch (Exception e) {
            throw new ConfigException("Error during SAX parsing: " + e.getMessage());
        }
        innerConfig = new InnerConfig(elementRoot);
    }

    public void writeConfig(Writer p_target) throws ConfigException {
        try {
            p_target.write("<?xml version=\"1.0\"?>\n");
            Iterator it = properties.entrySet().iterator();
            Map.Entry entry = (Map.Entry)it.next();
            String curKey = (String)entry.getKey();
            String curValue = (String)entry.getValue();
            String curNode = curKey;
            String curNodeValue = curValue;
            String indent = "";
            p_target.write(indent + "<" + curNode);
            indent += INDENTSTEP;
            while (true) {
                do {
                    if (it.hasNext()) {
                        entry = (Map.Entry)it.next();
                        curKey = (String)entry.getKey();
                        curValue = (String)entry.getValue();
                    } else {
                        curKey = "";
                        curValue = "";
                    }
                } while (curKey.endsWith(ATTRIBUTE_IDS));
                while (!(curNode.length() == 0
                        || curKey.startsWith(curNode + NUM_DELIMETER)
                        || curKey.startsWith(curNode + ATT_DELIMETER)
                        || curKey.startsWith(curNode + PATH_DELIMETER))) {
                    String addon = curNode.substring(curNode.lastIndexOf('.') + 1);
                    curNode = (curNode.lastIndexOf('.') == -1) ? "" : curNode.substring(0, curNode.lastIndexOf('.'));
                    String name = addon;
                    if (name.lastIndexOf(NUM_DELIMETER) != -1) {
                        name = name.substring(0, name.lastIndexOf(NUM_DELIMETER));
                    }
                    p_target.write(">\n");
                    if (curNodeValue.length() > 0) {
                        p_target.write(indent + curNodeValue + "\n");
                    }
                    indent = indent.substring(0, indent.length() - INDENTSTEP.length());
                    p_target.write(indent + "</" + name);
                    curNodeValue = "";
                }
                if (curNode.length() != 0) {
                    String addon = curKey.substring(curNode.length());
                    if (addon.startsWith("@")) {
                        p_target.write(" " + addon.substring(1) + "=\"" + curValue + "\"");
                    } else if (addon.startsWith(".")) {
                        p_target.write(">\n" + indent + curNodeValue);
                        curNode = curKey;
                        curNodeValue = curValue;
                        String name = addon.substring(1);
                        if (name.lastIndexOf("#") != -1) {
                            name = name.substring(0, name.lastIndexOf("#"));
                        }
                        p_target.write("<" + name);
                        indent += INDENTSTEP;
                    } else if (addon.startsWith("#")) {
                        String name = curNode.substring(curNode.lastIndexOf('.') + 1);
                        curNode = (curNode.lastIndexOf('.') == -1) ? "" : curNode.substring(0, curNode.lastIndexOf('.'));
                        p_target.write(">" + curNodeValue + "</" + name + "><" + name);
                        curNodeValue = curValue;
                        curNode = curKey;
                    } else {

                    }
                } else {
                    p_target.write(">");
                    break;
                }
            }
        } catch (IOException e) {
            throw new ConfigException("Some error occured during writing ");
        }
    }
    
    public void writeCompactConfig(Writer p_target) throws ConfigException {
        try {
            p_target.write("<?xml version=\"1.0\"?>");
            Iterator it = properties.entrySet().iterator();
            Map.Entry entry = (Map.Entry)it.next();
            String curKey = (String)entry.getKey();
            String curValue = (String)entry.getValue();
            String curNode = curKey;
            String curNodeValue = curValue;
            p_target.write("<" + curNode);
            while (true) {
                do {
                    if (it.hasNext()) {
                        entry = (Map.Entry)it.next();
                        curKey = (String)entry.getKey();
                        curValue = (String)entry.getValue();
                    } else {
                        curKey = "";
                        curValue = "";
                    }
                } while (curKey.endsWith(ATTRIBUTE_IDS));
                while (!(curNode.length() == 0
                        || curKey.startsWith(curNode + NUM_DELIMETER)
                        || curKey.startsWith(curNode + ATT_DELIMETER)
                        || curKey.startsWith(curNode + PATH_DELIMETER))) {
                    String addon = curNode.substring(curNode.lastIndexOf('.') + 1);
                    curNode = (curNode.lastIndexOf('.') == -1) ? "" : curNode.substring(0, curNode.lastIndexOf('.'));
                    String name = addon;
                    if (name.lastIndexOf(NUM_DELIMETER) != -1) {
                        name = name.substring(0, name.lastIndexOf(NUM_DELIMETER));
                    }
                    p_target.write(">");
                    if (curNodeValue.length() > 0) {
                        p_target.write(curNodeValue);
                    }
                    p_target.write("</" + name);
                    curNodeValue = "";
                }
                if (curNode.length() != 0) {
                    String addon = curKey.substring(curNode.length());
                    if (addon.startsWith("@")) {
                        p_target.write(" " + addon.substring(1) + "=\"" + curValue + "\"");
                    } else if (addon.startsWith(".")) {
                        p_target.write(">" + curNodeValue);
                        curNode = curKey;
                        curNodeValue = curValue;
                        String name = addon.substring(1);
                        if (name.lastIndexOf("#") != -1) {
                            name = name.substring(0, name.lastIndexOf("#"));
                        }
                        p_target.write("<" + name);
                    } else if (addon.startsWith("#")) {
                        String name = curNode.substring(curNode.lastIndexOf('.') + 1);
                        curNode = (curNode.lastIndexOf('.') == -1) ? "" : curNode.substring(0, curNode.lastIndexOf('.'));
                        p_target.write(">" + curNodeValue + "</" + name + "><" + name);
                        curNodeValue = curValue;
                        curNode = curKey;
                    } else {

                    }
                } else {
                    p_target.write(">");
                    break;
                }
            }
        } catch (IOException e) {
            throw new ConfigException("Some error occured during writing ");
        }
    }
    

/* ===================== Special class ==================================== */

/* ===================== Special class ==================================== */

/* ===================== Special class ==================================== */

    /**
     *  Implements SAX parsing for XML Configuration files.
     */
    class ConfigHandler extends DefaultHandler {

        /**
         * stores current property path
         */
        private String path = null;

        /**
         * initializes instance of class
         */
        public ConfigHandler() {
            path = "";
        }

        /**
         * Processes start of element
         */
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes)
                throws SAXException {
            // in case root not found yet we are storing it.
            if (elementRoot == null) {
                elementRoot = qName;
            }
            // adding current node to path
            path += (path.length() == 0 ?  "" : PATH_DELIMETER) + qName;
            // adding new node
            path = addNode(path, attributes.getValue(ATTRIBUTE_ID));
            // adding attributes
            for (int i = 0; i < attributes.getLength(); i++) {
                String s = attributes.getQName(i);
                addProperty(path, s, attributes.getValue(i));
            }
        }

        /**
         * Processes end of element
         */
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            finishNode(path);
            int pos = path.lastIndexOf(PATH_DELIMETER);
            if (pos == -1) {
                pos = 0;
            }
            path = path.substring(0, pos);
        }

        /**
         * processes text nodes.
         */
        public void characters(char ch[], int start, int length)
                throws SAXException {
            addProperty(path, new String(ch, start, length));
        }
    }

/* ===================== Special class ==================================== */

}
