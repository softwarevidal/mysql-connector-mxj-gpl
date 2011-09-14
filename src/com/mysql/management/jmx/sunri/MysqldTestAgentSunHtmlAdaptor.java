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
package com.mysql.management.jmx.sunri;

import javax.management.JMException;

import com.mysql.management.jmx.MysqldDynamicMBean;
import com.mysql.management.jmx.MysqldDynamicMBeanTestAgent;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * This Simple JMX Agent is useful for testing.
 * 
 * This JMX Agent requires the Sun specific tools which come with Sun's JMX
 * Reference Implementation in the "jmxtools.jar" file.
 * 
 * If Sun's "jmxtools.jar" is not available (or more specifically
 * <code>com.sun.jdmk.comm.HtmlAdaptorServer</code> then deploying to a JMX
 * Agent is required. (I use JBoss --Eric)
 * 
 * This Agent has exactly two beans: 1) a MysqldDynamicMBean 2) a Broswer
 * Interface for management
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldTestAgentSunHtmlAdaptor.java,v 1.1 2005/02/16 21:46:11
 *          eherman Exp $
 */
public class MysqldTestAgentSunHtmlAdaptor {

    private final MysqldDynamicMBeanTestAgent agent;

    /** creates the MBean server and adds the Mysqld & Browser Beans */
    public MysqldTestAgentSunHtmlAdaptor(int port, String name)
            throws JMException {
        this.agent = new MysqldDynamicMBeanTestAgent("MysqldAgent");
        agent.addBean("mysql", name, new MysqldDynamicMBean());
        addBrowser(port);
    }

    /** Creates and adds a Browser Interface for management */
    public final void addBrowser(int port) throws JMException {
        String adapterName = "htmladapter,port=" + port;
        HtmlAdaptorServer adapter = new HtmlAdaptorServer();
        adapter.setPort(port);
        adapter.start();
        agent.addBean("mysql", adapterName, adapter);
    }

    // ---------------
    /**
     * starts an MBean server with: 1) a MySQL bean 2) a Browser interface on
     * port 90902
     */
    public static void main(String args[]) throws Exception {
        new MysqldTestAgentSunHtmlAdaptor(9092, "mysqld");
    }
}
