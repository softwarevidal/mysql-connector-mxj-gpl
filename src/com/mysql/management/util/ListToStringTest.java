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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: StrTest.java,v 1.3 2005/12/01 21:45:31 eherman Exp $
 */
public class ListToStringTest extends TestCase {
    private ListToString ltos;

    protected void setUp() {
        ltos = new ListToString();
    }

    public void testArray() throws Exception {
        assertEquals("[foo]", ltos.toString(new Object[] { "foo" }));
        assertEquals("[foo][bar]", ltos.toString(new Object[] { "foo", "bar" }));
        Object[] objects = new Object[] { "foo", new Object[] { "bar", "baz" },
                "wiz" };
        assertEquals("[foo][[bar][baz]][wiz]", ltos.toString(objects));

        objects = new Object[] { "foo",
                Arrays.asList(new Object[] { "bar", "baz" }), "wiz" };
        assertEquals("[foo][[bar][baz]][wiz]", ltos.toString(objects));
    }

    public void testMap() throws Exception {
        Map map = new LinkedHashMap();
        map.put("foo", null);
        map.put("bar", "baz");
        map.put("wiz", new String[] { "bang" });
        assertEquals("[foo=null][bar=baz][wiz=[bang]]", ltos.toString(map));
    }
}
