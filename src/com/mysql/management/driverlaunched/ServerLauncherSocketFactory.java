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
package com.mysql.management.driverlaunched;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mysql.jdbc.Driver;
import com.mysql.jdbc.MysqlErrorNumbers;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.SocketFactory;
import com.mysql.jdbc.StandardSocketFactory;
import com.mysql.management.MysqldFactory;
import com.mysql.management.MysqldResource;
import com.mysql.management.MysqldResourceI;
import com.mysql.management.util.Exceptions;
import com.mysql.management.util.Files;
import com.mysql.management.util.SQLRuntimeException;
import com.mysql.management.util.Str;

/**
 * This class is final simply as a hint to the compiler, it may be un-finalized
 * safely.
 * 
 * @author Eric Herman <eric@mysql.com>
 */
public final class ServerLauncherSocketFactory implements SocketFactory {

    public static final String SERVER_DOT = "server.";

    private static int launchCount = 0;

    private MysqldFactory resourceFactory;

    private SocketFactory socketFactory;

    private boolean throwOnBadPassword;

    public ServerLauncherSocketFactory() {
        setResourceFactory(new MysqldFactory.Default());
        setSocketFactory(new StandardSocketFactory());
        throwOnBadPassword = true;
    }

    public Socket connect(String host, int portNumber, Properties props)
            throws SocketException, IOException {
        ensureMysqlStarted(portNumber, props);

        return getSocketFactory().connect(host, portNumber, props);
    }

    private void ensureMysqlStarted(int port, Properties props) {
        String user = props.getProperty(NonRegisteringDriver.USER_PROPERTY_KEY);
        String pass = props
                .getProperty(NonRegisteringDriver.PASSWORD_PROPERTY_KEY);

        if (alreadyStarted(port, user, pass)) {
            return;
        }

        Map serverOpts = new HashMap();
        for (Enumeration enums = props.propertyNames(); enums.hasMoreElements();) {
            String key = enums.nextElement().toString();
            String rawValue = props.getProperty(key);
            // System.err.println(key + ":" + rawValue);
            if (key.startsWith(SERVER_DOT)) {
                String val = replaceNullStringWithNull(rawValue);
                serverOpts.put(key.substring(SERVER_DOT.length()), val);
            }
        }

        serverOpts.put(MysqldResourceI.INITIALIZE_USER_NAME, user);
        serverOpts.put(MysqldResourceI.INITIALIZE_PASSWORD, pass);

        serverOpts.put(MysqldResourceI.PORT, Integer.toString(port));
        Object baseDirStr = serverOpts.get(MysqldResourceI.BASEDIR);
        File baseDir = new Files().newFile(baseDirStr);

        String dataDirString = (String) serverOpts.get(MysqldResourceI.DATADIR);

        File dataDir = null;
        if (dataDirString != null) {
            File ddir = new File(dataDirString);
            dataDir = new Files().validCononicalDir(ddir);
        }

        String mysqldVersion = (String) serverOpts
                .get(MysqldResourceI.MYSQLD_VERSION);

        MysqldResourceI mysqld = resourceFactory.newMysqldResource(baseDir,
                dataDir, mysqldVersion);

        if (mysqld.isRunning()) {
            int runningPort = mysqld.getPort();
            if (port != runningPort) {
                /* wait and try again in case of read failure */
                new Exceptions.VoidBlock() {
                    public void inner() throws InterruptedException {
                        Thread.sleep(10);
                    }
                }.exec();
                runningPort = mysqld.getPort();
            }
            if (runningPort <= 0) {
                System.err.println("unable to confirm running port of " + port);
            } else if (port != runningPort) {
                String location = mysqld.getBaseDir().getPath();
                if (dataDir != null) {
                    location += " with data at " + dataDir;
                }
                String msg = "Mysqld at " + location + " is running on port "
                        + runningPort + " not " + port;
                throw new RuntimeException(msg);
            }
            return;
        }

        launchCount++;
        String threadName = "driver_launched_mysqld_" + launchCount;
        mysqld.start(threadName, serverOpts);
    }

    private boolean alreadyStarted(int port, String user, String pass) {
        new Str().classForName(Driver.class.getName());
        Connection conn = null;
        String url = "jdbc:mysql://127.0.0.1:" + port + "/"
                + "?connectTimeout=150";

        try {
            conn = DriverManager.getConnection(url, user, pass);
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == MysqlErrorNumbers.ER_ACCESS_DENIED_ERROR
                    && throwOnBadPassword) {
                throw new SQLRuntimeException(e);
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    String replaceNullStringWithNull(String str) {
        return String.valueOf((Object) null).equals(str) ? null : str;
    }

    public Socket afterHandshake() throws SocketException, IOException {
        return getSocketFactory().afterHandshake();
    }

    public Socket beforeHandshake() throws SocketException, IOException {
        return getSocketFactory().beforeHandshake();
    }

    void setResourceFactory(MysqldFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    MysqldFactory getResourceFactory() {
        return resourceFactory;
    }

    void setSocketFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    void throwOnBadPassword(boolean val) {
        throwOnBadPassword = val;
    }

    SocketFactory getSocketFactory() {
        return socketFactory;
    }

    // -------------------------------------------------------------
    public synchronized static boolean shutdown(File baseDir, File dataDir) {
        return shutdown(baseDir, dataDir, 0);
    }

    public synchronized static boolean shutdown(File baseDir, File dataDir,
            int killDelay) {
        MysqldResource mysqld = new MysqldResource(baseDir, dataDir);
        if (killDelay > 0) {
            mysqld.setKillDelay(killDelay);
        }
        mysqld.shutdown();
        return mysqld.isRunning();
    }
}
