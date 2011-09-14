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

import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import com.mysql.management.MysqldFactory;
import com.mysql.management.jmx.SimpleMysqldDynamicMBean;
import com.mysql.management.util.Exceptions;

public final class JBossMysqldDynamicMBean extends SimpleMysqldDynamicMBean {
    static final String CREATE_METHOD = "create";

    static final String DESTROY_METHOD = "destroy";

    private MBeanOperationInfo createMethod;

    private MBeanOperationInfo destroyMethod;

    public JBossMysqldDynamicMBean() {
        super();
        initOps();
    }

    JBossMysqldDynamicMBean(MysqldFactory mysqldfactory) {
        super(mysqldfactory);
        initOps();
    }

    private void initOps() {
        createMethod = newVoidMBeanOperation(CREATE_METHOD,
                "Create MySQL MBean");
        destroyMethod = newVoidMBeanOperation(DESTROY_METHOD,
                "Destroy MySQL MBean");
        getMBeanOperationInfoList().add(createMethod);
        getMBeanOperationInfoList().add(destroyMethod);
    }

    public synchronized Object invoke(String methodName, Object args[],
            String types[]) throws ReflectionException {

        clearMBeanInfo();

        if (methodName.equals(CREATE_METHOD)) {
            create();
            return null;
        }

        if (methodName.equals(DESTROY_METHOD)) {
            destroy();
            return null;
        }

        return super.invoke(methodName, args, types);
    }

    public void create() {
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() throws Exception {
                String autoStart = "" + getAttribute(AUTOSTART_ATTR);
                autoStart = autoStart.toLowerCase();
                if (autoStart.equals(Boolean.TRUE.toString())) {
                    invoke(START_METHOD, null, null);
                }
            }
        };
        block.exec();
    }

    public void destroy() {
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() throws ReflectionException {
                invoke(STOP_METHOD, null, null);
            }
        };
        block.exec();
    }
}
