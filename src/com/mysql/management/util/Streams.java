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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Stream operation utility methods.
 * 
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: Streams.java,v 1.12 2005/12/01 21:45:31 eherman Exp $
 */
public class Streams {

    public static final String RESOURCE_SEPARATOR = "/";

    private static final int END_OF_STREAM = -1;

    private Exceptions exceptions;

    public Streams() {
        this.exceptions = new Exceptions();
    }

    /**
     * Reads the data from the Input stream and writes to the output stream
     * Buffers each stream. Terminates when a read from the Input stream results
     * in EOF
     */
    public void copy(InputStream from, OutputStream to) throws IOException {
        copy(from, to, true, false);
    }

    void copy(InputStream from, OutputStream to, boolean buffer,
            boolean terminateOnFailure) throws IOException {
        if (buffer) {
            from = new BufferedInputStream(from);
            to = new BufferedOutputStream(to);
        }
        while (true) {
            int i;
            try {
                i = from.read();
                if (i == END_OF_STREAM) {
                    break;
                }
                to.write(i);
            } catch (Exception e) {
                if (terminateOnFailure) {
                    break;
                }
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw exceptions.toRuntime(e);
            }
        }
        to.flush();
    }

    /** reads the entire contents of stream into a String */
    public String readString(InputStream from) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            copy(from, buf);
            return buf.toString();
        } finally {
            buf.close();
        }
    }

    /**
     * Uses ClassLoader which loaded Streams.class to obtain resource
     * 
     * @return an InputStream
     * @throws MissingResourceException
     */
    public InputStream getResourceAsStream(String name) {
        return getResourceAsStream(getClass(), name);
    }

    /**
     * Convenience method to check for MissingResource (null stream)
     * 
     * @return an InputStream
     * @throws MissingResourceException
     */
    public InputStream getResourceAsStream(Class aClass, String name) {
        InputStream is =  aClass.getResourceAsStream(name);
        if (is == null) {
            is = aClass.getResourceAsStream("/" + name);
        }
        if (is == null) {
            String msg = "Resource '" + name + "' not found";
            throw new MissingResourceException(msg, aClass.getName(), name);
        }
        return is;
    }

    /**
     * Copies a resource to the location specified by the File parameter.
     */
    public void createFileFromResource(final String resourceName,
            final File file) {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() throws Exception {
                InputStream is = getResourceAsStream(resourceName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        copy(is, fos);
                    } finally {
                        fos.close();
                    }
                } finally {
                    is.close();
                }
            }
        };
        block.exec();

    }

    /**
     * If the jar exists as a resource, the contents of the jar will be expanded
     * on the file system in the location specified by the File parameter.
     * 
     * If any files already exist, they will <b>not </b> be over-written.
     */
    public void expandResourceJar(final File outputDir,
            final String jarResourceName) {
        Exceptions.VoidBlock block = new Exceptions.VoidBlock() {
            public void inner() throws Exception {
                expandResourceJarInner(outputDir, jarResourceName);
            }
        };
        block.exec();
    }

    private void expandResourceJarInner(File outputDir, String jarResourceName)
            throws IOException {
        InputStream is = getResourceAsStream(jarResourceName);
        try {
            JarInputStream jis = new JarInputStream(is);
            try {
                expandEachEntry(outputDir, jis);
            } finally {
                jis.close();
            }
        } finally {
            is.close();
        }
    }

    private void expandEachEntry(File outputDir, JarInputStream jis)
            throws IOException, FileNotFoundException {
        while (true) {
            JarEntry entry = jis.getNextJarEntry();
            if (entry == null) {
                break;
            }

            File file = new File(outputDir, entry.getName());
            if (!file.exists()) {
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    parent.mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        copy(jis, fos);
                    } finally {
                        fos.close();
                    }
                }
            }
        }
    }

    public Properties loadProperties(String resourceName, PrintStream err) {
        final InputStream is = getResourceAsStream(resourceName);

        Properties props = new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace(err);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace(err);
            }
        }

        return props;
    }
}
