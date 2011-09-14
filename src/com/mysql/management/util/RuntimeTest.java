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

import junit.framework.TestCase;

public class RuntimeTest extends TestCase {

    public void testImplemented() {
        Runtime realRuntime = Runtime.getRuntime();
        RuntimeI runtime = new RuntimeI.Default();
        assertEquals(realRuntime.availableProcessors(), runtime
                .availableProcessors());
        assertEquals(realRuntime.freeMemory(), runtime.freeMemory());
        assertEquals(realRuntime.maxMemory(), runtime.maxMemory());
        assertEquals(realRuntime.totalMemory(), runtime.totalMemory());
    }

    public void testStub() throws Exception {
        new TestUtil().assertObjStubsInterface(new RuntimeI.Stub(),
                RuntimeI.class);
    }
}
