/*
 * Date: Mar 20, 2003
 *
 * $Id$
 */
package ru.ifmo.ips.config;

import java.util.*;
import java.io.Writer;
import java.io.Reader;

/**
 * <code>AbstractPropertiesConfig</code> class
 *
 * @author Matvey Kazakov
 */
public abstract class AbstractPropertiesConfig extends AbstractConfig {

    protected TreeMap properties = null;
    protected HashMap elementIds = null;
    protected String elementRoot = null;
    protected InnerConfig innerConfig;
    protected static final String ATTRIBUTE_ID = "id";
    private static final String IDS_DELIMETER = ",";
    protected static final String PATH_DELIMETER = ".";
    protected static final String NUM_DELIMETER = "#";
    protected static final String ATT_DELIMETER = "@";
    protected static final String ATTRIBUTE_IDS = ATT_DELIMETER + "ids";

    public AbstractPropertiesConfig() {
        elementIds = new HashMap();
        properties = new TreeMap(new SpecialComparator());
    }

    public Config getNode(String name) throws ConfigException {
        return innerConfig.getNode(name);
    }

    public String getProperty(String name) throws ConfigException {
        return innerConfig.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return innerConfig.getProperty(name, defaultValue);
    }

    public Config[] getNodeList(String name) throws ConfigException {
        return innerConfig.getNodeList(name);
    }

    public Map getProperties() {
        return properties;
    }

    /**
     * Adds property with specified name. Appends to existing property in case
     * given property already exists.
     * @param name property name
     * @param value property value
     */
    protected void addProperty(String name, String value) {
        String old = (String)properties.get(name);
        properties.put(name, (old == null) ? value : old + value);
    }

    /**
     * Adds property with specified path. Does the same as {@link #addProperty(java.lang.String, java.lang.String)}
     * @param path property path
     * @param value property value
     */
//    protected void addProperty(SpecialList path, String value) {
//        addProperty(path.toString(), value);
//    }

    /**
     * Adds property attribute into list.
     * @param path property path
     * @param attname attribute name
     * @param value attribute value
     */
    protected void addProperty(String path, String attname, String value) {
        addProperty(path + ATT_DELIMETER + attname, value);
    }

    public final void readConfig(Reader source) throws ConfigException {
        readConfigFrom(source);
        innerConfig = new InnerConfig(elementRoot);
    }

    protected abstract void readConfigFrom(Reader source);

    /**
     * Adds new XML node into property list.
     * @param path current path in the XML tree
     * @param id id value for listed tags
     */
    protected String addNode(String path, String id) {
        // getting row path to store list of properties with the same path
        String strPath = getRawPath(path);
        // initializing list of identifiers
        ArrayList ids = (ArrayList)elementIds.get(strPath);
        if (ids == null) {
            ids = new ArrayList();
            elementIds.put(strPath, ids);
        }
        // from the second element of the list we should assign
        // unique id, thus we are trying random number
        if (id == null && ids.size() >= 1) {
            id = newId();
        }
        ids.add(id);
        // replacing last node in the path - we should put '@<id>' to the
        // end of last node
        path += ((id == null ? "" : NUM_DELIMETER + id));
//        path.set(path.size() - 1, path.getLast().toString()
//                + (id == null ? "" : NUM_DELIMETER + id));
        // adding current property to the list
        addProperty(path, "");
        return path;
    }

    private String newId() {
        return "" + Math.round(Math.random() * 1000000000);
    }

    /**
     * Ends node. Adds id to the list of nodes (property with name ...@ids).
     * Trims spaces from the value of the property.
     * @param path property path
     */
    protected void finishNode(String path) {
        String strPath = getRawPath(path);
        String strPath1 = path;
        // trimming spaces
        properties.put(strPath1, properties.get(strPath1).toString().trim());
        ArrayList ids = (ArrayList)elementIds.get(strPath);
        if (ids != null) {
            // getting last id of the property
            String newId = (String)ids.get(ids.size() - 1);
            // addign to property with name @ids
            addProperty(strPath + ATTRIBUTE_IDS, (ids.size() > 1 ? IDS_DELIMETER : "")
                    + (newId == null ? "" : newId));
        }
    }

    /**
     * Returns raw path of the property. It means it does not include last #&lt;id&gt;.
     * @param path path to be converted to string
     * @return string representing raw path
     */
    private String getRawPath(String path) {
        String res = path;
        int pos1 = res.lastIndexOf(NUM_DELIMETER);
        int pos2 = res.lastIndexOf('.');
        // in case last node contains #<id> we should remove it.
        if (pos2 < pos1) {
            res = res.substring(0, pos1);
        }
        return res;
        
    }

