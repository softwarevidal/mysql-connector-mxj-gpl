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
import java.io.PrintWriter;
import java.sql.SQLException;

public class SQLRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String sql;

    private Object[] params;

    public SQLRuntimeException(SQLException cause) {
        this(cause, null, null);
    }

    public SQLRuntimeException(Throwable cause, String sql) {
        this(cause, sql, null);
    }

    public SQLRuntimeException(Throwable cause, String sql, Object[] params) {
        super(cause);
        this.sql = sql;
        this.params = params;
    }

    public SQLRuntimeException(String msg, String sql, Object[] params) {
        super(msg);
        this.sql = sql;
        this.params = params;
    }

    public SQLRuntimeException(String msg, Throwable cause, String sql,
            Object[] params) {
        super(msg, cause);
        this.sql = sql;
        this.params = params;
    }

    public int getErrorCode() {
        for (Throwable t = this; t != null; t = t.getCause()) {
            if (t instanceof SQLException) {
                return ((SQLException) t).getErrorCode();
            }
        }
        return 0;
    }

    public void printStackTrace(PrintStream ps) {
        synchronized (ps) {
            PrintWriter pw = new PrintWriter(ps);
            printState(pw);
            pw.flush();
            super.printStackTrace(ps);
        }
    }

    public void printStackTrace(PrintWriter pw) {
        synchronized (pw) {
            printState(pw);
            super.printStackTrace(pw);
        }
    }

    private void printState(PrintWriter p) {
        p.println("SQL: " + sql);
        if (params != null) {
            if (params instanceof Object[][]) {
                for (int i = 0; i < params.length; i++) {
                    dumpParams(p, ((Object[][]) params)[i]);
                }
            } else {
                dumpParams(p, params);
            }
        }
        p.println("ErrorCode: " + getErrorCode());
    }

    private void dumpParams(PrintWriter p, Object[] paramList) {
        p.print("PARAMS: ");
        for (int i = 0; i < paramList.length; i++) {
            final Object param = paramList[i];

            final String type = (param == null) ? "no type"
                    : shortClassName(param.getClass());

            p.print(" (" + type + "): '" + param + "' ");
        }
        p.println();
    }

    private String shortClassName(Class aClass) {
        String name = aClass.getName();
        int lastDot = name.lastIndexOf('.');
        return name.substring(lastDot + 1);
    }
}
