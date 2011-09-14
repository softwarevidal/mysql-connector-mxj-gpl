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

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;

import com.mysql.management.MysqldFactory;
import com.mysql.management.util.Exceptions;

/**
 * MySQL DynamicMBean
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldDynamicMBean.java,v 1.7 2005/10/25 19:11:16 eherman Exp $
 */
public final class MysqldDynamicMBean extends SimpleMysqldDynamicMBean {

    public MysqldDynamicMBean() {
        super();
    }

    MysqldDynamicMBean(MysqldFactory mysqldFactory) {
        super(mysqldFactory);
    }

    public synchronized void setAttribute(Attribute attribute)
            throws AttributeNotFoundException {
        super.setAttribute(attribute);
        if (attribute.getName().equals(AUTOSTART_ATTR)) {
            Object val = attribute.getValue().toString().toLowerCase();
            if (val.equals(Boolean.TRUE.toString())) {
                invokeStart();
            }
        }
    }

    private void invokeStart() {
        Exceptions.VoidBlock startMethod = new Exceptions.VoidBlock() {
            public void inner() throws Exception {
                invoke(START_METHOD, null, null);
            }
        };
        startMethod.exec();
    }
}
