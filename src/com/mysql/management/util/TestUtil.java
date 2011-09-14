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
package com.mysql.management.util;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

public class TestUtil {
    private static int testKillDelayDefault = 30 * 1000; // Thirty Seconds

    private static int testMysqldPortDefault = 3336;

    private int port;

    private int testKillDelay;

    public TestUtil() {
        this(new Str().parseInt(System.getProperty("c-mxj_test_port"),
                testMysqldPortDefault, System.err), //
                new Str().parseInt(System.getProperty("c-mxj_test_kill-delay"),
                        testKillDelayDefault, System.err));
    }

    public TestUtil(int port, int testKillDelay) {
        this.port = port;
        this.testKillDelay = testKillDelay;
    }

    public int testPort() {
        return port;
    }

    public int testKillDelay() {
        return testKillDelay;
    }

    public void assertContainsIgnoreCase(String searchIn, String searchFor) {
        if (new Str().containsIgnoreCase(searchIn, searchFor)) {
            return;
        }
        String msg = "<" + searchFor + "> not found in <" + searchIn + ">";
        throw new AssertionFailedError(msg);
    }

    public void assertConnectViaJDBC(String url, boolean dbInUrl)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, SQLException {

        assertConnectViaJDBC(url, "root", "", dbInUrl);
    }

    public void assertConnectViaJDBC(String url) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, SQLException {

        assertConnectViaJDBC(url, false);
    }

    public void assertConnectViaJDBC(String url, String user, String password)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, SQLException {

        assertConnectViaJDBC(url, user, password, false);
    }

    public void assertConnectViaJDBC(String url, String user, String password,
            boolean dbInUrl) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, SQLException {

        String name = com.mysql.jdbc.Driver.class.getName();
        Class c = Class.forName(name);
        c.newInstance();

        Connection conn = DriverManager.getConnection(url, user, password);
        try {
            if (!dbInUrl) {
                useDbTest(conn);
            }
            checkVersion(conn);
            checkBigInt(conn);
            variousStuff(conn);
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void assertNotImplemented(Object stub, Method method) {
        try {
            invoke(stub, method);
        } catch (NotImplementedException e) {
            return;
        } catch (Exception e) {
            if (isNotImplementedMsg(e.getMessage())) {
                return;
            }
            Throwable cause = e.getCause();
            if (cause instanceof NotImplementedException) {
                return;
            }
            if (cause != null) {
                if (isNotImplementedMsg(cause.getMessage())) {
                    return;
                }
            }
            new Exceptions().toRuntime(e);
        }
        throw new RuntimeException("This is now implemented.");
    }

    public void assertObjStubsInterface(Object stub, Class anInterface) {
        Method[] methods = anInterface.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            assertNotImplemented(stub, methods[i]);
        }
    }

    private boolean isNotImplementedMsg(String msg) {
        if (msg == null) {
            return false;
        }
        String serachFor = "Not implemented".toLowerCase();
        return msg.toLowerCase().indexOf(serachFor) >= 0;
    }

    private void invoke(final Object target, final Method method)
            throws Exception {
        Class[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = newParamObject(paramTypes[i]);
        }
        method.invoke(target, params);
    }

    private Object newParamObject(Class paramType) {
        if (paramType.equals(int.class) || paramType.equals(Integer.class)) {
            return new Integer(0);
        }

        if (paramType.equals(boolean.class) || paramType.equals(Boolean.class)) {
            return Boolean.FALSE;
        }

        if (paramType.equals(Object[].class)
                || paramType.equals(String[].class)) {
            return new String[0];
        }

        if (paramType.equals(Runnable.class) || paramType.equals(Thread.class)) {
            return new Thread();
        }

        return null;
    }

    /** basic check to see if the database is there, selects the version */
    private void checkVersion(Connection conn) throws SQLException {
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT VERSION()");
            Assert.assertTrue(rs.next());
            String version = rs.getString(1);
            Assert.assertTrue(version, version.startsWith("5."));
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void useDbTest(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("use test");
        } finally {
            stmt.close();
        }
    }

    /** creates table, inserts, selects, drops table */
    private void checkBigInt(Connection conn) {
        QueryUtil util = new QueryUtil(conn);
        String tableName = "bigIntRegression";
        String col1 = "bigIntCol";
        long testVal = 6692730313872877584L;
        try {
            util.executeUpdate("DROP TABLE IF EXISTS " + tableName);
            util.executeUpdate("CREATE TABLE " + tableName + " (" + col1
                    + " BIGINT NOT NULL)");
            util.executeUpdate("INSERT INTO " + tableName + " VALUES ("
                    + testVal + ")");
            List rows = util.executeQuery("SELECT " + col1 + " FROM "
                    + tableName);
            for (int i = 0; i < rows.size(); i++) {
                Map row = (Map) rows.get(i);
                Assert.assertTrue(row.size() > 0);
                Map.Entry column1 = (Map.Entry) row.entrySet().iterator()
                        .next();
                Number n = (Number) column1.getValue();
                Assert.assertEquals(testVal, n.longValue());
            }

            Assert.assertEquals(1, rows.size());
        } finally {
            util.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        }
    }

    /** creates table, inserts, selects, batch updates, drops table */
    private void variousStuff(Connection conn) {
        QueryUtil util = new QueryUtil(conn);
        String tableName = "foo_table";
        String col1 = "foo_id";
        String col2 = "bar";
        try {
            util.executeUpdate("DROP TABLE IF EXISTS " + tableName);
            util.executeUpdate("CREATE TABLE " + tableName + " (" + //
                    col1 + " INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY," + //
                    col2 + " TEXT" //
                    + ")");

            String sql = "INSERT INTO " + tableName + " VALUES (NULL,'alpha')";
            List rows = util.executeUpdateReturningKeys(sql);
            Assert.assertEquals(1, rows.size());
            Map key1Map = (Map) rows.get(0);
            Assert.assertEquals(1, key1Map.size());
            Map.Entry entry = ((Map.Entry) key1Map.entrySet().iterator().next());
            // Object columnName = entry.getKey();
            Object generatedKey = entry.getValue();
            // Assert.assertNotNull("GENERATED_KEY", columnName);
            Assert.assertEquals(new Long(1), generatedKey);

            sql = "INSERT INTO " + tableName + " VALUES (NULL,?)";
            Object[][] batchParams = { new Object[] { "foo" },
                    new Object[] { "bar" }, new Object[] { "baz" }, };

            int[] ints = util.executeBatch(sql, batchParams);
            Assert.assertEquals(3, ints.length);
            for (int i = 0; i < ints.length; i++) {
                Assert.assertEquals(1, ints[i]);
            }

        } finally {
            util.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        }
    }

    public String getSystemPropertyWithDefaultFromResource(String property,
            String resourceName, PrintStream err) {
        Properties props = new Streams().loadProperties(resourceName, err);
        String fileVal = props.getProperty(property);
        return System.getProperty(property, fileVal);
    }
}
