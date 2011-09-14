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

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mysql.management.util.QueryUtil;

public class TestDb {
    private String driver;

    private String url;

    private String user;

    private String password;

    public TestDb(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection connection() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void printResults(String query, PrintStream out,
            boolean printMetaData) {
        executeSql(query, out, false, printMetaData);
    }

    public void printUpdateResults(String query, PrintStream out,
            boolean printMetaData) {
        executeSql(query, out, true, printMetaData);
    }

    void executeSql(String query, PrintStream out, boolean update,
            boolean printMetaData) {
        display(query, out);
        Connection conn = connection();
        try {
            if (update) {
                out.println(new QueryUtil(conn).executeUpdate(query));
            } else {
                List results = new QueryUtil(conn).executeQuery(query);
                printResults(out, results, printMetaData);
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printResults(PrintStream out, List results,
            boolean printMetaData) {
        for (Iterator it = results.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            for (Iterator it2 = row.entrySet().iterator(); it2.hasNext();) {
                final Map.Entry entry = (Map.Entry) it2.next();
                if (printMetaData) {
                    out.print(entry.getKey() + ": ");
                }
                out.println(entry.getValue());
            }
            if (it.hasNext()) {
                out.println();
            }
        }
    }

    private void display(String query, PrintStream out) {
        out.println("driver: " + driver);
        out.println("url: " + url);
        out.println("user: " + user);
        out.println("password: " + password);
        out.println("query: " + query);
        out.println();
    }

    public static void main(String[] args) throws Exception {
        String driver = (args.length > 0) ? args[0] : "com.mysql.jdbc.Driver";
        String url = (args.length > 1) ? args[1] : "jdbc:mysql:///test";
        String user = (args.length > 2) ? args[2] : "root";
        String password = (args.length > 3) ? args[3] : "";
        String query = (args.length > 4) ? args[4] : "SELECT VERSION()";
        boolean printMetaData = (args.length > 5) ? Boolean.valueOf(args[5])
                .booleanValue() : false;

        TestDb testDb = new TestDb(driver, url, user, password);
        testDb.printResults(query, System.out, printMetaData);
    }
}
