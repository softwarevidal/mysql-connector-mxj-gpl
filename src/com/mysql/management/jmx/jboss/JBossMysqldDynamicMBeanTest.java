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
package com.mysql.management.jmx.jboss;

import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;

import junit.framework.TestCase;

import com.mysql.management.MysqldResourceI;
import com.mysql.management.MysqldResourceTestImpl;
import com.mysql.management.jmx.SimpleMysqldDynamicMBean;
import com.mysql.management.jmx.TestFactory;
import com.mysql.management.util.Files;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: JBossMysqldDynamicMBeanTest.java,v 1.1 2005/02/16 21:46:11
 *          eherman Exp $
 */
public class JBossMysqldDynamicMBeanTest extends TestCase {
    private String orig;

    protected void setUp() throws Exception {
        super.setUp();
        orig = System.getProperty(Files.USE_TEST_DIR, "");
        System.setProperty(Files.USE_TEST_DIR, Boolean.TRUE.toString());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty(Files.USE_TEST_DIR, orig);
    }

    public void testNoAutoStart() throws Exception {
        MysqldResourceI mysqld = new MysqldResourceTestImpl(new HashMap());
        JBossMysqldDynamicMBean myBean = new JBossMysqldDynamicMBean(
                new TestFactory(mysqld));
        assertFalse(mysqld.isRunning());
        myBean.create();
        assertFalse(mysqld.isRunning());
    }

    public void testAutoStart() throws Exception {
        MysqldResourceI mysqld = new MysqldResourceTestImpl(new HashMap());
        JBossMysqldDynamicMBean myBean = new JBossMysqldDynamicMBean(
                new TestFactory(mysqld));
        assertFalse(mysqld.isRunning());

        String autostart = SimpleMysqldDynamicMBean.AUTOSTART_ATTR;
        String str_true = Boolean.TRUE.toString();
        myBean.setAttribute(new Attribute(autostart, str_true));

        assertFalse(mysqld.isRunning());
        myBean.create();
        assertTrue(mysqld.isRunning());
    }

    public void testCreateAndDestroy() throws Exception {
        Map atts = new HashMap();
        atts.put(SimpleMysqldDynamicMBean.AUTOSTART_ATTR, Boolean.TRUE
                .toString());
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null, atts,
                new HashMap());
        JBossMysqldDynamicMBean myBean = new JBossMysqldDynamicMBean(
                new TestFactory(mysqld));
        assertFalse(mysqld.isRunning());

        myBean.invoke(JBossMysqldDynamicMBean.CREATE_METHOD, null, null);
        assertTrue(mysqld.isRunning());

        myBean.invoke(JBossMysqldDynamicMBean.DESTROY_METHOD, null, null);
        assertFalse(mysqld.isRunning());
    }
}
