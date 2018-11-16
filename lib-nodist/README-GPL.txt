MySQL Connector/MXJ 5.0

This is a release of MySQL Connector/MXJ, Oracle's dual-license 
MBean for embedding the MySQL Server in Java applications. For 
the avoidance of doubt, this particular copy of the software is
released under the version 2 of the GNU General Public License. 
MySQL Connector/MXJ is brought to you by the MySQL team at Oracle.

Copyright (c) 2004, 2010, Oracle and/or its affiliates. All rights reserved.

License information can be found in the COPYING file.

This program is free software; you can redistribute it and/or 
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; version 2 of the 
License.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
GNU General Public License for more details.

You should have received a copy of the GNU General Public License 
along with this program; if not, write to the Free Software 
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 
02110-1301  USA.

***************************************************************

Third-Party Component Notices

****************************************************************

This distribution may include materials developed by third
parties. For license and attribution notices for these
materials, please refer to the documentation that accompanies
this distribution.

GPLv2 Disclaimer
For the avoidance of doubt, except that if any license choice
other than GPL or LGPL is available it will apply instead, 
Oracle elects to use only the General Public License version 2 
(GPLv2) at this time for any software where a choice of GPL 
license versions is made available with the language indicating 
that GPLv2 or any later version may be used, or where a choice 
of which version of the GPL is applied is otherwise unspecified.

CONTENTS

* Description
* Documentation
* Limitations
* Getting Started

DESCRIPTION

The MySQL Connector/MXJ package is the "MySQL Cross-platform 
Jar"; in reality, a Java Wrapper around the MySQL Database 
Server.

MySQL may be instantiated as a POJO (Plain Old Java Object), 
as a JMX MBean, or simply by modifying your MySQL Connector/J 
JDBC connection string.

It is MySQL Connector/MXJ's job to determine what platform 
is in use and to launch the correct version of MySQL for that 
platform. Obviously, MySQL does not run on every platform for 
which a Java Runtime Environment exists. Within those limits, 
the goal of being able to deploy, manage and use MySQL simply 
and easily for Java developers is still quite obtainable. 

DOCUMENTATION

Documentation can be found at: 
http://dev.mysql.com/doc/refman/5.1/en/connector-mxj.html

LIMITATIONS

This release only supports the following platforms: 
 * Linux-i386
 * SunOS-sparc,x86
 * Mac OS X 10.3 ppc, 10.4 x86/ppc, 10.5 x86/ppc
 * WindowsXP/2000/NT/Vista x86

We have included resources for Linux, Windows, OS X, and Sun 
platforms, but have only done significant testing on Linux.

When using jdbc:mysql:mxj:// urls, MySQL Connector/J and MySQL
Connector/MXJ must be loaded by the same class-loader. This 
can be an issue if, for instance, the Connector/J jar file is 
in $APPSERVER/endorsed and the Connector/MXJ jars are in the 
local $WEB-APP/lib.

GETTING STARTED

Thank you for your interest.

Included in the src/ are two sample usages:

 src/ConnectorMXJUrlTestExample.java

and:

 src/ConnectorMXJObjectTestExample.java

Both of the included examples have the same idea of 
* deploying a new mysqld installation, 
* starting mysqld, 
* issuing a query,
* and finally shutting mysqld down.

By looking at the URL Example, you can see that an application 
would have to make a couple of changes, which will look a little 
like this:

(0) Add the MySQL Connector/MXJ jars (or classes) to the $CLASSPATH

(1) Alter your jdbc connection string from something like this typical
    MySQL Connector/J JDBC connection string:

      String url = "jdbc:mysql://localhost:3306/somedb"
                 + "?createDatabaseIfNotExist=true";

  To something more like this:

      String url = "jdbc:mysql:mxj://localhost:13306/somedb"
                 + "?createDatabaseIfNotExist=true"
                 + "&server.basedir=/path/to/deploy/mysql";

The first connection to the database will deploy and start mysqld.
Additional connections will recognize that a database already is
running.

Notice two key things about the URL above:
 (A) the "mxj:" which tells MySQL Connector/J to use Connector/MXJ 
and
 (B) the "server." prefix to an parameter. All "server." parameters
will be used by Connector/MXJ. In this way, anything that can normally
be specified to configure a mysqld can be configured via the URL with
this prefix.

For example, your app could have a very detailed configuration, with
many Connector/J parameters as well as many mysqld parameters:

    String url = "jdbc:mysql:mxj://127.0.0.1:13306/dbName"
            + "?createDatabaseIfNotExist=true"
            + "&elideSetAutoCommits=true"
            + "&useServerPrepStmts=false"
            + "&cachePrepStmts=true"
            + "&cacheCallableStmts=true"
            + "&noAccessToProcedureBodies=true"
            + "&characterEncoding=UTF-8"
            + "&includeInnodbStatusInDeadlockExceptions=true"
            + "&cacheServerConfiguration=true"
            + "&useDynamicCharsetInfo=false"
            + "&server.initialize-user=true"
            + "&server.basedir=/path/to/deploy/mysql"
            + "&server.datadir=/path/to/var/mysql/data"
            + "&server.innodb_locks_unsafe_for_binlog=1"
            + "&server.innodb_buffer_pool_size=128M"
            + "&server.innodb_log_file_size=32M"
            + "&server.innodb_flush_log_at_trx_commit=2"
            + "&server.innodb_support_xa=0"
            + "&server.skip-locking"
            + "&server.key_buffer=32M"
            + "&server.max_allowed_packet=64M"
            + "&server.table_cache=64"
            + "&server.sort_buffer_size=512K"
            + "&server.net_buffer_length=16K"
            + "&server.read_buffer_size=256K"
            + "&server.read_rnd_buffer_size=512K"
            + "&server.myisam_sort_buffer_size=16M";

As you can imagine, there is quite a bit of tuning available, if needed.

More information on MySQL Connector/J configuration can be found at:
http://dev.mysql.com/doc/refman/5.1/en/connector-j-reference-configuration-properties.html

More information on mysqld command-line options can be found at:
http://dev.mysql.com/doc/refman/5.1/en/mysqld-option-tables.html

(2) Finally, when it is time to shutdown the database,

      File dbDir = new File("/path/to/deploy/mysql");
      File dataDir = null; /* defaults to dbDir's "data" subdir */

      ServerLauncherSocketFactory.shutdown(dbDir, dataDir);

As an alternative to the URL syntax, the Object Test Example shows how
an instance of the MysqldResource may be instantiated directly as a POJO (Plain Old Java Object):

      MysqldResource mysqldResource = new MysqldResource(databaseDir);
      Map databaseOptions = new HashMap();
      databaseOptions.put("port", "13306");
      mysqldResource.start("thread-name", databaseOptions)

And finally:

      mysqldResource.shutdown();

The databaseOptions map may contain any option that mysqld can be
configured with, just like the urls above. For instance:
      databaseOptions.put("max_allowed_packet", "64M");

As you can see, MySQL Connector/MXJ may be invoked via JDBC connection
parameters, and directly as a POJO.