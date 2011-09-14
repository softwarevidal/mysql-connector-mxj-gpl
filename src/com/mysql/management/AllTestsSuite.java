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

import junit.framework.Test;
import junit.framework.TestSuite;

import com.mysql.management.util.Files;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: AllTestsSuite.java,v 1.6 2005/07/01 02:06:55 eherman Exp $
 */
public class AllTestsSuite {
    public static Test suite() {
        new Files().cleanTestDir();
        TestSuite suite = new TestSuite();

        suite.addTest(com.mysql.management.util.AllTestsSuite.suite());
        suite
                .addTest(com.mysql.management.driverlaunched.AllTestsSuite
                        .suite());

        suite.addTestSuite(HelpOptionsParserTest.class);

        suite.addTest(com.mysql.management.jmx.AllTestsSuite.suite());

        // slow tests:
        suite.addTestSuite(MysqldResourceTest.class);
        suite.addTestSuite(AcceptanceTest.class);
        suite
                .addTestSuite(com.mysql.management.driverlaunched.AcceptanceTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.main(new String[] { AllTestsSuite.class
                .getName() });
    }
}