    /**
     * Concatenates two pieces of path.
     *
     * @param s1 first piece of path
     * @param s2 second piece of path
     * @return concatenated path
     */
    private String makePath(String s1, String s2) {
        if (s2 == null) {
            return s1;
        } else if (s1 == null) {
            return s2;
        } else if ("".equals(s2)
                || s2.startsWith(NUM_DELIMETER)
                || s2.startsWith(ATT_DELIMETER)) {
            return s1 + s2;
        } else {
            return s1 + PATH_DELIMETER + s2;
        }
    }

    public String setProperty(String p_name, String p_value) throws ConfigException {
        return innerConfig.setProperty(p_name, p_value);
    }

    public void deleteProperty(String p_name) throws ConfigException {
        innerConfig.deleteProperty(p_name);
    }

    class SpecialComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                String s1 = (String)o1;
                String s2 = (String)o2;
                int len1 = s1.length();
                int len2 = s2.length();
                int i = 0;
                while (i < len1 && i < len2 && s1.charAt(i) == s2.charAt(i)) {
                    i++;
                }
                ;
                if (i == len1 && i == len2) {
                    return 0;
                } else if (i == len1) {
                    return -1;
                } else if (i == len2) {
                    return 1;
                } else {
                    char c1 = s1.charAt(i);
                    char c2 = s2.charAt(i);
                    if (c1 == '@') {
                        return -1;
                    } else if (c2 == '@') {
                        return 1;
                    } else if (c1 == '.') {
                        return -1;
                    } else if (c2 == '.') {
                        return 1;
                    } else if (c1 == '#') {
                        return -1;
                    } else if (c2 == '#') {
                        return 1;
                    }
                }
                return s1.compareTo(s2);
            } else {
                throw new ClassCastException();
            }
        }
    }

    /**
     * This class adds to standard LinkedList functionality of output to string.
     * This is used to convert list into string representing path.
     */
