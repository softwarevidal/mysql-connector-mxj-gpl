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
package com.mysql.management.driverlaunched;

import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.StandardSocketFactory;
import com.mysql.management.MysqldFactory;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.MysqldResourceTestImpl;
import com.mysql.management.util.Files;

public class ServerLauncherSocketFactoryTest extends TestCase {
    static class FakeMysqldFactory implements MysqldFactory {
        Map fakeRunningMysqlds;

        public FakeMysqldFactory() {
            this.fakeRunningMysqlds = new HashMap();
        }

        public MysqldResourceI newMysqldResource(File base, File data,
                String version) {
            return new MysqldResourceTestImpl(base, data, fakeRunningMysqlds);
        }
    }

    static class FakeSocketFactory implements SocketFactory {
        int afterHandshakeCalled = 0;

        int beforeHandshakeCalled = 0;

        int connectCalled = 0;

        Object[] connectInfo;

        public Socket afterHandshake() {
            afterHandshakeCalled++;
            return null;
        }

        public Socket beforeHandshake() {
            beforeHandshakeCalled++;
            return null;
        }

        public Socket connect(String host, int portNumber, Properties props) {
            connectInfo = new Object[] { host, new Integer(portNumber), props };
            connectCalled++;
            return null;
        }
    }

    private String orig;

    protected void setUp() {
        orig = System.getProperty(Files.USE_TEST_DIR);
        System.setProperty(Files.USE_TEST_DIR, Boolean.TRUE.toString());
    }

    protected void tearDown() {
        if (orig != null) {
            System.setProperty(Files.USE_TEST_DIR, orig);
        } else {
            // System.clearProperty(Files.USE_TEST_DIR);
            System.setProperty(Files.USE_TEST_DIR, "");
        }
    }

    public void testReplaceNullStringWithNull() {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        assertEquals(null, sf.replaceNullStringWithNull("null"));
        assertEquals(null, sf.replaceNullStringWithNull(null));
        assertEquals("foo", sf.replaceNullStringWithNull("foo"));
    }

    public void testDefaultConstruction() {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        assertEquals(StandardSocketFactory.class, sf.getSocketFactory()
                .getClass());
        assertEquals(MysqldFactory.Default.class, sf.getResourceFactory()
                .getClass());
    }

    public void testComposition() throws Exception, Exception {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        FakeSocketFactory fake = new FakeSocketFactory();
        sf.setSocketFactory(fake);
        sf.throwOnBadPassword(false);
        sf.setResourceFactory(new FakeMysqldFactory());

        assertEquals(0, fake.connectCalled);
        assertEquals(0, fake.beforeHandshakeCalled);
        assertEquals(0, fake.afterHandshakeCalled);

        sf.connect(null, 0, new Properties());

        assertEquals(1, fake.connectCalled);
        assertEquals(0, fake.beforeHandshakeCalled);
        assertEquals(0, fake.afterHandshakeCalled);

        sf.beforeHandshake();

        assertEquals(1, fake.connectCalled);
        assertEquals(1, fake.beforeHandshakeCalled);
        assertEquals(0, fake.afterHandshakeCalled);

        sf.afterHandshake();

        assertEquals(1, fake.connectCalled);
        assertEquals(1, fake.beforeHandshakeCalled);
        assertEquals(1, fake.afterHandshakeCalled);
    }

    public void testMultipleConnectionsAndShutdownListener() throws Exception {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        FakeMysqldFactory factory = new FakeMysqldFactory();
        sf.setResourceFactory(factory);
        sf.setSocketFactory(new FakeSocketFactory());
        sf.throwOnBadPassword(false);

        assertEquals(0, factory.fakeRunningMysqlds.size());
        String host = "localhost";
        int port = 3306;

        File baseDir = new Files().tmp(MysqldResource.MYSQL_C_MXJ);

        Properties props = new Properties();
        props.setProperty("foo", "bar");
        props.setProperty(ServerLauncherSocketFactory.SERVER_DOT
                + MysqldResourceI.BASEDIR, baseDir.toString());
        props.setProperty(ServerLauncherSocketFactory.SERVER_DOT // 
                + "baz", "wiz");
        props.setProperty(ServerLauncherSocketFactory.SERVER_DOT // 
                + "nullMe", "null");

        assertEquals(0, factory.fakeRunningMysqlds.size());
        sf.connect(host, port, props);

        assertEquals(1, factory.fakeRunningMysqlds.size());

        MysqldResourceI mysqldResource = (MysqldResourceI) factory.fakeRunningMysqlds
                .get(baseDir);
        Map serverParams = mysqldResource.getServerOptions();
        assertEquals("wiz", serverParams.get("baz"));
        assertFalse("wiz", serverParams.containsKey("foo"));
        assertTrue("nullMe", serverParams.containsKey("nullMe"));
        assertEquals(null, serverParams.get("nullMe"));

        sf.connect(host, port, props);

        assertEquals(1, factory.fakeRunningMysqlds.size());

        props.setProperty(ServerLauncherSocketFactory.SERVER_DOT
                + MysqldResourceI.BASEDIR, baseDir.toString() + "2");
        sf.connect(host, port + 1, props);

        assertEquals(2, factory.fakeRunningMysqlds.size());

        mysqldResource.shutdown();

        assertEquals(1, factory.fakeRunningMysqlds.size());
    }

    public void testDefaultAnd3306() throws Exception {
        ServerLauncherSocketFactory sf = new ServerLauncherSocketFactory();
        FakeMysqldFactory factory = new FakeMysqldFactory();
        FakeSocketFactory fakeSf = new FakeSocketFactory();
        sf.setResourceFactory(factory);
        sf.setSocketFactory(fakeSf);
        sf.throwOnBadPassword(false);

        Properties props = new Properties();
        int port = 3306;

        assertEquals(0, factory.fakeRunningMysqlds.size());
        sf.connect("localhost", port, props);
        assertEquals(1, factory.fakeRunningMysqlds.size());
        sf.connect("127.0.0.1", port, props);
        assertEquals(1, factory.fakeRunningMysqlds.size());

        File baseDir = new Files().tmp("MysqlDir3307");
        props.setProperty(ServerLauncherSocketFactory.SERVER_DOT
                + MysqldResourceI.BASEDIR, baseDir.toString() + "1");
        sf.connect("localhost", port + 1, props);

        assertEquals(2, factory.fakeRunningMysqlds.size());
    }
}
