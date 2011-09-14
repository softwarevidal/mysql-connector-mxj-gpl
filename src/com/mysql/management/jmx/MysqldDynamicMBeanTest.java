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

import javax.management.Attribute;

import junit.framework.TestCase;

import com.mysql.management.MysqldResourceI;
import com.mysql.management.MysqldResourceTestImpl;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldDynamicMBeanTest.java,v 1.1 2005/02/16 21:46:10 eherman
 *          Exp $
 */
public class MysqldDynamicMBeanTest extends TestCase {
    public void testAutoStart() throws Exception {
        MysqldResourceI mysqld = new MysqldResourceTestImpl(null, null,
                new HashMap());
        MysqldDynamicMBean myBean = new MysqldDynamicMBean(new TestFactory(
                mysqld));
        assertFalse(mysqld.isRunning());
        String autostart = SimpleMysqldDynamicMBean.AUTOSTART_ATTR;
        String str_true = Boolean.TRUE.toString();
        myBean.setAttribute(new Attribute(autostart, str_true));
        assertTrue(mysqld.isRunning());
        assertFalse(mysqld.getServerOptions().containsKey(autostart));
    }
}
