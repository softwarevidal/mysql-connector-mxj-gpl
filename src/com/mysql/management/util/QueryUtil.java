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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryUtil {

    private final Connection conn;

    private PrintStream err;

    public QueryUtil(Connection conn) {
        this(conn, System.err);
    }

    public QueryUtil(Connection conn, PrintStream err) {
        this.conn = conn;
        this.err = err;
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[] containing the variables for the query
     * 
     * @return a java.util.List of Maps. Each Row of the query result set is
     *         represented by a map in the list. Each map in the list has an
     *         Map.Entry of column name to column value.
     */
    public List executeQuery(String query, Object[] params) {
        PreparedStatement pStmt = null;
        ResultSet rs = null;
        try {
            pStmt = prepareStatement(query, params);
            rs = pStmt.executeQuery();
            return mapRows(rs);
        } catch (SQLException sqlEx) {
            throw new SQLRuntimeException(sqlEx, query, params);
        } finally {
            cleanupRsAndStmt(pStmt, rs, query, null);
        }
    }

    private List mapRows(ResultSet rs) throws SQLException {
        List rows;
        int columns = rs.getMetaData().getColumnCount();
        rows = new ArrayList();
        while (rs.next()) {
            Map rowVals = new LinkedHashMap(columns);
            for (int i = 1; i <= columns; i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                Object columValue = rs.getObject(i);
                rowVals.put(columnName, columValue);
            }
            rows.add(rowVals);
        }
        return rows;
    }

    /**
     * @param query
     *                the SQL Query to execute
     * 
     * @return a java.util.List of Maps. Each Row of the query result set is
     *         represented by a map in the list. Each map in the list has an
     *         Map.Entry of column name to column value.
     */
    public List executeQuery(String query) {
        return executeQuery(query, null);
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[] containing the parameters for the query
     * 
     * @return the row count (or 0 for SQL statements which return nothing)
     */
    public int executeUpdate(String query, Object[] params) {
        PreparedStatement pStmt = null;
        try {
            pStmt = prepareStatement(query, params);
            return pStmt.executeUpdate();
        } catch (SQLException sqlEx) {
            throw new SQLRuntimeException(sqlEx, query, params);
        } finally {
            cleanupRsAndStmt(pStmt, null, query, params);
        }
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[] containing the parameters for the query
     * 
     * @return true if the next result is a ResultSet; false if it is an update
     *         count or there are no more results
     */
    public boolean execute(String query, Object[] params) {
        PreparedStatement pstmt = null;
        try {
            pstmt = prepareStatement(query, params);
            return pstmt.execute();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e, query, params);
        } finally {
            cleanupRsAndStmt(pstmt, null, query, null);
        }
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[][] containing the parameters for the query for
     *                each item in the batch
     * 
     * @return an array of update counts containing one element for each command
     *         in the batch. The array is ordered according to the order in
     *         which commands were inserted into the batch
     */
    public int[] executeBatch(String query, Object[][] params) {
        PreparedStatement pStmt = null;
        ResultSet resultSet = null;
        try {
            pStmt = prepareStatement(query, false);
            for (int i = 0; i < params.length; i++) {
                setParameters(query, params[i], pStmt);
                pStmt.addBatch();
            }
            return pStmt.executeBatch();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e, query, params);
        } finally {
            cleanupRsAndStmt(pStmt, resultSet, query, params);
        }
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[] containing the parameters for the query
     * 
     * @return either a row count, or 0 for SQL commands
     */
    public int executeUpdate(String query) {
        return executeUpdate(query, null);
    }

    /**
     * @param query
     *                the SQL Query to execute
     * 
     * @return true if the next result is a ResulSet, false if it is an update
     *         count or there are no more results
     */
    public boolean execute(String query) {
        return execute(query, null);
    }

    /**
     * @param query
     *                the SQL Query to execute
     * 
     * @return a java.util.List of Maps. Each Row of the query result set is
     *         represented by a map in the list. Each map in the list has an
     *         Map.Entry of column name (GENERATED_KEY) to column value (the key
     *         generated).
     */
    public List executeUpdateReturningKeys(String query) {
        return executeUpdateReturningKeys(query, null);
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[] containing the variables for the query
     * 
     * @return a java.util.List of Maps. Each Row of the query result set is
     *         represented by a map in the list. Each map in the list has an
     *         Map.Entry of column name (GENERATED_KEY) to column value (the key
     *         generated).
     */
    public List executeUpdateReturningKeys(String query, Object[] params) {
        PreparedStatement pStmt = null;
        ResultSet resultSet = null;
        try {
            pStmt = prepareStatement(query, params, true);
            pStmt.executeUpdate();
            resultSet = pStmt.getGeneratedKeys();
            return mapRows(resultSet);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e, query, params);
        } finally {
            cleanupRsAndStmt(pStmt, resultSet, query, params);
        }
    }

    private PreparedStatement prepareStatement(String query, Object[] params) throws SQLException {
        return prepareStatement(query, params, false);
    }    
    
    private PreparedStatement prepareStatement(String query, Object[] params,
            boolean returnKeys) throws SQLException {
        PreparedStatement pStmt = prepareStatement(query, returnKeys);
        setParameters(query, params, pStmt);
        return pStmt;
    }

    private void setParameters(String query, Object[] params,
            PreparedStatement pStmt) throws SQLException {
        int numParams = pStmt.getParameterMetaData().getParameterCount();
        int paramCount = (params == null) ? 0 : params.length;

        if (numParams != paramCount) {
            final String msg = "Expected " + numParams + " parameters, got "
                    + paramCount;
            throw new SQLRuntimeException(msg, query, params);
        }

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                pStmt.setObject(i + 1, param);
            }
        }
    }

    private PreparedStatement prepareStatement(String query, boolean returnKeys)
            throws SQLException {
        if (query == null) {
            throw new IllegalArgumentException("query string may not be null");
        }
        if (query.length() == 0) {
            throw new IllegalArgumentException("query string may not be empty");
        }
        if (returnKeys) {
            return conn.prepareStatement(query,
                    Statement.RETURN_GENERATED_KEYS);
        }
        return conn.prepareStatement(query);
    }

    private void cleanupRsAndStmt(Statement stmt, ResultSet rs, String origSql,
            Object[] params) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Throwable t) {
                new SQLRuntimeException(t, origSql, params)
                        .printStackTrace(err);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Throwable t) {
                new SQLRuntimeException(t, origSql, params)
                        .printStackTrace(err);
            }
        }
    }

    /**
     * @param query
     *                the SQL Query to execute
     * 
     * @return first column value of the first row returned, or null
     */
    public String queryForString(String query) {
        return queryForString(query, null);
    }

    /**
     * @param query
     *                the SQL Query to execute
     * @param params
     *                the Object[] containing the parameters for the query
     * 
     * @return either null or the first row's first column value.toString()
     */
    public String queryForString(String query, Object[] params) {
        List restults = executeQuery(query, params);
        for (Iterator it = restults.iterator(); it.hasNext();) {
            Map row = (Map) it.next();
            for (Iterator it2 = row.entrySet().iterator(); it2.hasNext();) {
                Map.Entry column = (Map.Entry) it2.next();
                if (column.getValue() == null) {
                    return null;
                }
                return column.getValue().toString();
            }
        }
        return null;
    }
}
