/*
 * Date: Oct 2, 2003
 *
 * $Id: JdbcConfig.java,v 1.2 2004/11/05 16:35:48 matvey Exp $
 */
package ru.ifmo.ips.config;

import ru.ifmo.ips.IpsRuntimeException;

import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * <code>JdbcConfig</code> class
 *
 * @author Matvey Kazakov
 */
public class JdbcConfig extends AbstractPropertiesConfig {

    public static final String ROOT_NAME= "ROOT";

    public JdbcConfig(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet 
                    = statement.executeQuery("select property_name, property_value from config");
            innerConfig = new InnerConfig(ROOT_NAME);
            elementRoot = ROOT_NAME;
            while (resultSet.next()) {
                String name = resultSet.getString("property_name");
                String value = resultSet.getString("property_value");
                setProperty(name, value);
            }
            resultSet.close();
            statement.close();
        } catch (Exception e) {
            throw new IpsRuntimeException("Error reading config", e);
        }
    }

    protected void readConfigFrom(Reader source) {
        throw new IpsRuntimeException("Not supported");
    }

    public void writeConfig(Writer target) throws ConfigException {
        throw new IpsRuntimeException("Not supported");
    }
    
    

}

