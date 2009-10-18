/**
 * Date: Sep 13, 2002
 */

package ru.ifmo.ips.config;

import java.util.Map;
import java.io.Writer;
import java.io.Reader;

/**
 * <code>Config</code> defines generic interface for using configuration files in Java applications.
 * <br>
 * Implementations of this interface could allow various formats of configuration files: ".property" files,
 * XML files, etc. For details about properties naming please check description of particular implementation.
 * But here is some basic rules:
 * <ul>
 *   <li>Different levels of the properties should be separated by comma:
 *     <p>
 *     <code>parent.child.subchild</code>
 *     <p>
 *   <li>Repeatable properties should be distinguished by unique attribute using '#' symbol:
 *     <p>
 *     <code>parent.child.subchild#1</code>
 *     <br>
 *     <code>parent.child.subchild#2</code>
 *     <p>
 *     In case of XML configuration format, user may set these unique identifiers using 'id' attribute for element.
 *     Otherwise they will be generated automatically using random() function.
 *   <li>In case of XML configuration format, the root tag should be skipped from property name
 *     <p>
 *     In this example:
 *     <pre>
 *     &lt;main&gt;
 *       &lt;parent&gt;
 *         &lt;child&gt;an value&lt;/child&gt;
 *       &lt;/parent&gt;
 *     &lt;/main&gt;
 *     </pre>
 *     property should be named:
 *     <br>
 *     <code>parent.child</code>
 *     <p>
 *   <li>In case of XML configuration format, the Attribute should be marked with a leading '@' symbol (XPath style)
 *     <p>
 *     <code>parent.child.subchild@name</code>
 *     <p>
 * </ul>
 */
public interface Config {
    /**
     * Returns value for given property or throws {@link ConfigException ConfigException} if there's no such property
     * in configuration.
     *
     * @param name property name
     * @return property value
     * @throws ConfigException if case there's no such property
     */
    String getProperty(String name) throws ConfigException;
    /**
     * Returns value for given property or default value. Please note that this method returns default value instead
     * of throwing exception if there's no such property, unlike {@link #getProperty(String) getProperty(String)}.
     *
     * @param name property name
     * @param defaultValue will be returned if there's no such property
     * @return property value or default value
     */
    String getProperty(String name, String defaultValue);

    /**
     * Returns <code>Config</code> object rooted at the given property
     *
     * @param name property name
     * @return <code>Config</code> object
     * @throws ConfigException if case there's no such property
     */
    Config getNode(String name) throws ConfigException;

    /**
     * Returns array of <code>Config</code> objects with each one rooted at given repeatable property
     *
     * @param name property name
     * @return array of <code>Config</code> objects
     * @throws ConfigException if case there's no such property
     */
    Config[] getNodeList(String name) throws ConfigException;

    /**
     * Returns map of all properties available. This method is mostly used for testing and debugging purposes.
     *
     * @return a <code>Map</code> with all available properties
     * @throws java.lang.UnsupportedOperationException in case if operation is not supported by particular
     * Config implementation
     */
    Map getProperties();

    /**
     * Assigns new value for given property. If property with given name doesn't exist, it will be created.
     * Therefore, property name should be full-qualified property path from the configuration root.
     * <br>
     * Method could throw {@link ConfigException ConfigException} if property with given name couldn't be created by
     * some reason - for example if property's path is not correct.
     * <br><br>
     * Special cases:<ul>
     * <li>
     * <code>setProperty("a.b#c@att", "attvalue")</code> creates repeatable tag a.b with id equal to c in case it
     * is not created yet and sets attribute att
     * </li>
     * <li>
     * <code>setProperty("a.b", "")</code> creates repeatable tag a.b with id equal to c in case it
     * is not created yet.
     * </li>
     * </ul>
     *
     *
     * @return old property value or null if there wasn't value previously
     * @param name property name
     * @param value new property value, if null or empty string this property is just created with empty value.
     * @throws ConfigException if property with given name is not exists and couldn't be created.
     * @throws java.lang.UnsupportedOperationException in case if operation is not supported by particular
     * Config implementation
     */
    String setProperty(String name, String value) throws ConfigException;

    /**
     * Deletes given property. If property with given name doesn't exist, exception is thrown.
     * Therefore, property name should be full-qualified property path from the configuration root.
     * <br>
     * Method could throw {@link ConfigException ConfigException} if property with given name couldn't be deleted.
     * <br>
     *
     *
     * @param name property name
     * @throws ConfigException if property with given name is not exists and/or couldn't be deleted.
     * @throws java.lang.UnsupportedOperationException in case if operation is not supported by particular
     * Config implementation
     */
    void deleteProperty(String name) throws ConfigException;

    /**
     * Writes current config to the target writer. Various implementation could write config in specific formats like
     * properties file, XML files, etc.
     *
     * @param target the target writer
     * @throws ConfigException if any error occure trying to write config to given writer
     */
    void writeConfig(Writer target) throws ConfigException;

    /**
     * Reads config from given reader. Various implementation could read config
     * in specific formats like properties file, XML files, etc.
     *
     * @param source the source reader
     * @throws ConfigException if any error occure trying to read config from given reader
     */
    void readConfig(Reader source) throws ConfigException;

    int getInt(String name, int defaultValue);

    int getInt(String name);

    String getString(String name);

    String getString(String name, String defaultValue);

    Config createNode(String name);
    
}

