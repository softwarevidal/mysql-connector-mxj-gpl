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

import java.io.PrintStream;

import junit.framework.TestCase;

public abstract class QuietTestCase extends TestCase {
    private PrintStream systemDotOut;

    private PrintStream systemDotErr;

    protected void setUp() {
        this.systemDotOut = System.out;
        this.systemDotErr = System.err;
        System.setOut(getTestStream(systemDotOut));
        System.setErr(getTestStream(systemDotErr));
    }

    protected void tearDown() {
        resetOutAndErr();
    }

    protected void resetOutAndErr() {
        System.setOut(systemDotOut);
        System.setErr(systemDotErr);
    }

    protected void warn(String msg) {
        systemDotErr.println(msg);
    }

    protected void warn(Exception e) {
        e.printStackTrace(systemDotErr);
    }

    private PrintStream getTestStream(PrintStream real) {
        String defaultVal = Boolean.TRUE.toString();
        String silentStr = System.getProperty("c-mxj_test_silent", defaultVal);
        Boolean b = Boolean.valueOf(silentStr);
        return b.booleanValue() ? new NullPrintStream() : real;
    }
}
