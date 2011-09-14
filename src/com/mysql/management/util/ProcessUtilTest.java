/*
 Copyright (C) 2007-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.io.PrintStream;

import com.mysql.management.MysqldResourceI;

import junit.framework.TestCase;

public class ProcessUtilTest extends TestCase {
    private File dir;

    private PrintStream devNull;

    private String wk;

    private TestUtil testUtil;

    protected void setUp() {
        dir = new File(new Files().testDir(), "ProcUtiTest");
        devNull = new NullPrintStream();
        testUtil = new TestUtil();
        wk = testUtil.getSystemPropertyWithDefaultFromResource(
                MysqldResourceI.WINDOWS_KILL_COMMAND,
                MysqldResourceI.CONNECTOR_MXJ_PROPERTIES, System.err);

    }

    public void testNullPid() {
        ProcessUtil kp = new ProcessUtil(null, devNull, devNull, dir);
        assertEquals("-1", kp.pid());
    }

    public void testPidWithEOL() {
        String pid = " 3343\n";
        ProcessUtil kp = new ProcessUtil(pid, devNull, devNull, dir);
        assertEquals("3343", kp.pid());
    }

    public void testKillCommandLineUnix() {
        Utils utils = new Utils();
        utils.setFiles(new Files() {
            public boolean isWindows() {
                return false;
            }
        });
        String pid = "2342";
        ProcessUtil kp = new ProcessUtil(pid, devNull, devNull, dir, utils, wk);
        String[] args = kp.killArgs(false);
        assertEquals(args[0], "kill");
        assertEquals(pid, args[args.length - 1]);
    }

    public void testKillCommandLineWindows() {
        Utils utils = new Utils();
        utils.setFiles(new Files() {
            public boolean isWindows() {
                return true;
            }
        });
        String pid = "2342";
        ProcessUtil kp = new ProcessUtil(pid, devNull, devNull, dir, utils, wk);
        String[] args = kp.killArgs(false);
        assertTrue(args[0], args[0].endsWith("kill.exe"));
        assertEquals(pid, args[args.length - 1]);
    }

    public void testForce() {
        ProcessUtil kp = new ProcessUtil("4321", devNull, devNull, dir);
        String[] args = kp.killArgs(true);
        assertEquals("-9", args[1]);
    }

    public void testIsRunning() {
        String pid = "5234";
        ProcessUtil kp = new ProcessUtil(pid, devNull, devNull, dir);
        String[] args = kp.isRunningArgs();
        testUtil.assertContainsIgnoreCase(args[0], "kill");
        assertEquals("-0", args[1]);
        assertEquals(pid, args[2]);
    }

    public void testFileCreation() {
        ProcessUtil pu = new ProcessUtil("1234", devNull, devNull, dir);
        File winKill;
        winKill = pu.getWindowsKillFile(wk);
        assertTrue(winKill.exists());
        winKill.delete();
        assertFalse(winKill.exists());
        winKill = pu.getWindowsKillFile(wk);
        assertTrue(winKill.exists());
    }
}
