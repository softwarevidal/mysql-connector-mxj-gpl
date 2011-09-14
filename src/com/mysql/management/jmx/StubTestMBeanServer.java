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

import java.io.ObjectInputStream;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.loading.ClassLoaderRepository;

import com.mysql.management.util.NotImplementedException;

public abstract class StubTestMBeanServer implements MBeanServer {

    public ObjectInstance createMBean(String arg0, ObjectName arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public ObjectInstance createMBean(String arg0, ObjectName arg1,
            ObjectName arg2) {
        throw new NotImplementedException(arg0, arg1, arg2);
    }

    public ObjectInstance createMBean(String arg0, ObjectName arg1,
            Object[] arg2, String[] arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public ObjectInstance createMBean(String arg0, ObjectName arg1,
            ObjectName arg2, Object[] arg3, String[] arg4) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3, arg4);
    }

    public ObjectInstance registerMBean(Object arg0, ObjectName arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public void unregisterMBean(ObjectName arg0) {
        throw new NotImplementedException(arg0);
    }

    public ObjectInstance getObjectInstance(ObjectName arg0) {
        throw new NotImplementedException(arg0);
    }

    public Set queryMBeans(ObjectName arg0, QueryExp arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public Set queryNames(ObjectName arg0, QueryExp arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public boolean isRegistered(ObjectName arg0) {
        throw new NotImplementedException(arg0);
    }

    public Integer getMBeanCount() {
        throw new NotImplementedException();
    }

    public Object getAttribute(ObjectName arg0, String arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public AttributeList getAttributes(ObjectName arg0, String[] arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public void setAttribute(ObjectName arg0, Attribute arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public AttributeList setAttributes(ObjectName arg0, AttributeList arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public Object invoke(ObjectName arg0, String arg1, Object[] arg2,
            String[] arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public String getDefaultDomain() {
        throw new NotImplementedException();
    }

    public String[] getDomains() {
        throw new NotImplementedException();
    }

    public void addNotificationListener(ObjectName arg0,
            NotificationListener arg1, NotificationFilter arg2, Object arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public void addNotificationListener(ObjectName arg0, ObjectName arg1,
            NotificationFilter arg2, Object arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public void removeNotificationListener(ObjectName arg0, ObjectName arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public void removeNotificationListener(ObjectName arg0, ObjectName arg1,
            NotificationFilter arg2, Object arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public void removeNotificationListener(ObjectName arg0,
            NotificationListener arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public void removeNotificationListener(ObjectName arg0,
            NotificationListener arg1, NotificationFilter arg2, Object arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public MBeanInfo getMBeanInfo(ObjectName arg0) {
        throw new NotImplementedException(arg0);
    }

    public boolean isInstanceOf(ObjectName arg0, String arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public Object instantiate(String arg0) {
        throw new NotImplementedException(arg0);
    }

    public Object instantiate(String arg0, ObjectName arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public Object instantiate(String arg0, Object[] arg1, String[] arg2) {
        throw new NotImplementedException(arg0, arg1, arg2);
    }

    public Object instantiate(String arg0, ObjectName arg1, Object[] arg2,
            String[] arg3) {
        throw new NotImplementedException(arg0, arg1, arg2, arg3);
    }

    public ObjectInputStream deserialize(ObjectName arg0, byte[] arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public ObjectInputStream deserialize(String arg0, byte[] arg1) {
        throw new NotImplementedException(arg0, arg1);
    }

    public ObjectInputStream deserialize(String arg0, ObjectName arg1,
            byte[] arg2) {
        throw new NotImplementedException(arg0, arg1, arg2);
    }

    public ClassLoader getClassLoaderFor(ObjectName arg0) {
        throw new NotImplementedException(arg0);
    }

    public ClassLoader getClassLoader(ObjectName arg0) {
        throw new NotImplementedException(arg0);
    }

    public ClassLoaderRepository getClassLoaderRepository() {
        throw new NotImplementedException();
    }

}
