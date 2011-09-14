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

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(CausedSQLExceptionTest.class);
        suite.addTestSuite(CommandLineOptionsParserTest.class);
        suite.addTestSuite(DefaultsMapTest.class);
        suite.addTestSuite(EqualsTest.class);
        suite.addTestSuite(ExceptionsTest.class);
        suite.addTestSuite(FilesTest.class);
        suite.addTestSuite(MapEntryTest.class);
        suite.addTestSuite(PlatformTest.class);
        suite.addTestSuite(ProcessUtilTest.class);
        suite.addTestSuite(RuntimeTest.class);
        suite.addTestSuite(ShellTest.class);
        suite.addTestSuite(StreamsTest.class);
        suite.addTestSuite(StreamConnectorTest.class);
        suite.addTestSuite(StrTest.class);

        return suite;
    }
}
