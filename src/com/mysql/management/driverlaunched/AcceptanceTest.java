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
import java.net.URLEncoder;
import java.sql.SQLException;

import com.mysql.management.MysqldResource;
import com.mysql.management.util.Files;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.TestUtil;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AcceptanceTest.java,v 1.14 2005/12/02 16:00:50 eherman Exp $
 */
public class AcceptanceTest extends QuietTestCase {

    private int port;

    private String orig;

    private File dataDir;

    private TestUtil testUtil;

    protected void setUp() {
        super.setUp();
        try {
            new com.mysql.jdbc.Driver();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        orig = System.getProperty(Files.USE_TEST_DIR);
        System.setProperty(Files.USE_TEST_DIR, Boolean.TRUE.toString());
        testUtil = new TestUtil();
    }

    protected void tearDown() {
        super.tearDown(); // un-quiet so we can see errors.

        try {
            File baseDir = new Files().tmp(MysqldResource.MYSQL_C_MXJ);
            ServerLauncherSocketFactory.shutdown(baseDir, dataDir, testUtil
                    .testKillDelay());
        } catch (MysqldResourceNotFoundException e) {
            warn(e);
        } finally {
            if (orig != null) {
                System.setProperty(Files.USE_TEST_DIR, orig);
            } else {
                // System.clearProperty(Files.USE_TEST_DIR);
                System.setProperty(Files.USE_TEST_DIR, "");
            }
            new Files().cleanTestDir();
            if (dataDir != null) {
                new Files().deleteTree(dataDir);
            }
        }
    }

    public void testServerDriverLauncherFactory() throws Exception {
        dataDir = new File(new Files().tmp("TestApp"), "data");
        String safePath = URLEncoder.encode(dataDir.getPath(), "UTF-8");

        port = testUtil.testPort();

        String url = "jdbc:mysql:mxj://localhost:" + port + "/alice_db" //
                + "?server.datadir=" + safePath //
                + "&server.initialize-user=true"//
                + "&createDatabaseIfNotExist=true"//
        ;

        testUtil.assertConnectViaJDBC(url, "alice", "opt4g01396");
        testUtil.assertConnectViaJDBC(url, "alice", "opt4g01396");
        testUtil.assertConnectViaJDBC(url, "alice", "opt4g01396");
    }
}
