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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: FilesTest.java,v 1.1 2005/08/30 18:20:23 eherman Exp $
 */
public class FilesTest extends TestCase {
    private Files fileUtil;

    private File foo;

    private File defaultDir;

    private TestUtil testUtil;

    protected void setUp() {
        fileUtil = new Files();
        testUtil = new TestUtil();
    }

    protected void tearDown() throws Exception {
        if (foo != null) {
            fileUtil.deleteTree(foo);
        }
        if (defaultDir != null) {
            fileUtil.deleteTree(defaultDir);
        }
    }

    public void testTempDir() throws Exception {
        String property = System.getProperty("java.io.tmpdir");
        assertNotNull(property);
        File javaTmp = new File(property);
        assertEquals(javaTmp.getPath(), true, javaTmp.exists());
        File testParent = fileUtil.testDir().getParentFile();
        assertEquals(javaTmp.getCanonicalFile(), testParent.getCanonicalFile());
    }

    private void writeToFile(File file, String text)
            throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter out = new PrintWriter(fos);
        out.print(text);
        out.close();
    }

    public void testDeleteTree() throws FileNotFoundException {
        foo = new File(fileUtil.testDir(), "foo");
        File bar = new File(foo, "bar");
        bar.mkdirs();
        assertEquals(true, bar.exists());

        File baz = new File(foo, "baz");
        writeToFile(baz, "baz");

        assertEquals(true, baz.exists());
        assertEquals(3, baz.length());

        assertEquals(true, fileUtil.deleteTree(foo));
        assertEquals(false, foo.exists());
    }

    public void testMakeExecutable() {
        class FakeShellStub extends Shell.Stub {
            String[] args;

            int runCalled = 0;

            public void run() {
                runCalled++;
            }
        }
        final FakeShellStub shell = new FakeShellStub();
        class FakeShellFactory extends Shell.Factory {
            public Shell newShell(String[] args, String name, PrintStream out,
                    PrintStream err) {
                assertNotNull(name);
                assertNotNull(out);
                assertNotNull(err);
                shell.args = args;
                return shell;
            }
        }

        fileUtil = new Files(new FakeShellFactory(), '\\', new Streams());
        fileUtil.addExecutableRights(new File("bogus"), System.out, System.err);
        assertNull(shell.args);
        assertEquals(0, shell.runCalled);

        fileUtil = new Files(new FakeShellFactory(), '/', new Streams());
        fileUtil.addExecutableRights(new File("bogus"), System.out, System.err);
        assertEquals(1, shell.runCalled);
        assertEquals(3, shell.args.length);
        assertEquals("chmod", shell.args[0]);
        assertEquals("+x", shell.args[1]);
        assertTrue(shell.args[2].indexOf("bogus") >= 0);
    }

    public void testValidCononicalDir() throws Exception {
        foo = fileUtil.tmp("foo");
        File cononicalFoo = foo.getCanonicalFile();
        defaultDir = fileUtil.tmp("defaultDir");

        File valid = fileUtil.validCononicalDir(foo);
        assertEquals(cononicalFoo, valid);

        valid = fileUtil.validCononicalDir(foo, defaultDir);
        assertEquals(cononicalFoo, valid);

        valid = fileUtil.validCononicalDir(null, defaultDir);
        assertEquals(defaultDir, valid);

        File testDir = fileUtil.testDir();
        assertEquals(testDir, fileUtil.validCononicalDir(testDir));

        File bar = new File(testDir, "junkFile.txt");
        bar.deleteOnExit();
        writeToFile(bar, "junk");

        Exception expected = null;
        try {
            fileUtil.validCononicalDir(bar);
        } catch (IllegalArgumentException e) {
            expected = e;
        }
        assertNotNull(expected);
        testUtil.assertContainsIgnoreCase(expected.getMessage(), "directory");

        expected = null;
        try {
            fileUtil.validCononicalDir(null);
        } catch (IllegalArgumentException e) {
            expected = e;
        }
        assertNotNull(expected);
        testUtil.assertContainsIgnoreCase(expected.getMessage(), "null");
    }

    public void testIsEmpty() throws Exception {
        foo = fileUtil.tmp("foo");
        assertEquals(false, foo.exists());
        assertEquals(true, fileUtil.isEmpty(foo));
        foo.mkdirs();
        assertEquals(true, foo.exists());
        assertEquals(true, foo.isDirectory());
        assertEquals(true, fileUtil.isEmpty(foo));
        File bar = new File(foo, "bar");
        assertEquals(false, bar.exists());
        assertEquals(true, fileUtil.isEmpty(bar));
        writeToFile(bar, "stuff");
        assertEquals(true, bar.exists());
        assertEquals(false, fileUtil.isEmpty(bar));
        assertEquals(false, fileUtil.isEmpty(foo));
        assertEquals(true, bar.delete());
        assertEquals(true, fileUtil.isEmpty(foo));
    }

}
