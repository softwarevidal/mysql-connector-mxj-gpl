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

import java.io.File;

public final class Utils {
    private Files files;

    private Streams streams;

    private Shell.Factory shellFactory;

    private Threads threads;

    private Str str;

    public Utils() {
        this.shellFactory = new Shell.Factory();
        this.str = new Str();
        this.streams = new Streams();
        this.threads = new Threads();
        this.files = new Files(shellFactory, File.separatorChar, streams);
    }

    public Utils(Files files, Shell.Factory shellFactory, Streams streams,
            Threads threads, Str str) {
        this.files = files;
        this.shellFactory = shellFactory;
        this.str = str;
        this.streams = streams;
        this.threads = threads;
    }

    public Files files() {
        return files;
    }

    public Streams streams() {
        return streams;
    }

    public Shell.Factory shellFactory() {
        return shellFactory;
    }

    public Threads threads() {
        return threads;
    }

    public Str str() {
        return str;
    }

    public void setFiles(Files files) {
        this.files = files;
    }

    public void setStreams(Streams streams) {
        this.streams = streams;
    }
}
