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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;

import com.mysql.management.util.Files;
import com.mysql.management.util.NullPrintStream;
import com.mysql.management.util.QuietTestCase;
import com.mysql.management.util.Shell;
import com.mysql.management.util.Str;
import com.mysql.management.util.Streams;
import com.mysql.management.util.TestUtil;
import com.mysql.management.util.Threads;
import com.mysql.management.util.Utils;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldResourceTest.java,v 1.36 2005/12/01 21:45:31 eherman Exp $
 */
public class MysqldResourceTest extends QuietTestCase {

    private MysqldResource mysqldResource;

    private TestFileUtil fileUtil;

    private TestUtil testUtil;

    private Utils utils;

    private File baseDir;

    private File dataDir;

    protected void setUp() {
        super.setUp();
        testUtil = new TestUtil();
        fileUtil = new TestFileUtil();
        utils = new Utils(fileUtil, new Shell.Factory(), new Streams(),
                new Threads(), new Str());

        baseDir = new File(fileUtil.testDir(), "MRTest");
        dataDir = new File(baseDir, "data");
        fileUtil.deleteTree(baseDir);
        if (baseDir.exists()) {
            warn("residual files");
        }

        mysqldResource = new MysqldResource(baseDir, dataDir, null, System.out,
                System.err, utils);
        mysqldResource.setKillDelay(testUtil.testKillDelay());
    }

