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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: StreamsTest.java,v 1.2 2005/07/21 03:08:02 eherman Exp $
 */
public class StreamsTest extends TestCase {
    private static final String EXCEPTION_MSG = "pretend";

    public void testStreamsCopyErrorSilent() throws Exception {
        ThrowingInputStream from = new ThrowingInputStream();
        ByteArrayOutputStream to = new ByteArrayOutputStream();
        boolean isBuffered = false;
        boolean terminateOnFalure = true;

        // close quitely if "terminateOnFalure = true"
        new Streams().copy(from, to, isBuffered, terminateOnFalure);
        assertEquals(from.data(), new String(to.toByteArray()));
    }

    public void testStreamsCopyError() throws Exception {
        checkCopyError(true);
        checkCopyError(false);
    }

    private void checkCopyError(boolean isIOException) throws Exception {
        ThrowingInputStream from = new ThrowingInputStream();
        from.ioexception = isIOException;
        ByteArrayOutputStream to = new ByteArrayOutputStream();
        boolean isBuffered = false;
        boolean terminateOnFalure = false;
        // throw exception if "terminateOnFalure = false"
        Exception expected = null;
        try {
            new Streams().copy(from, to, isBuffered, terminateOnFalure);
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull(expected);
        assertEquals(EXCEPTION_MSG, expected.getMessage());
        assertEquals(from.data(), new String(to.toByteArray()));
    }

    private static class ThrowingInputStream extends InputStream {
        private char c = '0';

        boolean ioexception;

        public int read() throws IOException {
            if (c < '5') {
                return c++;
            }
            if (ioexception) {
                throw new IOException(EXCEPTION_MSG);
            }
            throw new RuntimeException(EXCEPTION_MSG);
        }

        public void reset() {
            c = '0';
        }

        String data() {
            return "01234";
        }
    }
}
