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

import java.util.Map;

/**
 * Simple (and obvious) implementation of java.util.Map.Entry
 * 
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MapEntry.java,v 1.5 2005/09/22 20:41:36 eherman Exp $
 */
public final class MapEntry implements Map.Entry {
    private Object key;

    private Object value;

    private Equals equals;

    public MapEntry(Object key, Object value) {
        this.key = key;
        this.value = value;
        this.equals = new Equals();
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Object setValue(Object value) {
        Object oldVal = this.value;
        this.value = value;
        return oldVal;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Map.Entry)) {
            return false;
        }
        return equals((Map.Entry) obj);
    }

    public boolean equals(Map.Entry other) {
        if (other == this) {
            return true;
        }

        if ((other == null) || (hashCode() != other.hashCode())) {
            return false;
        }

        return equals.nullSafe(key, other.getKey())
                && equals.nullSafe(value, other.getValue());
    }

    /**
     * XOR of the key and value hashCodes. (Zero used for nulls) as defined by
     * Map.Entry java doc.
     */
    public int hashCode() {
        int keyHashCode = (key == null) ? 0 : key.hashCode();
        int valHashCode = (value == null) ? 0 : value.hashCode();
        return keyHashCode ^ valHashCode;
    }

    public String toString() {
        return getKey() + "=" + getValue();
    }
}
