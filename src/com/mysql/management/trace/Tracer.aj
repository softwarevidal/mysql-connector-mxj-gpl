/*
      Copyright (C) 2005-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. All rights reserved.
      Use is subject to license terms.

      This program is free software; you can redistribute it and/or modify
      it under the terms of version 2 of the GNU General Public License as 
      published by the Free Software Foundation.

      There are special exceptions to the terms and conditions of the GPL 
      as it is applied to this software. View the full text of the 
      exception in file EXCEPTIONS-CONNECTOR-J in the directory of this 
      software distribution.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.mysql.management.trace;

import java.io.PrintStream;
import java.sql.SQLException;

import com.mysql.jdbc.log.LogUtils;

import org.aspectj.lang.JoinPoint;

public aspect Tracer {

    public static final String TRACE_PROPERTY = "c-mxj_trace";

    private static boolean skipLogging = !Boolean.valueOf(System.getProperty(
                      TRACE_PROPERTY, Boolean.FALSE.toString())).booleanValue();
                      
    private static PrintStream logErr = System.err;
    
    private static void setErrStream(PrintStream printStream) {
        logErr = printStream;
    }

    public static void setLogging(boolean b) {
    	skipLogging = !b;
    }

    // ---------------------------------------------------------------------
    
    pointcut constructors(): execution(* *(..)) 
        && within(com.mysql.management.* ) 
    	&& within(!com.mysql.management.trace.*);
  
    pointcut methods(): execution(* *(..)) && within(com.mysql.management.* ) 
    	&& within(!com.mysql.management.trace.*);

    before(): constructors() && methods() {
		entry(thisJoinPoint, false);
    }
    
    after() returning (Object o): constructors() && methods() {
	  exit(thisJoinPoint, false, o);
    }
	
    private ThreadLocal callDepth = new ThreadLocal() {
	    protected Object initialValue() {
			return new Integer(0);
	    }
	};
    
    private int  getCallDepth() { 
		return ((Integer)(callDepth.get())).intValue();
    }
    
    private void setCallDepth(int n) { 
		callDepth.set(new Integer(n)); 
    }

    private void entry(JoinPoint jp, boolean isConstructor) {
		setCallDepth(getCallDepth() + 1);
		printEntering(jp, isConstructor);
    }

    private void exit(JoinPoint jp,  boolean isConstructor,
                                                          Object returnValue) {
		printExiting(jp, isConstructor, returnValue);
		setCallDepth(getCallDepth() - 1);
    }

    private void printEntering (JoinPoint jp, boolean isConstructor) {
        if( skipLogging ){
            return;
        }
    	StringBuffer buf = new StringBuffer(80);
    	printCalledFromIfNotMySQL(buf);
		printIndent(buf);
		buf.append("--> ");

		buf.append(jp.getSourceLocation().getFileName());
		buf.append(":");
		buf.append(jp.getSourceLocation().getLine());
		buf.append(" ");
		buf.append(jp.getSignature().getDeclaringTypeName());
		buf.append(".");
		buf.append(jp.getSignature().getName());
    	printParameters(jp, buf);

		logErr.println(buf.toString());
    }

    private void printExiting (JoinPoint jp, boolean isConstructor,
                                                          Object returnValue) {
        if( skipLogging ){
            return;
        }
    	StringBuffer buf = new StringBuffer(80);
		printIndent(buf);
			
		buf.append("<--  ");
		buf.append(jp.getSourceLocation().getFileName());
		buf.append(":");
		buf.append(jp.getSourceLocation().getLine());
		buf.append(" ");
		buf.append(jp.getSignature().getDeclaringTypeName());
		buf.append(".");
		buf.append(jp.getSignature().getName());
		buf.append("(..) returning ");
			
		boolean isString = returnValue instanceof String;
  			
  		if (isString) {
  			buf.append("\"");
  		}
  	    	
  	    buf.append(returnValue);
  	    	
  	    if (isString) {
  			buf.append("\"");
  		}

		logErr.println(buf.toString());
    }



    private void printIndent(StringBuffer buf) {
    	buf.append("[mxj-trace]");
		for (int i = 0; i < getCallDepth(); i++) {
	    	buf.append(" ");
	    }
    }

    private void printCalledFromIfNotMySQL(StringBuffer buf) {
        String callingClassAndMethod = 
                            LogUtils.findCallingClassAndMethod(new Throwable());
                            
        if (callingClassAndMethod.startsWith("com.mysql.managment")) {
            return;
        }
        if (callingClassAndMethod
                     .indexOf(LogUtils.CALLER_INFORMATION_NOT_AVAILABLE) >= 0) {
            return;
        }
        
		printIndent(buf);
		buf.append("Caller ");
		buf.append(callingClassAndMethod);
		buf.append(System.getProperty("line.separator"));
	}
	    
    private void printParameters(JoinPoint jp, StringBuffer buf) {
  		Object[] params = jp.getArgs();
	 	
  		buf.append("(");
  		
  		for (int i = 0; i < params.length; i++) {
  			boolean isString = params[i] instanceof String;
  			
  			if (isString) {
  				buf.append("\"");
  			}
  	    	
  	    	if (params[i] != null) {
  	    		buf.append(params[i]);
  	    	} else {
  	    		buf.append("null");
  	    	}
  	    	
  	    	if (isString) {
  				buf.append("\"");
  			}
  			
  	    	if (i < params.length - 1) {
  	    		buf.append(", ");
  	    	}
  		}
  		
  		buf.append(")");
    }

}