    protected void tearDown() {
        utils.threads().pause(50);
        try {
            if (mysqldResource.isRunning()) {
                mysqldResource.setKillDelay(testUtil.testKillDelay());
                mysqldResource.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileUtil.deleteTree(baseDir);
        super.tearDown();
    }

    private void setSystemPropertiesToWinNT() {
        mysqldResource.setOsAndArch("Windows NT", "x86");
        assertTrue(mysqldResource.isWindows());
    }

    private void setSytemPropertiesToLinux() {
        mysqldResource.setOsAndArch("Linux", "i386");
        assertFalse(mysqldResource.isWindows());
    }

    public void testLaunch() throws Exception {
        Map args = new HashMap();
        String port = "" + testUtil.testPort();
        args.put(MysqldResourceI.PORT, port);
        String url = "jdbc:mysql://localhost:" + port + "/test";
        String threadName = "testLaunch";
        assertFalse("mysqld should not be running", mysqldResource.isRunning());

        mysqldResource.start(threadName, args);
        Shell s1 = mysqldResource.getShell();

        /* this asserts the thread starts */
        assertRunning();

        /* pause for mysqld to bind to port */
        for (int i = 0; i < 100 && !mysqldResource.isReadyForConnections(); i++) {
            utils.threads().pause(25);
        }

        testUtil.assertConnectViaJDBC(url, true);
        mysqldResource.start(threadName, args);
        Shell s2 = mysqldResource.getShell();
        assertEquals(s1, s2);
        assertTrue(s1.isDaemon());
        assertRunningThenShutdown();
    }

    public void testUseDatabase() throws Exception {
        String url = "jdbc:mysql://localhost:" + testUtil.testPort() + "/test";
        String threadName = "testLaunch";
        assertFalse("mysqld should not be running", mysqldResource.isRunning());

        startMysql(threadName);
        Shell s1 = mysqldResource.getShell();

        /* this asserts the thread starts */
        assertRunning();

        /* pause for mysqld to bind to port */
        for (int i = 0; i < 100 && !mysqldResource.isReadyForConnections(); i++) {
            utils.threads().pause(25);
        }

        testUtil.assertConnectViaJDBC(url);
        if (!mysqldResource.isWindows()) {
            File sockFile = new File(mysqldResource.getDataDir(), "mysql.sock");
            assertTrue(sockFile.exists());
        }

        mysqldResource.start(threadName, new HashMap());
        Shell s2 = mysqldResource.getShell();
        assertEquals(s1, s2);
        assertTrue(s1.isDaemon());
        assertRunningThenShutdown();
    }

    private void startMysql(String threadName) {
        Map map = new HashMap();
        map.put(MysqldResourceI.PORT, "" + testUtil.testPort());
        mysqldResource.start(threadName, map);
        assertTrue(mysqldResource.isRunning());
    }

    public void testGetFileName() {
        File mysqld = mysqldResource.getMysqldFilePointer();
        assertNotNull(mysqld);
        String name = mysqld.getPath();
        assertTrue(name, name.indexOf("mysqld") > 0);
    }

    public void testWindowsFileName() {
        setSystemPropertiesToWinNT();
        String resourceName = mysqldResource.getResourceName();
        String fileName = mysqldResource.getMysqldFilePointer().getName();
        assertTrue(resourceName.indexOf(".exe") > 0);
        assertTrue(fileName.indexOf(".exe") > 0);
    }

    public void testUglyPlatformName() {
        assertEquals("a_b_c_d", mysqldResource.stripUnwantedChars("a b/c\\d"));
        mysqldResource.setOsAndArch("Bogus OS W/ Spaces", "\\Arch in Space");
        String resourceName = mysqldResource.getResourceName();
        assertTrue(resourceName.indexOf(' ') == -1);
        assertTrue(resourceName.indexOf('\\') == -1);
        String expected = "Bogus_OS_W__Spaces-_Arch_in_Space";
        assertTrue(resourceName.indexOf(expected) >= -1);
    }

    private void checkMysqldFile() {
        File mysqld = mysqldResource.makeMysqld();
        assertTrue(mysqld.exists());
        assertTrue(mysqld.length() > 100);
    }

    public void testGetMysqldNative() {
        checkMysqldFile();
    }

    public void testGetMysqldWinNT() {
        setSystemPropertiesToWinNT();
        checkMysqldFile();
    }

    public void testGetMysqldLinux() {
        setSytemPropertiesToLinux();
        checkMysqldFile();
        assertTrue(fileUtil.madeExecutable(mysqldResource
                .getMysqldFilePointer()));
    }

    public void testUnknownOsShouldSetDefaultOSOrWindowsOS() {
        mysqldResource.setOsAndArch("bogus", "x86");
        assertEquals("Win-x86", mysqldResource.os_arch());
    }

    public void testCreateDbFiles() {
        new Files().deleteTree(dataDir);

        File dbDataDir = new File(dataDir, "mysql");
        File host_frm = new File(dbDataDir, "host.frm");
        assertEquals(false, host_frm.exists());
        assertEquals(false, dataDir.exists());

        mysqldResource.ensureEssentialFilesExist();

        assertTrue(host_frm.exists());
    }

    public void testBug35804() {
        testCreateDbFiles();

        File dbDataDir = new File(dataDir, "mysql");
        File host_frm = new File(dbDataDir, "host.frm");

        assertTrue(host_frm.delete());
        assertFalse(host_frm.exists());
        mysqldResource.ensureEssentialFilesExist();
        assertFalse(host_frm.exists());
    }

    private void assertRunningThenShutdown() {
        assertRunning();
        mysqldResource.shutdown();
        assertNotRunning();
    }

    private void assertNotRunning() {
        for (int i = 0; i < 500; i++) {
            if (!mysqldResource.isRunning())
                break;
            utils.threads().pause(25);
        }
        assertFalse("mysqld should not be running", mysqldResource.isRunning());
    }

    private void assertRunning() {
        for (int i = 0; i < 500; i++) {
            if (mysqldResource.isRunning())
                break;
            utils.threads().pause(25);
        }
        assertTrue("mysqld should be running", mysqldResource.isRunning());
    }

    public void testServerOptions() {
        Map optionsMap = mysqldResource.getServerOptions();
        String expectedBaseDir = mysqldResource.getBaseDir().getPath();
        assertEquals(expectedBaseDir, optionsMap.get(MysqldResourceI.BASEDIR));
    }

    public void testTestReporting() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos);
        mysqldResource = new MysqldResource(baseDir, dataDir, null, captured,
                captured, utils);

        mysqldResource.reportIfNoPidfile(true);
        captured.flush();
        assertEquals("", new String(baos.toByteArray()));

        mysqldResource.reportIfNoPidfile(false);
        captured.flush();
        String output = new String(baos.toByteArray());
        testUtil.assertContainsIgnoreCase(output, "pid-file not found");
        testUtil.assertContainsIgnoreCase(output, baseDir.toString());
    }

    public void testForceKill() {
        startMysql("killMe");
        assertTrue(mysqldResource.isRunning());
        mysqldResource.issueForceKill();
        assertFalse(mysqldResource.isRunning());
    }

