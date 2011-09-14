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
package com.mysql.management;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mysql.management.util.Files;
import com.mysql.management.util.QueryUtil;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.Str;
import com.mysql.management.util.TestUtil;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.30 2005/08/30 18:20:23 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    private Connection conn = null;

    private File tmpDir;

    private MysqldResourceI mysqld;

    private Files fileUtil;

    private TestUtil testUtil;

    protected void setUp() {
        super.setUp();
        fileUtil = new Files();
        tmpDir = new Files().testDir();
        testUtil = new TestUtil();
    }

    protected void tearDown() {
        super.tearDown();

        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn = null;
        }

        if (mysqld != null) {
            try {
                mysqld.setKillDelay(testUtil.testKillDelay());
                mysqld.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void testMain() throws Exception {
        int port1 = testUtil.testPort();
        File baseDir1 = new File(tmpDir, "cmxj-dir.1");

        String url1 = "jdbc:mysql://127.0.0.1:" + port1 + "/test";
        String[] startArgs1 = new String[] {
                "--port=" + port1,
                "--" + MysqldResourceI.BASEDIR + "=" + baseDir1,
                "--" + MysqldResourceI.KILL_DELAY + "="
                        + testUtil.testKillDelay() };
        String[] stopArgs1 = new String[] {
                "--" + MysqldResourceI.BASEDIR + "=" + baseDir1,
                "--shutdown",
                "--" + MysqldResourceI.KILL_DELAY + "="
                        + testUtil.testKillDelay() };

        MysqldResource.main(startArgs1);

        int port2 = port1 + 1000;
        String url2 = "jdbc:mysql://127.0.0.1:" + port2 + "/test";
        File baseDir2 = new File(tmpDir, "cmxj-dir.2");
        String[] startArgs2 = new String[] {
                "--" + MysqldResourceI.PORT + "=" + port2,
                "--" + MysqldResourceI.BASEDIR + "=" + baseDir2,
                "--" + MysqldResourceI.KILL_DELAY + "="
                        + testUtil.testKillDelay() };
        String[] stopArgs2 = new String[] {
                "--" + MysqldResourceI.BASEDIR + "=" + baseDir2,
                "--shutdown",
                "--" + MysqldResourceI.KILL_DELAY + "="
                        + testUtil.testKillDelay() };

        MysqldResource.main(startArgs2);

        testUtil.assertConnectViaJDBC(url1);
        testUtil.assertConnectViaJDBC(url2);

        MysqldResource.main(stopArgs1);

        testUtil.assertConnectViaJDBC(url2);

        SQLException expected = null;
        try {
            testUtil.assertConnectViaJDBC(url1);
        } catch (SQLException e) {
            expected = e;
        }
        assertNotNull(expected);

        MysqldResource.main(stopArgs2);
        expected = null;
        try {
            testUtil.assertConnectViaJDBC(url2);
        } catch (SQLException e) {
            expected = e;
        }
        assertNotNull(expected);
    }

    public void testCreateUser() throws Exception {
        // resetOutAndErr();
        File baseDir3 = new File(tmpDir, "mxj-user-test");
        fileUtil.deleteTree(baseDir3);
        mysqld = new MysqldResource(baseDir3);
        baseDir3.mkdirs();

        Map params = new HashMap();
        int port = testUtil.testPort();
        params.put(MysqldResourceI.PORT, Integer.toString(port));
        params.put(MysqldResourceI.KILL_DELAY, Integer.toString(testUtil
                .testKillDelay()));

        mysqld.start("mxj-user-test", params);

        // String url = "jdbc:mysql://127.0.0.1:" + port + "/";
        String url = "jdbc:mysql://localhost:" + port + "/";
        String rootUser = "root";
        String rootPassword = "";
        makeDb(url + "test", rootUser, rootPassword);
        checkVersion(mysqld.getVersion());

        conn.close();
        conn = null;

        com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
        Properties props = new Properties();
        props.setProperty("user", "JAVA");
        props.setProperty("password", "SAPR3");

        Exception exception = null;
        try {
            conn = driver.connect(url + "MY1", props);
        } catch (Exception e) {
            exception = e;
        }

        assertNull("" + exception, exception);

        checkVersion(mysqld.getVersion());
    }

    private void checkVersion(String version) {
        QueryUtil util = new QueryUtil(conn);
        String searchIn = util.queryForString("SELECT VERSION()");
        assertTrue("<" + version + "> not found in <" + searchIn + ">",
                new Str().containsIgnoreCase(searchIn, version));
    }

    private void makeDb(String url, String userName, String password)
            throws Exception {
        testUtil.assertConnectViaJDBC(url, userName, password, true);
        Class.forName(com.mysql.jdbc.Driver.class.getName());
        conn = DriverManager.getConnection(url, userName, password);
        QueryUtil util = new QueryUtil(conn);
        util.execute("CREATE DATABASE MY1");
        util.execute("USE MY1");

        String sql = "GRANT ALL PRIVILEGES ON MY1.*"
                + " TO 'JAVA'@'%' IDENTIFIED BY 'SAPR3'" + " WITH GRANT OPTION";
        util.execute(sql);

        sql = "GRANT ALL PRIVILEGES ON MY1.*"
                + " TO 'JAVA'@'localhost' IDENTIFIED BY 'SAPR3'"
                + " WITH GRANT OPTION";
        util.execute(sql);

        util.execute("commit");
    }

    public void testInitializeUser() throws Exception {
        File baseDir4 = new File(tmpDir, "mxj-init-user-test");
        fileUtil.deleteTree(baseDir4);
        mysqld = new MysqldResource(baseDir4);
        mysqld.setKillDelay(testUtil.testKillDelay());

        Map params = new HashMap();
        int port = testUtil.testPort();
        params.put(MysqldResourceI.PORT, Integer.toString(port));

        String rootUser = "root";
        String rootPass = "";
        String aliceName = "alice";
        String alicePass = "q3htgi98q34";

        mysqld.start("init-user", params);

        String url = "jdbc:mysql://127.0.0.1:" + port + "/test";
        conn = DriverManager.getConnection(url, rootUser, rootPass);
        QueryUtil util = new QueryUtil(conn);
        util.execute("SELECT 1");
        conn.close();
        conn = null;

        Exception exception = null;
        try {
            conn = DriverManager.getConnection(url, aliceName, alicePass);
            fail("Should not be able to connect as " + aliceName);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull("" + exception, exception);

        mysqld.setKillDelay(testUtil.testKillDelay());
        mysqld.shutdown();
        assertEquals(false, mysqld.isRunning());
        fileUtil.deleteTree(baseDir4);

        params.put(MysqldResourceI.INITIALIZE_USER, Boolean.TRUE.toString());
        params.put(MysqldResourceI.INITIALIZE_USER_NAME, aliceName);
        params.put(MysqldResourceI.INITIALIZE_PASSWORD, alicePass);
        mysqld.start("init-user", params);

        conn = DriverManager.getConnection(url, aliceName, alicePass);
        util = new QueryUtil(conn);
        util.execute("SELECT 1");
        conn.close();
        conn = null;

        exception = null;
        try {
            conn = DriverManager.getConnection(url, rootUser, rootPass);
            fail("Should not be able to connect as " + rootUser);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull("" + exception, exception);

        mysqld.setKillDelay(testUtil.testKillDelay());
        mysqld.shutdown();
    }
}
