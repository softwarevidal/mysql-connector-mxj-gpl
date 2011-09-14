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

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import junit.framework.TestCase;

import com.mysql.management.MysqldResourceI;
import com.mysql.management.MysqldResourceTestImpl;
import com.mysql.management.util.Str;
import com.mysql.management.util.TestUtil;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: SimpleMysqldDynamicMBeanTest.java,v 1.1 2005/02/16 21:46:10
 *          eherman Exp $
 */
public class SimpleMysqldDynamicMBeanTest extends TestCase {

    public void testGetMBeanInfo() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());

        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));

        MBeanInfo info = myBean.getMBeanInfo();
        assertNotNull(info);
        assertEquals(myBean.getClass().getName(), info.getClassName());
        assertEquals("MySQL MBean", info.getDescription());

        MBeanAttributeInfo[] atts = info.getAttributes();
        assertEquals(3, atts.length);
        assertEquals(SimpleMysqldDynamicMBean.AUTOSTART_ATTR, atts[0].getName());
        assertEquals("", atts[0].getDescription());
        assertEquals(String.class.getName(), atts[0].getType());
        assertTrue(atts[0].isReadable());
        assertTrue(atts[0].isWritable());
        assertEquals(false, atts[0].isIs());

        assertEquals("foo", atts[1].getName());
        assertEquals("", atts[1].getDescription());
        assertEquals(String.class.getName(), atts[1].getType());
        assertTrue(atts[1].isReadable());
        assertTrue(atts[1].isWritable());
        assertEquals(false, atts[1].isIs());

        MBeanConstructorInfo[] cons = info.getConstructors();
        assertEquals(1, cons.length);
        assertEquals(0, cons[0].getSignature().length);

        MBeanNotificationInfo[] notes = info.getNotifications();
        assertEquals(0, notes.length);

        MBeanOperationInfo[] ops = info.getOperations();
        assertEquals(1, ops.length);
        assertEquals(SimpleMysqldDynamicMBean.START_METHOD, ops[0].getName());
        assertEquals("Start MySQL", ops[0].getDescription());
        assertEquals(0, ops[0].getSignature().length);
        assertEquals("void", ops[0].getReturnType());
        assertEquals(MBeanOperationInfo.ACTION, ops[0].getImpact());

        myBean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        info = myBean.getMBeanInfo();

        ops = info.getOperations();
        assertEquals(1, ops.length);
        assertEquals(SimpleMysqldDynamicMBean.STOP_METHOD, ops[0].getName());
        assertEquals("Stop MySQL", ops[0].getDescription());
        assertEquals(0, ops[0].getSignature().length);
        assertEquals("void", ops[0].getReturnType());
        assertEquals(MBeanOperationInfo.ACTION, ops[0].getImpact());

        myBean.invoke(SimpleMysqldDynamicMBean.STOP_METHOD, null, null);
        info = myBean.getMBeanInfo();

        ops = info.getOperations();
        assertEquals(1, ops.length);
        assertEquals(SimpleMysqldDynamicMBean.START_METHOD, ops[0].getName());
    }

    public void testGetAttribute() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());

        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        assertEquals("bar", myBean.getAttribute("foo"));
        try {
            myBean.getAttribute("b0gus");
            fail();
        } catch (AttributeNotFoundException e) {
            assertTrue(e.getMessage().indexOf("b0gus") >= 0);
        }
    }

    public void testGetAttributeList() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        options.put("baz", "wiz");
        options.put("datadir", "/bogus/dir");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());

        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        AttributeList attributeList = myBean.getAttributes(new String[] {
                "foo", "datadir" });
        assertEquals(2, attributeList.size());
        Attribute att = (Attribute) attributeList.get(0);
        assertEquals("foo", att.getName());
        assertEquals("bar", att.getValue());
    }

    public void testGetAgentVersion() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());

        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        String verAttName = myBean.versionAttributeName();
        String expected = "$Id: " + new Str().shortClassName(myBean);
        String beanVersion = (String) myBean.getAttribute(verAttName);
        new TestUtil().assertContainsIgnoreCase(beanVersion, expected);

        MBeanInfo info = myBean.getMBeanInfo();
        MBeanAttributeInfo[] atts = info.getAttributes();

        assertTrue(atts[0].isReadable());
        assertTrue(atts[0].isWritable());
        assertFalse(atts[0].isIs());

        assertTrue(atts[1].isReadable());
        assertTrue(atts[1].isWritable());
        assertFalse(atts[1].isIs());

        assertTrue(atts[2].isReadable());
        assertFalse(atts[2].isWritable());
        assertFalse(atts[2].isIs());
    }

    public void testSetAttributes() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        options.put("baz", "wiz");
        options.put("datadir", "/bogus/dir");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());

        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));

        AttributeList list = new AttributeList();
        list.add(new Attribute("foo", "changed"));
        list.add(new Attribute("datadir", "/changed/dir"));
        myBean.setAttributes(list);

        assertEquals("changed", myBean.getAttribute("foo"));
        assertEquals("wiz", myBean.getAttribute("baz"));
        assertEquals("/changed/dir", myBean.getAttribute("datadir"));
    }

    public void testSetAttribute() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());
        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        try {
            myBean.setAttribute(new Attribute("bogus", "werid"));
            fail();
        } catch (AttributeNotFoundException e) {
            assertTrue(e.getMessage(), e.getMessage().indexOf("bogus") >= 0);
        }
    }

    public void testAutoStart() throws Exception {
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                new HashMap());
        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        assertFalse(mysqld.isRunning());
        String autostart = SimpleMysqldDynamicMBean.AUTOSTART_ATTR;
        String str_true = Boolean.TRUE.toString();
        myBean.setAttribute(new Attribute(autostart, str_true));
        assertFalse(mysqld.isRunning());
        assertFalse(mysqld.getServerOptions().containsKey(autostart));
    }

    public void testInvoke() throws Exception {
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                new HashMap());
        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        assertEquals(false, mysqld.isRunning());
        myBean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        assertTrue(mysqld.isRunning());
        myBean.invoke(SimpleMysqldDynamicMBean.STOP_METHOD, null, null);
        assertEquals(false, mysqld.isRunning());

        try {
            myBean.invoke("bogus", null, null);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().indexOf("bogus") >= 0);
        }
    }

    public void testPassOptionsMapToMysqld() throws Exception {
        Map options = new HashMap();
        options.put("foo", "bar");
        options.put("baz", "wiz");
        options.put("datadir", "/bogus/dir");
        options.put("help", "");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());
        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));
        myBean.setAttribute(new Attribute("foo", "changed"));
        assertTrue(myBean.attributesToOpionMap().containsKey("foo"));
        assertEquals(1, myBean.attributesToOpionMap().size());
        myBean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        assertEquals("changed", mysqld.getServerOptions().get("foo"));
    }

    public void testFreezeAttributes() {
        Map options = new HashMap();
        options.put("foo", "bar");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());
        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));

        MBeanInfo info = myBean.getMBeanInfo();
        MBeanAttributeInfo[] atts = info.getAttributes();
        assertTrue(atts[0].isWritable());

        myBean.freezeAttributes();
        info = myBean.getMBeanInfo();
        atts = info.getAttributes();
        assertFalse(atts[0].isWritable());
    }

    public void testAttributesFrozenWhenRunning() throws ReflectionException {
        Map options = new HashMap();
        options.put("foo", "bar");
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                options, new HashMap());
        SimpleMysqldDynamicMBean myBean = new SimpleMysqldDynamicMBean(
                new TestFactory(mysqld));

        MBeanInfo info = myBean.getMBeanInfo();
        MBeanAttributeInfo[] atts = info.getAttributes();
        assertTrue(atts[0].isWritable());

        myBean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        info = myBean.getMBeanInfo();
        atts = info.getAttributes();
        assertFalse(atts[0].isWritable());

        myBean.invoke(SimpleMysqldDynamicMBean.STOP_METHOD, null, null);
        info = myBean.getMBeanInfo();
        atts = info.getAttributes();
        assertTrue(atts[0].isWritable());
    }
}
