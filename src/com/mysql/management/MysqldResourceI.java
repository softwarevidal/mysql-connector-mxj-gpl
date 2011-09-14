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
package com.mysql.management;

import java.io.File;
import java.util.Map;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldResourceI.java,v 1.20 2005/07/27 23:41:27 eherman Exp $
 */
public interface MysqldResourceI {
    public static final String PORT = "port";

    public static final String PID_FILE = "pid-file";

    public static final String BASEDIR = "basedir";

    public static final String DATADIR = "datadir";

    public static final String SOCKET = "socket";

    public static final String CONNECTOR_MXJ_PROPERTIES = "connector-mxj.properties";

    public static final String PLATFORM_MAP_PROPERTIES = "platform-map.properties";

    public static final String MYSQLD_VERSION = "mysql-version";

    public static final String INITIALIZE_USER = "initialize-user";

    public static final String INITIALIZE_USER_NAME = INITIALIZE_USER + ".user";

    public static final String INITIALIZE_PASSWORD = INITIALIZE_USER
            + ".password";

    public static final String KILL_DELAY = "kill-delay";

    public static final String WINDOWS_KILL_COMMAND = "windows-kill-command";

    void setVersion(String version);

    String getVersion();

    void start(String threadName, Map mysqldArgs);

    void start(String threadName, Map mysqldArgs, boolean populateAllOptions);

    void shutdown();

    Map getServerOptions();

    boolean isRunning();

    boolean isReadyForConnections();

    void setKillDelay(int millis);

    void addCompletionListenser(Runnable listener);

    File getBaseDir();

    File getDataDir();

    int getPort();
}
