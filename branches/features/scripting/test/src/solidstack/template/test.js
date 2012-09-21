// Test if the import at the bottom works, and this comment too of course
out.write("SELECT *\n\
");
out.write("FROM SYS.SYSTABLES\n\
");



out.write("WHERE 1 = 1\n\
"); if( prefix ) { 
;out.write("AND TABLENAME LIKE '");out.write( prefix );out.write("%'\n\
"); } 
; if( name ) { 
;out.write("AND TABLENAME = ");out.writeEncoded(name);out.write("\n\
"); } 
; if( names ) { 
;out.write("AND TABLENAME IN (");out.writeEncoded(names);out.write(")\n\
"); } 
;

out.write( new java.util.Date() )
out.write( "\n" )

importClass( Packages.solidstack.template.JavascriptTest )
out.write( JavascriptTest.CONSTANT )
out.write( "\n" )
