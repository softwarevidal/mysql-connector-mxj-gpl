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

import java.io.InputStream;
import java.io.OutputStream;

public class TestStubProcess extends Process {

    public int exitValue() {
        throw new RuntimeException("not implemented");
    }

    public int waitFor() { // throws InterruptedException {
        throw new RuntimeException("not implemented");
    }

    public void destroy() {
        throw new RuntimeException("not implemented");
    }

    public InputStream getErrorStream() {
        throw new RuntimeException("not implemented");
    }

    public InputStream getInputStream() {
        throw new RuntimeException("not implemented");
    }

    public OutputStream getOutputStream() {
        throw new RuntimeException("not implemented");
    }
}
