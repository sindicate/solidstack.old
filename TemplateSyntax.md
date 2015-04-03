# Query directive #

The query directive can be used to import classes. Keep in mind that the template uses Groovy as the script language. Groovy imports more packages by default than Java does.

The following packages and classes are imported by default:

  * java.io.`*`
  * java.lang.`*`
  * java.math.BigDecimal
  * java.math.BigInteger
  * java.net.`*`
  * java.util.`*`
  * groovy.lang.`*`
  * groovy.util.`*`

Example1:

```
<%@ query import="packagename.ClassName" %>
```

Example2:

```
<%@ query import="packagename1.*" import="packagename2.ClassName" %>
```

# Scriptlet #

Scriptlets can be used to add groovy code to your template.

Syntax:

```
<% groovy code %>
```

Example:

```
<%
def today = new java.sql.Date( System.currentTimeMillis() );
%>
SELECT NAME, AGE
FROM EMPLOYEES
WHERE TO_CHAR( BIRTHDAY, 'MMDD' ) = TO_CHAR( ${today}, 'MMDD' )
<% if( gender ) { %>
AND GENDER = ${gender}
<% } %>
```

Beware that because of a Groovy syntax bug the following needs a semicolon (;) where Java does not:

```
<% if( gender ) { %>
AND GENDER = ${gender}
<% }; if( age ) { %>
AND AGE = ${age}
<% } %>
```

# Expression #

Expressions can be used to add text to the SQL.

Syntax:

```
<%= groovy expression %>
```

Example:

```
<%@ query import="app.DBUtils" %>
SELECT NAME, AGE
FROM EMPLOYEES
WHERE ACTIVE = <%= DBUtils.TRUE %>
```

# Bind parameter #

A bind parameter is added to the SQL as a question mark (?). Question marks in SQL represent bind parameters that are sent to the database separate from the SQL.

Bind parameters are useful for performance reasons and for preventing SQL injection attacks. But they should not be overused. It is better not to use bind parameters for the following:

  * Oracle's "ROWNUM < x". Oracle adapts its query execution plan depending on the number of rows you want. If it is a bind parameter, Oracle can't use the value to adapt its execution plan;
  * LIKE 'prefix%';
  * Constants.

Syntax:

```
${groovy expression}
```

Example 1:

```
SELECT NAME, AGE
FROM EMPLOYEES
WHERE AGE = ${age}
```

Example 2, IN can also be used. The variable 'departments' is a Collection or an array.

```
SELECT NAME, AGE
FROM EMPLOYEES
WHERE DEPARTMENT IN (${departments})
```

Oracle limits the size of the list to 1000 elements. When you need more elements you could do the following, assuming the variable 'departments' is of type List:

```
SELECT NAME, AGE
FROM EMPLOYEES
WHERE DEPARTMENT IN ( ${ departments.subList( 0, Math.min( 1000, departments.size() ) ) } )
<% for( def i = 1000; i < departments.size(); i += 1000 ) { %>
OR DEPARTMENT IN ( ${ departments.subList( i, Math.min( i + 1000, departments.size() ) ) } )
<% } %>
```

# Comment #

Adds a comment to the template.

Syntax:

```
<%-- comment --%>
```