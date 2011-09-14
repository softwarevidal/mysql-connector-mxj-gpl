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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.management.util.Files;
import com.mysql.management.util.ListToString;

/**
 * @author Eric Herman <eric@mysql.com>
 * @version $Id: MysqldResourceTestImpl.java,v 1.4 2005/04/13 19:53:24 eherman
 *          Exp $
 */
public class MysqldResourceTestImpl implements MysqldResourceI {
    public File baseDir;

    public File dataDir;

    public Map runningFakeMysqlds;

    private String running;

    private Map currentOptions;

    private List completionListensers;

    private String version;

    public MysqldResourceTestImpl(Map runningFakeMysqlds) {
        this(null, null, new HashMap(), runningFakeMysqlds);
    }

    public MysqldResourceTestImpl(File baseDir, File dataDir,
            Map runningFakeMysqlds) {
        this(baseDir, dataDir, new HashMap(), runningFakeMysqlds);
    }

    public MysqldResourceTestImpl(File baseDir, File dataDir, Map options,
            Map runningFakeMysqlds) {
        this.baseDir = (baseDir == null) ? new Files().nullFile() : baseDir;
        this.dataDir = dataDir;
        this.running = null;
        this.currentOptions = options;
        this.completionListensers = new ArrayList();
        this.runningFakeMysqlds = runningFakeMysqlds;
        this.version = "5.2.23";
    }

    public void start(String threadName, Map mysqldArgs) {
        start(threadName, mysqldArgs, false);
    }

    public void start(String threadName, Map mysqldArgs,
            boolean populateAllOptions) {
        if (running != null) {
            String msg = "already running " + running + " mysqldArgs: "
                    + new ListToString().toString(mysqldArgs);
            throw new RuntimeException(msg);
        }
        currentOptions.putAll(mysqldArgs);
        running = threadName;
        runningFakeMysqlds.put(baseDir, this);
    }

    public void shutdown() {
        runningFakeMysqlds.remove(baseDir);
        running = null;
        for (int i = 0; i < completionListensers.size(); i++) {
            Runnable listener = (Runnable) completionListensers.get(i);
            listener.run();
        }
    }

    public Map getServerOptions() {
        return new HashMap(currentOptions);
    }

    public boolean isRunning() {
        return running != null;
    }

    public String getVersion() {
        return version;
    }

    public void addCompletionListenser(Runnable listener) {
        completionListensers.add(listener);
    }

    public void setVersion(String version) {
        if (version == null) {
            this.version = "5.2.23";
        }
        this.version = version;
    }

    public void setKillDelay(int millis) {
        throw new RuntimeException("Millis: " + millis);
    }

    public boolean isReadyForConnections() {
        return isRunning();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getDataDir() {
        return dataDir;
    }

    public int getPort() {
        return 0;
    }
}
