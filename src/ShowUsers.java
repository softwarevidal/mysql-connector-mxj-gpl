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

import java.io.File;
import java.net.URLEncoder;

import com.mysql.management.driverlaunched.ServerLauncherSocketFactory;

public class ShowUsers {
    public static void main(String[] args) throws Exception {
        File ourAppDir = new File(System.getProperty("java.io.tmpdir"));
        File databaseDir = new File(ourAppDir, "test-show-users-mxj");
        String databasePath = URLEncoder.encode(databaseDir.getPath(), "UTF-8");
        int port = 3336;

        String url = "jdbc:mysql:mxj://localhost:" + port + "/mysql" //
                + "?" + "server.basedir=" + databasePath;

        String driver = "com.mysql.jdbc.Driver";
        String user = (args.length > 0) ? args[0] : "root";
        String password = (args.length > 1) ? args[1] : "";

        try {
            boolean printMetaData = false;
            TestDb testDb = new TestDb(driver, url, user, password);
            testDb.printResults("SElECT version()", System.out, printMetaData);
            printMetaData = true;
            // testDb.printUpdateResults(
            // "DELETE from user where host like 'hal%'", System.out,
            // printMetaData);
            testDb.printResults("SElECT host, user, password from user",
                    System.out, printMetaData);
        } finally {
            // databaseDir.deleteOnExit();
            ServerLauncherSocketFactory.shutdown(databaseDir, null);
        }
    }
}
