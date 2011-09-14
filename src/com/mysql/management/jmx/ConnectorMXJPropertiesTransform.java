/*
 Copyright (C) 2004-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. All rights reserved.
 Use is subject to license terms.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License version 2 as 
 published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.management.jmx;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.mysql.jdbc.ConnectionPropertiesTransform;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.jmx.jboss.JBossMysqldDynamicMBean;
import com.mysql.management.util.Exceptions;

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: ConnectorMXJPropertiesTransform.java,v 1.1 2005/02/16 21:46:11
 *          eherman Exp $
 */
public final class ConnectorMXJPropertiesTransform implements
        ConnectionPropertiesTransform {

    private static Class[] mbeanClasses = new Class[] {
            MysqldDynamicMBean.class, SimpleMysqldDynamicMBean.class,
            JBossMysqldDynamicMBean.class };

    private MBeanServer mbeanServer;

    public ConnectorMXJPropertiesTransform(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    public ConnectorMXJPropertiesTransform() {
        this((MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0));
    }

    MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    /**
     * replaces the host and port and parameters with values for the MBean
     */
    public Properties transformProperties(Properties props) throws SQLException {
        String host = getHost();
        String port = getPort();
        if (!port.equals("3306")) {
            host = host + ":" + port;
        }
        props.put(NonRegisteringDriver.HOST_PROPERTY_KEY, host);
        props.put(NonRegisteringDriver.PORT_PROPERTY_KEY, port);
        return props;
    }

    /**
     * @return "localhost"
     */
    String getHost() {
        return "localhost";
    }

    /**
     * @return the port of the MBean managed MySQL server
     * @throws SQLException
     */
    String getPort() throws SQLException {
        Exceptions.SQLBlock block = new Exceptions.SQLBlock(System.err) {
            public Object inner() throws Exception {
                return getPortInner();
            }
        };
        return (String) block.exec();
    }

    private Object getPortInner() throws InstanceNotFoundException,
            MBeanException, AttributeNotFoundException, ReflectionException {

        ObjectName objName = getMysqldObjectName();
        String port = ((String) getMBeanServer().getAttribute(objName,
                MysqldResourceI.PORT));
        return port;
    }

    /**
     * @return the MysqldDynamicMBean ObjectName
     * @throws InstanceNotFoundException
     */
    ObjectName getMysqldObjectName() throws InstanceNotFoundException {
        Set objectNames = getMBeanServer().queryNames(null, null);
        StringBuffer error = errorMsgHeader();
        for (Iterator iter = objectNames.iterator(); iter.hasNext();) {
            ObjectName objectName = (ObjectName) iter.next();
            ObjectInstance objInst = getMBeanServer().getObjectInstance(
                    objectName);
            String className = objInst.getClassName();
            if (classNameMatch(className)) {
                return objectName;
            }
            appendItem(error, objectName, className);
        }

        throw new IllegalStateException(error.toString());
    }

    /**
     * @param error
     * @param objectName
     * @param className
     */
    private void appendItem(StringBuffer error, ObjectName objectName,
            String className) {
        error.append("[");
        error.append(className);
        error.append("(");
        error.append(objectName.getCanonicalName());
        error.append(")]");
    }

    private StringBuffer errorMsgHeader() {
        StringBuffer error = new StringBuffer();

        error.append("MySQL MBean (");

        for (int i = 0; i < mbeanClasses.length; i++) {
            error.append(mbeanClasses[i].getName());
            if (i < (mbeanClasses.length - 1)) {
                error.append(", ");
            }
        }

        error.append(") Not Found in: ");
        return error;
    }

    boolean classNameMatch(String className) {
        for (int i = 0; i < mbeanClasses.length; i++) {
            if (mbeanClasses[i].getName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
