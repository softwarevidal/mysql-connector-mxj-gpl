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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MapEntryTest.java,v 1.2 2005/07/05 21:19:40 eherman Exp $
 */
public class MapEntryTest extends TestCase {
    private Map.Entry entry;

    private Map.Entry hashMapEntry;

    protected void setUp() throws Exception {
        setEntries("foo", "bar");
    }

    private void setEntries(String key, String val) {
        entry = new MapEntry(key, val);
        Map map = new HashMap();
        map.put(key, val);
        hashMapEntry = (Map.Entry) map.entrySet().iterator().next();
    }

    public void testGetters() {
        assertEquals(hashMapEntry.getKey(), entry.getKey());
        assertEquals(hashMapEntry.getValue(), entry.getValue());
    }

    public void testEquallity() {
        assertTrue(entry.equals(entry));
        assertEquals(hashMapEntry.hashCode(), entry.hashCode());
        assertTrue(hashMapEntry.equals(entry));
        assertTrue(entry.equals(hashMapEntry));

        assertFalse(entry.equals(hashMapEntry.toString()));
        assertFalse(entry.equals(new MapEntry(entry.getKey(), entry.getKey())));
        assertFalse(((MapEntry) entry).equals((Map.Entry) null));
    }

    public void testToString() {
        assertEquals(hashMapEntry.toString(), entry.toString());
    }

    public void testHashCode() throws Exception {
        assertEquals(hashMapEntry.hashCode(), entry.hashCode());

        setEntries("foo", null);
        assertEquals(hashMapEntry.hashCode(), entry.hashCode());

        setEntries(null, "bar");
        assertEquals(hashMapEntry.hashCode(), entry.hashCode());

        setEntries(null, null);
        assertEquals(hashMapEntry.hashCode(), entry.hashCode());
    }

    public void testSetter() {
        entry.setValue("baz");
        hashMapEntry.setValue("baz");
        testGetters();
        testEquallity();
    }
}
