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

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class InitializeUser {

    private String userName;

    private String password;

    private String url;

    private PrintStream err;

    public InitializeUser(int port, String userName, String password,
            PrintStream err) {
        this.userName = userName;
        this.password = password;
        this.url = "jdbc:mysql://127.0.0.1:" + port + "/mysql";
        this.err = err;

        try {
            Class.forName(com.mysql.jdbc.Driver.class.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /** returns true if the password was set with this attempt */
    public boolean initializeUser() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, userName, password);
            return false;
        } catch (SQLException e) {
            // Okay, current user not initialized;
        } finally {
            close(conn);
        }

        try {
            final String NO_PASSWORD = null;
            conn = DriverManager.getConnection(url, "root", NO_PASSWORD);
        } catch (SQLException e) {
            String msg = "User initialization error." //
                    + " Can not connect as " + userName + " with password." //
                    + " Can not connect as root without password." //
                    + " URL: " + url;
            throw new SQLRuntimeException(msg, e, null, null);
        }
        try {
            QueryUtil util = new QueryUtil(conn, err);
            // util.execute("drop user ''");
            // util.execute("drop user 'root'@'localhost'");
            // util.execute("drop user 'root'@'127.0.0.1'");
            util.execute("DELETE from user");
            String sql = "grant all on *.* to ?@'localhost' identified by ? with grant option";
            final Object[] params = new Object[] { userName, password };
            util.execute(sql, params);
            util.execute("flush privileges");
        } finally {
            close(conn);
        }

        try {
            conn = DriverManager.getConnection(url, userName, password);
            QueryUtil util = new QueryUtil(conn, err);
            util.execute("SELECT 1");
        } catch (SQLException e) {
            String msg = "User initialization error." //
                    + " Can not connect as " + userName + " with password" //
                    + " after creating user and password." //
                    + " URL: " + url;
            throw new SQLRuntimeException(msg, e, null, null);
        } finally {
            close(conn);
        }
        return true;
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Throwable t) {
                t.printStackTrace(err);
            }
        }
    }
}