    public void testDestroyShell() {
        /** TODO: improve this test */
        startMysql("DestroyMe");
        assertTrue(mysqldResource.isRunning());
        mysqldResource.destroyShell();
        if (mysqldResource.isRunning()) {
            new MysqldResource(fileUtil.nullFile()).shutdown();
        }
    }

    public void testVersion() {
        assertNotNull(mysqldResource.getVersion());
        assertTrue(mysqldResource.getVersion().indexOf("5") >= 0);
        mysqldResource.setVersion("5.11.42");
        assertEquals("5.11.42", mysqldResource.getVersion());
    }

    public void testNoPidFile() {
        assertEquals(mysqldResource.pid(), "No PID");
        startMysql("pid file");
        assertTrue(Integer.parseInt(mysqldResource.pid()) > 0);
    }

    public void testTestFinalize() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos);

        mysqldResource = new MysqldResource(baseDir, dataDir, "", captured,
                captured, utils);
        mysqldResource.finalize();
        captured.flush();
        String output = new String(baos.toByteArray());
        assertEquals("", output);

        PrintStream devNull = new NullPrintStream();
        String[] none = new String[0];
        mysqldResource.setShell(new Shell.Default(none, "bogus", devNull,
                devNull));

        mysqldResource.finalize();
        captured.flush();
        output = new String(baos.toByteArray());
        testUtil.assertContainsIgnoreCase(output, "MysqldResource.initTrace");
    }

    public void testUsage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream captured = new PrintStream(baos);
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(captured));
        try {
            MysqldResource.main(new String[] { "--help" });
        } finally {
            System.setOut(stdout);
        }
        assertTrue(baos.toString().indexOf("Usage") >= 0);
    }

    public void testJarName() {
        Properties props = utils.streams().loadProperties(
                "connector-mxj.properties", System.err);
        String expected = props.getProperty("windows-share-dir-jar",
                "win_share_dir.jar");

        setSystemPropertiesToWinNT();
        assertEquals(expected, mysqldResource.shareJar());
        setSytemPropertiesToLinux();
        assertEquals("share_dir.jar", mysqldResource.shareJar());
    }

    public void testCanConnectToServer() {
        assertFalse(mysqldResource.canConnectToServer(testUtil.testPort(), 1));
        startMysql("testCanConnectToServer");
        assertTrue(mysqldResource.canConnectToServer(testUtil.testPort(), 1));
    }

    public void testPlatformMapProperties() {

        mysqldResource.setOsAndArch("Linux", "x86_64");
        assertEquals("Linux-i386", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("Linux", "i686");
        assertEquals("Linux-i386", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("Windows NT", "x86");
        assertEquals("Win-x86", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("Windows XP", "x86");
        assertEquals("Win-x86", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("Mac OS X", "i386");
        assertEquals("Mac_OS_X-i386", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("Mac OS X", "ppc");
        assertEquals("Mac_OS_X-ppc", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("SunOS", "sparc");
        assertEquals("SunOS-sparc", mysqldResource.os_arch());

        mysqldResource.setOsAndArch("SunOS", "i386");
        assertEquals("SunOS-i386", mysqldResource.os_arch());
    }

    public void testGetWindowsKillCommand() {
        Properties props = new Properties();
        assertEquals("kill.exe", mysqldResource.getWindowsKillCommand(props));
        props.setProperty(MysqldResourceI.WINDOWS_KILL_COMMAND, " kill.exe ");
        assertEquals("kill.exe", mysqldResource.getWindowsKillCommand(props));
        props.setProperty(MysqldResourceI.WINDOWS_KILL_COMMAND, " ");
        assertEquals("kill.exe", mysqldResource.getWindowsKillCommand(props));
        props.setProperty(MysqldResourceI.WINDOWS_KILL_COMMAND, " foo.bar ");
        assertEquals("foo.bar", mysqldResource.getWindowsKillCommand(props));
    }

    // -------------------
    private static class TestFileUtil extends Files {
        private List execFiles = new ArrayList();

        public void addExecutableRights(File mysqld, PrintStream out,
                PrintStream err) {
            execFiles.add(mysqld);
            super.addExecutableRights(mysqld, out, err);
        }

        public boolean madeExecutable(File file) {
            return execFiles.contains(file);
        }
    }
}
