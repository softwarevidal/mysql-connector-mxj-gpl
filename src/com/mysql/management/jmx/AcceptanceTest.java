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

import java.io.File;

import javax.management.Attribute;

import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.jmx.jboss.JBossMysqldDynamicMBean;
import com.mysql.management.util.Files;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.TestUtil;
import com.mysql.management.util.Threads;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.8 2005/07/05 21:19:40 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    private MysqldDynamicMBeanTestAgent agent;

    private SimpleMysqldDynamicMBean bean;

    private Threads threads = new Threads();

    private String orig;

    private File dataDir;

    private TestUtil testUtil;

    protected void setUp() {
        super.setUp();
        orig = System.getProperty(Files.USE_TEST_DIR, "");
        System.setProperty(Files.USE_TEST_DIR, Boolean.TRUE.toString());
        dataDir = new Files().tmp("MxjAccTest_" + System.currentTimeMillis());
        testUtil = new TestUtil();
    }

    protected void tearDown() {
        try {
            if (bean != null) {
                try {
                    bean.invoke(SimpleMysqldDynamicMBean.STOP_METHOD, null,
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                threads.pause(50);
            }
            if (agent != null) {
                agent.shutdown();
            }
            new Files().deleteTree(dataDir);
        } finally {
            System.setProperty(Files.USE_TEST_DIR, orig);
            super.tearDown();
        }
    }

    public void testConnectorMXJPropertiesTransformDefaultConstructor() {
        ConnectorMXJPropertiesTransform munger = null;
        agent = new MysqldDynamicMBeanTestAgent("mysql");
        munger = new ConnectorMXJPropertiesTransform();
        assertEquals(agent.get(), munger.getMBeanServer());
    }

    public void testEverything() throws Exception {
        agent = new MysqldDynamicMBeanTestAgent("mysql");

        String url = "jdbc:mysql:///test" + "?"
                + NonRegisteringDriver.PROPERTIES_TRANSFORM_KEY + "="
                + ConnectorMXJPropertiesTransform.class.getName();

        bean = new MysqldDynamicMBean();
        bean.getMysqldResource().setKillDelay(testUtil.testKillDelay());
        agent.addBean("mysql", "MySQL1", bean);

        assertEquals(false, bean.getMysqldResource().isRunning());
        String port = "" + testUtil.testPort();

        assertEquals("3306", bean.getMysqldResource().getServerOptions().get(
                MysqldResourceI.PORT));

        bean.setAttribute(new Attribute(MysqldResourceI.PORT, port));
        bean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        int i = 0;
        while (++i < 100) {
            if (bean.getMysqldResource().isRunning()) {
                break;
            }
            threads.pause(50);
        }
        assertTrue("still not started: " + i, bean.getMysqldResource()
                .isRunning());
        assertEquals(port, bean.getMysqldResource().getServerOptions().get(
                MysqldResourceI.PORT));

        new TestUtil().assertConnectViaJDBC(url);
    }

    public void testJBossDefaultConstructor() throws Exception {
        SimpleMysqldDynamicMBean jbossbean = new JBossMysqldDynamicMBean();
        MysqldResourceI mysqldResource = jbossbean.getMysqldResource();
        assertEquals(MysqldResource.class, mysqldResource.getClass());
    }

    public void testDifferentDataDir() throws Exception {
        agent = new MysqldDynamicMBeanTestAgent("mysql");

        String url = "jdbc:mysql:///test" + "?"
                + NonRegisteringDriver.PROPERTIES_TRANSFORM_KEY + "="
                + ConnectorMXJPropertiesTransform.class.getName();

        bean = new MysqldDynamicMBean();
        bean.getMysqldResource().setKillDelay(testUtil.testKillDelay());
        agent.addBean("mysql", "MySQL1", bean);

        assertEquals(false, bean.getMysqldResource().isRunning());
        String port = "" + testUtil.testPort();

        assertEquals("3306", bean.getMysqldResource().getServerOptions().get(
                MysqldResourceI.PORT));

        bean.setAttribute(new Attribute(MysqldResourceI.PORT, port));
        bean.setAttribute(new Attribute(MysqldResourceI.DATADIR, dataDir
                .getPath()));
        bean.invoke(SimpleMysqldDynamicMBean.START_METHOD, null, null);
        int i = 0;
        while (++i < 100) {
            if (bean.getMysqldResource().isRunning()) {
                break;
            }
            threads.pause(50);
        }
        assertTrue("still not started: " + i, bean.getMysqldResource()
                .isRunning());

        assertEquals(port, "" + bean.getMysqldResource().getPort());
        assertEquals(port, bean.getMysqldResource().getServerOptions().get(
                MysqldResourceI.PORT));

        assertEquals(dataDir, bean.getMysqldResource().getDataDir());

        new TestUtil().assertConnectViaJDBC(url);
    }

}
