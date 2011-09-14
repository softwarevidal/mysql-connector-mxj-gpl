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

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;

import junit.framework.TestCase;

import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.jmx.jboss.JBossMysqldDynamicMBean;
import com.mysql.management.util.TestUtil;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: ConnectorMXJPropertiesTransformTest.java,v 1.1 2005/02/16
 *          21:46:10 eherman Exp $
 */
public class ConnectorMXJPropertiesTransformTest extends TestCase {

    private static final String PORT = "3307";

    private ConnectorMXJPropertiesTransform munger;

    private TestUtil testUtil;

    protected void setUp() throws Exception {
        testUtil = new TestUtil();
        String[] objectNames = new String[] { "mysql:name=MySQL1" };
        Class mbeanClass = MysqldDynamicMBean.class;
        TestMBeanServer mbs = new TestMBeanServer(objectNames, mbeanClass);
        munger = new ConnectorMXJPropertiesTransform(mbs);
    }

    public void testGetMysqldDynamicMBean() throws Exception {
        ObjectName objName = munger.getMysqldObjectName();
        String name = objName.getCanonicalName();
        testUtil.assertContainsIgnoreCase(name, "mysql");
    }

    public void testGetHostAndPort() throws Exception {
        assertEquals("localhost", munger.getHost());
        assertEquals(PORT, munger.getPort());
    }

    public void testTransformProperties() throws Exception {
        Properties props = new Properties();
        props.put(NonRegisteringDriver.HOST_PROPERTY_KEY, "foo");
        props.put(NonRegisteringDriver.PORT_PROPERTY_KEY, PORT);

        munger.transformProperties(props);

        assertEquals("localhost:" + PORT, props
                .get(NonRegisteringDriver.HOST_PROPERTY_KEY));
        assertEquals(PORT, props.get(NonRegisteringDriver.PORT_PROPERTY_KEY));
    }

    public void testGetMysqldObjectName() throws Exception {
        String[] objectNames = new String[] { "foo:name=foo1", "foo:name=bar1",
                "baz:name=baz1" };
        TestMBeanServer mbs = new TestMBeanServer(objectNames, String.class);
        munger = new ConnectorMXJPropertiesTransform(mbs);
        Exception expected = null;
        ObjectName objectName = null;
        try {
            objectName = munger.getMysqldObjectName();
        } catch (IllegalStateException e) {
            expected = e;
        }
        assertNull(objectName);
        assertNotNull(expected);
        String errMsg = expected.getMessage();
        testUtil.assertContainsIgnoreCase(errMsg, "foo");
        testUtil.assertContainsIgnoreCase(errMsg, "bar");
        testUtil.assertContainsIgnoreCase(errMsg, "baz");
    }

    public void testNameMatch() throws Exception {
        assertFalse(munger.classNameMatch("foo"));
        assertTrue(munger.classNameMatch(MysqldDynamicMBean.class.getName()));
        assertTrue(munger.classNameMatch(SimpleMysqldDynamicMBean.class
                .getName()));
        assertTrue(munger.classNameMatch(JBossMysqldDynamicMBean.class
                .getName()));
    }

    static class TestMBeanServer extends StubTestMBeanServer {
        private Set objectNames;

        private String className;

        Object[] methodparams;

        public TestMBeanServer(String[] names, Class aClass) throws Exception {
            this.objectNames = new LinkedHashSet();

            for (int i = 0; i < names.length; i++) {
                objectNames.add(new ObjectName(names[i]));
            }

            this.className = aClass.getName();
        }

        public Set queryNames(ObjectName arg0, QueryExp arg1) {
            methodparams = new Object[] { arg0, arg1 };
            return objectNames;
        }

        public ObjectInstance getObjectInstance(ObjectName arg0) {
            return new ObjectInstance(arg0, className);
        }

        public Object getAttribute(ObjectName arg0, String arg1) {
            if (MysqldResourceI.PORT.equals(arg1))
                return PORT;

            return arg1 + "? ... arg0: " + arg0;
        }
    }
}
