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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class CommandLineOptionsParserTest extends TestCase {
    public void testEmptyList() {
        List list = new ArrayList();
        CommandLineOptionsParser parser = new CommandLineOptionsParser(list);
        Map params = parser.asMap();
        assertEquals(0, params.size());
    }

    public void testList() {
        List list = new ArrayList();
        list.add("foo0");
        list.add("foo1=bar1");
        list.add("--foo2=bar2");
        list.add("foo3 = bar3");

        CommandLineOptionsParser parser = new CommandLineOptionsParser(list);
        Map params = parser.asMap();
        assertEquals(list.size(), params.size());

        assertEquals(null, params.get("foo0"));
        assertEquals("bar1", params.get("foo1"));
        assertEquals("bar2", params.get("foo2"));
        assertEquals("bar3", params.get("foo3"));
    }
}