//    public static final class SpecialList extends LinkedList {
//
//        public String toString() {
//            Iterator it = iterator();
//            StringBuffer path = new StringBuffer();
//            while (it.hasNext()) {
//                path.append((path.length() <= 0 ? "" : ".")
//                        + it.next().toString());
//            }
//            return path.toString();
//        }
//    }

    /**
     * Class implements Config functionality for subtree
     */
    class InnerConfig extends AbstractConfig {

        /**
         * root node of the subtree
         */
        private String root;

        /**
         * Initializes instance of class.
         *
         * @param root root property
         */
        public InnerConfig(String root) {
            this.root = root;
        }

/* ===================== Interface methods implementation ================= */

        public Config getNode(String name) throws ConfigException {
            Config[] configs = getNodeList(name);
            if (configs.length != 1) {
                throw new ConfigException("Not a sigle element");
            }
            return configs[0];
        }

        public String getProperty(String name) throws ConfigException {
            String result, path = makePath(root, name);
            result = (String)properties.get(path);
            if (result == null) {
                throw new ConfigException("Unknown property: " + name);
            }
            return result;
        }

        public String getProperty(String name, String defaultValue) {
            String value = null;
            try {
                value = getProperty(name);
            } catch (ConfigException e) {
                value = defaultValue;
            }
            return value;
        }

        public Config[] getNodeList(String name) throws ConfigException {
            String path = makePath(root, name);
            // we cannot return list for attribute property
            if (path.indexOf(ATT_DELIMETER) != -1) {
                throw new ConfigException("Can't take a node from attribute");
            }
            // otherwise we try to retrieve ids of nodes
            ArrayList nodeList = (ArrayList)elementIds.get(path);
            Config[] result;
            // if they are exist
            if (nodeList != null) {
                result = new Config[nodeList.size()];
                // we are creating list of nodes
                for (int i = 0; i < result.length; i++) {
                    String id = (String)nodeList.get(i);
                    result[i] = new InnerConfig((id == null) ? path
                            : makePath(path, NUM_DELIMETER + (String)nodeList.get(i)));
                }
            } else if (path.lastIndexOf(PATH_DELIMETER) < path.lastIndexOf(NUM_DELIMETER)
                    && properties.get(path) != null) {
                result = new Config[1];
                result[0] = new InnerConfig(path);
            } else {
                // otherwise returning single node as the list
                throw new ConfigException("Unknown property: " + name);
            }
            return result;
        }

        public Map getProperties() {
            throw new UnsupportedOperationException("getProperties() method is not supported by " + this.getClass().getName());
        }

        public String setProperty(String p_name, String p_value) throws ConfigException {
            if (p_name == null || p_name.endsWith(ATTRIBUTE_IDS)) {
                throw new ConfigException("property with name " + p_name + " can't be set");
            }
            if (p_value == null) {
                p_value = "";
            }
            String result = (String)properties.get(root + PATH_DELIMETER + p_name);
            int attpos = p_name.lastIndexOf(ATT_DELIMETER);
            String attname = null;
            boolean renameid = false;
            if (attpos != -1) {
                attname = p_name.substring(attpos + 1);
                p_name = p_name.substring(0, attpos);
                int lastnodepos = p_name.lastIndexOf(PATH_DELIMETER);
                if (ATTRIBUTE_ID.equals(attname) && lastnodepos != -1) {
                    String lastnode = p_name.substring(lastnodepos + 1);
                    int idpos = lastnode.indexOf(NUM_DELIMETER);
                    if (idpos == -1) {
                        // we should create with specified tag, thus
                        // a@id=3 equals to a#3@id=3
                        p_name += NUM_DELIMETER + p_value;
                    }
                    // we should rename id tag...
                    // remember this.
                    renameid = true;
                }
            }
            p_name = root + PATH_DELIMETER + p_name;
            while (p_name.endsWith(PATH_DELIMETER)) {
                p_name = p_name.substring(0, p_name.length() - 1);
            }
            StringTokenizer tokenizer = new StringTokenizer(p_name, PATH_DELIMETER);
            String curNode = tokenizer.nextToken();
            String curName = null;
            String name = null;
            while (tokenizer.hasMoreTokens()) {
                curName = tokenizer.nextToken();
                if ((curNode + PATH_DELIMETER + curName).length() <= root.length()) {
                    curNode += PATH_DELIMETER + curName;
                    continue;
                }
                name = curName;
                String id = null;
                int idpos = curName.indexOf(NUM_DELIMETER);
                if (idpos != -1) {
                    id = curName.substring(idpos + 1);
                    name = curName.substring(0, idpos);
                }
                if (id == null) {
                    // key for element without id.
                    String key = getProperty((curNode + PATH_DELIMETER + curName).substring((root + PATH_DELIMETER).length()), null);
                    if (key == null) {
                        // we need to create it.
                        properties.put(curNode + PATH_DELIMETER + curName, "");
                        ArrayList ids = new ArrayList();
                        ids.add(null);
                        elementIds.put(curNode + PATH_DELIMETER + curName, ids);
                        properties.put(curNode + PATH_DELIMETER + curName + ATTRIBUTE_IDS, "");
                    }
                } else {
                    // key for element without id.
                    ArrayList ids = (ArrayList)elementIds.get(curNode + PATH_DELIMETER + name);
                    // key for element without id.
                    String idsStr = (String)properties.get(curNode + PATH_DELIMETER + name + ATTRIBUTE_IDS);
                    // key for element with id
                    String fullkey = (String)properties.get(curNode + PATH_DELIMETER + curName);
                    if (ids == null) {
                        // we need to create it.
                        ids = new ArrayList();
                        ids.add(null);
                        elementIds.put(curNode + PATH_DELIMETER + name, ids);
                        properties.put(curNode + PATH_DELIMETER + name + ATTRIBUTE_IDS, "");
                        idsStr = "";
                    }
                    if (fullkey == null) {
                        if (ids.contains(null)) {
                            ids.remove(null);
                        }
                        ids.add(id);
                        if (idsStr.length() != 0) {
                            idsStr += IDS_DELIMETER;
                        }
                        idsStr += id;
                        properties.put(curNode + PATH_DELIMETER + curName, "");
                        properties.put(curNode + PATH_DELIMETER + curName + ATT_DELIMETER + ATTRIBUTE_ID, id);
                        properties.put(curNode + PATH_DELIMETER + name + ATTRIBUTE_IDS, idsStr);
                    }
                }
                curNode += PATH_DELIMETER + curName;
            }

            if (renameid) {
                // we should rename id of the specified node...
                // rather serious operation.
                // old name was p_name, new one is p_name-<oldid>+p_value
                String oldname = p_name;
                String oldid = p_name.substring(p_name.lastIndexOf(NUM_DELIMETER) + 1);
                String newname = p_name.substring(0, p_name.lastIndexOf(NUM_DELIMETER) + 1) + p_value;
                if (properties.get(newname + ATTRIBUTE_IDS) != null) {
                    throw new ConfigException("Cannot rename node " + oldname + " to " + newname);
                }
                Iterator it = properties.tailMap(oldname).entrySet().iterator();
                HashMap tempMap = new HashMap();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String key = ((String)entry.getKey());
                    if (key.length() == oldname.length() && oldname.equals(key)
                            || key.startsWith(oldname + ATT_DELIMETER)
                            || key.startsWith(oldname + PATH_DELIMETER)) {
                        tempMap.put(entry.getKey(), entry.getValue());
                    } else {
                        break;
                    }
                }
                it = tempMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    String newKey = newname + ((String)entry.getKey()).substring(oldname.length());
                    properties.remove(entry.getKey());
                    properties.put(newKey, entry.getValue());
                }
                properties.put(newname + p_name.substring(oldname.length()) + ATT_DELIMETER + attname, p_value);
                String idsKey = p_name.substring(0, p_name.lastIndexOf(NUM_DELIMETER)) + ATTRIBUTE_IDS;
                String oldIds = (String)properties.get(idsKey);
                boolean removefirstchar = false;
                if (oldIds.startsWith(IDS_DELIMETER)) {
                    oldIds = "_" + oldIds;
                    removefirstchar = true;
                }
                String newIds = "";
                StringTokenizer idtokenizer = new StringTokenizer(oldIds, ",");
                while (idtokenizer.hasMoreTokens()) {
                    String to_add = idtokenizer.nextToken();
                    if (to_add.equals(oldid)) {
                        to_add = p_value;
                    }
                    if (newIds.length() != 0) {
                        newIds += IDS_DELIMETER;
                    }
                    newIds += to_add;
                }
                if (removefirstchar) {
                    newIds = newIds.substring(1);
                }
                // key for element without id.
                ArrayList ids = (ArrayList)elementIds.get(p_name.substring(0, p_name.lastIndexOf(NUM_DELIMETER)));
                ids.remove(oldid);
                ids.add(p_value);
                properties.put(idsKey, newIds);
            } else if (attname != null) {
                properties.put(p_name + ATT_DELIMETER + attname, p_value);
            } else {
                properties.put(p_name, p_value);
            }
            return result;
        }

        public void writeConfig(Writer p_target) throws ConfigException {
            throw new UnsupportedOperationException("writeConfig() method is not supported by " + this.getClass().getName());
        }

        public void readConfig(Reader source) throws ConfigException {
            throw new UnsupportedOperationException("readConfig() method is not supported by " + this.getClass().getName());
        }

        public void deleteProperty(String p_name) throws ConfigException {
            if (p_name == null || p_name.endsWith(ATTRIBUTE_IDS)) {
                throw new ConfigException("property with name " + p_name + " can't be deleted");
            }
            p_name = root + PATH_DELIMETER + p_name;
            Iterator it = properties.tailMap(p_name).entrySet().iterator();
            HashMap tempMap = new HashMap();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                String key = ((String)entry.getKey());
                if (key.length() == p_name.length() && p_name.equals(key)
                        || key.startsWith(p_name + ATT_DELIMETER)
                        || key.startsWith(p_name + PATH_DELIMETER)) {
                    if (key.lastIndexOf(AbstractPropertiesConfig.ATTRIBUTE_IDS)
                            == key.length() - AbstractPropertiesConfig.ATTRIBUTE_IDS.length()) {
                        String idsKey = key.substring(0, key.length() - AbstractPropertiesConfig.ATTRIBUTE_IDS.length());
                        AbstractPropertiesConfig.this.elementIds.remove(idsKey);
                    }
                    tempMap.put(key, entry.getValue());
                } else {
                    break;
                }
            }
            it = tempMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                properties.remove(entry.getKey());
            }
            if (p_name.lastIndexOf(ATT_DELIMETER) == -1) {
                int numpos = p_name.lastIndexOf(NUM_DELIMETER);
                int pathpos = p_name.lastIndexOf(PATH_DELIMETER);
                if (numpos > pathpos) {
                    String node = p_name.substring(0, numpos);
                    ArrayList ids = (ArrayList)elementIds.get(node);
                    String id = p_name.substring(numpos + 1);
                    ids.remove(id);
                    String oldIds = (String)properties.get(node + ATTRIBUTE_IDS) + IDS_DELIMETER;
                    int idpos = oldIds.indexOf(id + IDS_DELIMETER);
                    String newIds = oldIds.substring(0, idpos) + oldIds.substring(idpos + id.length() + 1);
                    properties.put(node + ATTRIBUTE_IDS, newIds.substring(0, newIds.length() - 1));
                }
            }
        }
    }

    //=================================================================

}

