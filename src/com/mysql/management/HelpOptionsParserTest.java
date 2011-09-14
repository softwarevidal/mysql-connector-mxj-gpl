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
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;

import junit.framework.TestCase;

import com.mysql.management.util.ListToString;
import com.mysql.management.util.Streams;
import com.mysql.management.util.TestUtil;
import com.mysql.management.util.Utils;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: HelpOptionsParserTest.java,v 1.1 2005/02/16 21:46:11 eherman
 *          Exp $
 */
public class HelpOptionsParserTest extends TestCase {

    public void testOptionParser() throws Exception {
        String resourceVersion = new TestUtil()
                .getSystemPropertyWithDefaultFromResource(
                        MysqldResourceI.MYSQLD_VERSION,
                        MysqldResourceI.CONNECTOR_MXJ_PROPERTIES, System.err);
        String resourceVersionDir = resourceVersion.replaceAll("\\.", "-");
        String sampleHelp = resourceVersionDir
                + "/com/mysql/management/MySQL_Help.txt";

        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(sampleHelp);
        String help = new Streams().readString(is);
        is.close();

        HelpOptionsParser parser = new HelpOptionsParser(System.err,
                new Utils());
        Map parsed = parser.getOptionsFromHelp(help);
        String optStr = new ListToString().toString(parsed);
        assertEquals(optStr, "TRUE", parsed.get("auto-rehash"));
        // assertEquals("/usr/local/mysql/",
        // parsed.get(MysqldResourceI.BASEDIR));
        // assertEquals("FALSE", parsed.get("bdb"));
        // assertEquals("(No default value)", parsed.get("time-format"));
        // assertTrue(parsed.containsKey("time_format"));
        // assertEquals("", parsed.get("time_format"));
    }

    public void testTrimOptionsErrorMsg() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HelpOptionsParser parser = new HelpOptionsParser(new PrintStream(out),
                new Utils());
        Exception expected = null;
        try {
            parser.trimToOptions("bogus");
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull(expected);
        String errMsg = new String(out.toByteArray());
        assertTrue(errMsg.indexOf("bogus") >= 0);
    }

    public void testOptionParserSupportIssue8601() throws Exception {
        String text = "support-issue-8601-help-output.txt";

        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(text);
        String help = new Streams().readString(is);
        is.close();

        HelpOptionsParser parser = new HelpOptionsParser(System.err,
                new Utils());
        Map parsed = parser.getOptionsFromHelp(help);
        // System.err.println(new ListToString().toString(parsed));
        assertEquals("FALSE", parsed.get("allow-suspicious-udfs"));
    }
}
