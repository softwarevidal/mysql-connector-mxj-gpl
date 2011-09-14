<%@ page info="simple example" %>
<%@ page import="java.sql.*" %>
<html>
<head><title>Simple Example</title></head>
<body bgcolor="#ffffff">

<%
 String driver = System.getProperty("SQL_DRIVER", "com.mysql.jdbc.Driver");
 String defaultUrl = "jdbc:mysql:///test?propertiesTransform=com.mysql.management.jmx.ConnectorMXJPropertiesTransform";
 String url = System.getProperty("SQL_URL", defaultUrl);
 String user = System.getProperty("SQL_USER", "root");
 String password = System.getProperty("SQL_PW", "");

 Class.forName(driver);

 Connection conn =  DriverManager.getConnection(url, user, password);
%>

<b> Connection Properties </b><br>

<table>
  <tr><td align='right'>driver:</td><td>&nbsp;<%=driver%></td></tr>
  <tr><td align='right'>url:</td><td>&nbsp;<%=url%></td></tr>
  <tr><td align='right'>user:</td><td>&nbsp;<%=user%></td></tr>
  <tr><td align='right'>password:</td><td>&nbsp;<%=password%></td></tr>
</table>
<br>
<%
 StringBuffer result = new StringBuffer();
 String selectSQL = "SELECT 1";
 Statement stmt = conn.createStatement();
 ResultSet rs = stmt.executeQuery(selectSQL);
 while (rs.next()) {
     result.append(rs.getString(1));
 }
 conn.close();
%>
<br>
<b>The SQL query '<%=selectSQL%>' resulted in '<%=result.toString()%>'</b>
</body>
</html>