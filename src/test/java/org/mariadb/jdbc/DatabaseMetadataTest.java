package org.mariadb.jdbc;

import org.junit.Test;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DatabaseMetadataTest extends BaseTest{
    static { Logger.getLogger("").setLevel(Level.OFF); }

    @Test
    public void primaryKeysTest() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("drop table if exists pk_test");
        stmt.execute("create table pk_test (id1 int not null, id2 int not null, val varchar(20), primary key(id1, id2)) engine=innodb");
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = dbmd.getPrimaryKeys("test",null,"pk_test");
        int i=0;
        while(rs.next()) {
            i++;
            assertEquals("test",rs.getString("table_cat"));
            assertEquals(null,rs.getString("table_schem"));
            assertEquals("pk_test",rs.getString("table_name"));
            assertEquals("id"+i,rs.getString("column_name"));
            assertEquals(i,rs.getShort("key_seq"));
        }
        assertEquals(2,i);
    }

    @Test
    public void datetimeTest() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("drop table if exists datetime_test");
        stmt.execute("create table datetime_test (dt datetime)");
        ResultSet rs = stmt.executeQuery("select * from datetime_test");
        assertEquals(93,rs.getMetaData().getColumnType(1));

    }

    @Test
    public void exportedKeysTest() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("drop table if exists fore_key0");
        stmt.execute("drop table if exists fore_key1");
        stmt.execute("drop table if exists prim_key");


        stmt.execute("create table prim_key (id int not null primary key, " +
                                            "val varchar(20)) engine=innodb");
        stmt.execute("create table fore_key0 (id int not null primary key, " +
                                            "id_ref0 int, foreign key (id_ref0) references prim_key(id)) engine=innodb");
        stmt.execute("create table fore_key1 (id int not null primary key, " +
                                            "id_ref1 int, foreign key (id_ref1) references prim_key(id) on update cascade) engine=innodb");


        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = dbmd.getExportedKeys("test",null,"prim_key");
        int i =0 ;
        while(rs.next()) {
            assertEquals("id",rs.getString("pkcolumn_name"));
            assertEquals("fore_key"+i,rs.getString("fktable_name"));
            assertEquals("id_ref"+i,rs.getString("fkcolumn_name"));
            i++;

        }
        assertEquals(2,i);
    }
    @Test
    public void importedKeysTest() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("drop table if exists fore_key0");
        stmt.execute("drop table if exists fore_key1");
        stmt.execute("drop table if exists prim_key");

        stmt.execute("create table prim_key (id int not null primary key, " +
                                            "val varchar(20)) engine=innodb");
        stmt.execute("create table fore_key0 (id int not null primary key, " +
                                            "id_ref0 int, foreign key (id_ref0) references prim_key(id)) engine=innodb");
        stmt.execute("create table fore_key1 (id int not null primary key, " +
                                            "id_ref1 int, foreign key (id_ref1) references prim_key(id) on update cascade) engine=innodb");

        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = dbmd.getImportedKeys(connection.getCatalog(),null,"fore_key0");
        int i = 0;
        while(rs.next()) {
            assertEquals("id",rs.getString("pkcolumn_name"));
            assertEquals("prim_key",rs.getString("pktable_name"));
            i++;
        }
        assertEquals(1,i);
    }
    @Test
    public void testGetCatalogs() throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        
        ResultSet rs = dbmd.getCatalogs();
        boolean haveMysql = false;
        boolean haveInformationSchema = false;
        while(rs.next()) {
        	String cat = rs.getString(1);
        	
        	if (cat.equalsIgnoreCase("mysql"))
        		haveMysql = true;
        	else if (cat.equalsIgnoreCase("information_schema"))
        		haveInformationSchema = true;
        }
        assertTrue(haveMysql);
        assertTrue(haveInformationSchema);
    }
    
    @Test
    public void testGetTables() throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = dbmd.getTables(null,null,"prim_key",null);
        assertEquals(true,rs.next());
        rs = dbmd.getTables("", null,"prim_key",null);
        assertEquals(true,rs.next());
    }
    @Test
    public void testGetTables2() throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = 
        		dbmd.getTables("information_schema",null,"TABLE_PRIVILEGES",new String[]{"SYSTEM VIEW"});
        assertEquals(true, rs.next());
        assertEquals(false, rs.next());
        rs = dbmd.getTables(null,null,"TABLE_PRIVILEGES",new String[]{"TABLE"});
        assertEquals(false, rs.next());

    }
    @Test
    public void testGetColumns() throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = dbmd.getColumns(null,null,"t1",null);
        while(rs.next()){
            System.out.println(rs.getString(3));
        }
    }
    
    void testResultSetColumnNames(ResultSet rs, String spec) throws SQLException {
   	 	ResultSetMetaData rsmd = rs.getMetaData();
   	 	String[] tokens   = spec.split(",");
   	 	
   	 	for(int i = 0; i < tokens.length; i++) {
   	 		String[] a = tokens[i].trim().split(" ");
   	 		String label = a[0];
   	 		String type = a[1];
   	 		
   	 		int col = i +1;
   	 		assertEquals(label,rsmd.getColumnLabel(col));
   	 		int t = rsmd.getColumnType(col);
   	 		if (type.equals("String")) {
   	 			assertTrue("invalid type  " + t + " for " + rsmd.getColumnLabel(col) + ",expected String",
   	 					t == java.sql.Types.VARCHAR || t == java.sql.Types.NULL );
   	 		} else if (type.equals("int") || type.equals("short")) {
   	 			
   	 			assertTrue("invalid type  " + t + " for " + rsmd.getColumnLabel(col) + ",expected numeric",
   	 					t == java.sql.Types.BIGINT || t == java.sql.Types.INTEGER ||
   	 					t == java.sql.Types.SMALLINT || t == java.sql.Types.TINYINT);
   	 			
   	 		} else if (type.equals("null")){
   	 			assertTrue("invalid type  " + t + " for " + rsmd.getColumnLabel(col) + ",expected null",
   	 					t == java.sql.Types.NULL);	
   	 		} else {
   	 			assertTrue("invalid type '"+ type + "'", false);
   	 		}
   	 	}
    }
    
    @Test 
    public void getAttributesBasic()throws Exception {
    	 testResultSetColumnNames(
    		 connection.getMetaData().getAttributes(null, null, null, null),
			 "TYPE_CAT String,TYPE_SCHEM String,TYPE_NAME String," 
			 +"ATTR_NAME String,DATA_TYPE int,ATTR_TYPE_NAME String,ATTR_SIZE int,DECIMAL_DIGITS int," 
			 +"NUM_PREC_RADIX int,NULLABLE int,REMARKS String,ATTR_DEF String,SQL_DATA_TYPE int,SQL_DATETIME_SUB int," 
			 +"CHAR_OCTET_LENGTH int,ORDINAL_POSITION int,IS_NULLABLE String,SCOPE_CATALOG String,SCOPE_SCHEMA String,"
			 +"SCOPE_TABLE String,SOURCE_DATA_TYPE short");
    }
    
    
    @Test
    public void getBestRowIdentifierBasic()throws SQLException {
    	testResultSetColumnNames(
    		connection.getMetaData().getBestRowIdentifier(null, null, "", 0, true), 
    		"SCOPE short,COLUMN_NAME String,DATA_TYPE int, TYPE_NAME String,"
    		+"COLUMN_SIZE int,BUFFER_LENGTH int,"
    		+"DECIMAL_DIGITS short,PSEUDO_COLUMN short"); 
    }
    
    @Test 
    public void getClientInfoPropertiesBasic() throws Exception {
    	testResultSetColumnNames(
    		connection.getMetaData().getClientInfoProperties(),
    		"NAME String, MAX_LEN int, DEFAULT_VALUE String, DESCRIPTION String");
    }

    @Test
    public void getCatalogsBasic()throws SQLException  {
    	testResultSetColumnNames(
    		connection.getMetaData().getCatalogs(),
    		"TABLE_CAT String");
    }
    
    
    @Test
    public void getColumnsBasic()throws SQLException {
    	testResultSetColumnNames(connection.getMetaData().getColumns(null, null, null, null),
			"TABLE_CAT String,TABLE_SCHEM String,TABLE_NAME String,COLUMN_NAME String,"  
			+"DATA_TYPE int,TYPE_NAME String,COLUMN_SIZE int,BUFFER_LENGTH int," 
			+"DECIMAL_DIGITS int,NUM_PREC_RADIX int,NULLABLE int," 
	        +"REMARKS String,COLUMN_DEF String,SQL_DATA_TYPE int," 
			+"SQL_DATETIME_SUB int, CHAR_OCTET_LENGTH int," 
			+"ORDINAL_POSITION int,IS_NULLABLE String," 
			+"SCOPE_CATALOG String,SCOPE_SCHEMA String," 
			+"SCOPE_TABLE String,SOURCE_DATA_TYPE null");
    }
    
    @Test
    public void getColumnPrivilegesBasic()throws SQLException {
    	testResultSetColumnNames(
		 connection.getMetaData().getColumnPrivileges(null, null,"", null),
		 "TABLE_CAT String,TABLE_SCHEM String,TABLE_NAME String,COLUMN_NAME String," +
		 "GRANTOR String,GRANTEE String,PRIVILEGE String,IS_GRANTABLE String");
    }
    
    @Test
    public void getTablePrivilegesBasic()throws SQLException {
    	testResultSetColumnNames(
		 connection.getMetaData().getTablePrivileges(null, null, null),
		 "TABLE_CAT String,TABLE_SCHEM String,TABLE_NAME String,GRANTOR String," 
		 +"GRANTEE String,PRIVILEGE String,IS_GRANTABLE String");
    	
    }

    @Test 
    public void getVersionColumnsBasic()throws SQLException {
   	 	testResultSetColumnNames(
		 connection.getMetaData().getVersionColumns(null, null, null),
		 "SCOPE short, COLUMN_NAME String,DATA_TYPE int,TYPE_NAME String,"
		 +"COLUMN_SIZE int,BUFFER_LENGTH int,DECIMAL_DIGITS short,"
		 +"PSEUDO_COLUMN short");
    }
    @Test
    public void getPrimaryKeysBasic()throws SQLException {
   	 	testResultSetColumnNames(
		 connection.getMetaData().getPrimaryKeys(null, null, null),
		"TABLE_CAT String,TABLE_SCHEM String,TABLE_NAME String,COLUMN_NAME String,KEY_SEQ short,PK_NAME String"
		 ); 
    }
    @Test
    public void getImportedKeysBasic()throws SQLException {
    	testResultSetColumnNames(
   			 connection.getMetaData().getImportedKeys(null, null, ""),
        "PKTABLE_CAT String,PKTABLE_SCHEM String,PKTABLE_NAME String, PKCOLUMN_NAME String,FKTABLE_CAT String," 
    	+"FKTABLE_SCHEM String,FKTABLE_NAME String,FKCOLUMN_NAME String,KEY_SEQ short,UPDATE_RULE short," 
    	+"DELETE_RULE short,FK_NAME String,PK_NAME String,DEFERRABILITY short");

    }
   
    
    @Test
    public void getExportedKeysBasic()throws SQLException {
    	testResultSetColumnNames(
   			 connection.getMetaData().getExportedKeys(null, null, ""),
        "PKTABLE_CAT String,PKTABLE_SCHEM String,PKTABLE_NAME String, PKCOLUMN_NAME String,FKTABLE_CAT String," 
    	+"FKTABLE_SCHEM String,FKTABLE_NAME String,FKCOLUMN_NAME String,KEY_SEQ short,UPDATE_RULE short," 
    	+"DELETE_RULE short,FK_NAME String,PK_NAME String,DEFERRABILITY short");

    }
    
    @Test 
    public void getCrossReferenceBasic()throws SQLException {
    	testResultSetColumnNames(
        connection.getMetaData().getCrossReference(null, null, "", null, null, ""),
        "PKTABLE_CAT String,PKTABLE_SCHEM String,PKTABLE_NAME String, PKCOLUMN_NAME String,FKTABLE_CAT String," 
       	+"FKTABLE_SCHEM String,FKTABLE_NAME String,FKCOLUMN_NAME String,KEY_SEQ short,UPDATE_RULE short," 
       	+"DELETE_RULE short,FK_NAME String,PK_NAME String,DEFERRABILITY short");
    }
    
    @Test 
    public void getUDTsBasic() throws SQLException {
    	testResultSetColumnNames(
    	connection.getMetaData().getUDTs(null, null, null, null),
	    "TYPE_CAT String,TYPE_SCHEM String,TYPE_NAME String,CLASS_NAME String,DATA_TYPE int,"
    	+"REMARKS String,BASE_TYPE short" );
    }
    
    @Test
    public void getSuperTypesBasic() throws SQLException {
    	testResultSetColumnNames(
    	connection.getMetaData().getSuperTypes(null, null, null),
    	"TYPE_CAT String,TYPE_SCHEM String,TYPE_NAME String,SUPERTYPE_CAT String," 
    	+"SUPERTYPE_SCHEM String,SUPERTYPE_NAME String");
    }
    
    @Test 
    public void getSuperTablesBasic() throws SQLException {
    	testResultSetColumnNames(
    	connection.getMetaData().getSuperTables(null, null, null),
    	"TABLE_CAT String,TABLE_SCHEM String,TABLE_NAME String, SUPERTABLE_NAME String") ;
    }
    
    @Test
    public void testGetSchemas2() throws SQLException {
        DatabaseMetaData dbmd = connection.getMetaData();
        ResultSet rs = dbmd.getCatalogs();
        boolean foundTestUnitsJDBC = false;
        while(rs.next()) {
            if(rs.getString(1).equals("test"))
                foundTestUnitsJDBC=true;
        }
        assertEquals(true,foundTestUnitsJDBC);
    }
    

    @Test
    public void dbmetaTest() throws SQLException {
        DatabaseMetaData dmd = connection.getMetaData();
        dmd.getBestRowIdentifier(null,"test","t1",DatabaseMetaData.bestRowSession, true);
    }

    static void checkType(String name, int actualType, String colName, int expectedType)
    {
       if (name.equals(colName))
           assertEquals(actualType, expectedType);
    }
    @Test
     public void getColumnsTest() throws SQLException {
        connection.createStatement().execute(
                        "CREATE TABLE  IF NOT EXISTS `manycols` (\n" +
                        "  `tiny` tinyint(4) DEFAULT NULL,\n" +
                        "  `tiny_uns` tinyint(3) unsigned DEFAULT NULL,\n" +
                        "  `small` smallint(6) DEFAULT NULL,\n" +
                        "  `small_uns` smallint(5) unsigned DEFAULT NULL,\n" +
                        "  `medium` mediumint(9) DEFAULT NULL,\n" +
                        "  `medium_uns` mediumint(8) unsigned DEFAULT NULL,\n" +
                        "  `int_col` int(11) DEFAULT NULL,\n" +
                        "  `int_col_uns` int(10) unsigned DEFAULT NULL,\n" +
                        "  `big` bigint(20) DEFAULT NULL,\n" +
                        "  `big_uns` bigint(20) unsigned DEFAULT NULL,\n" +
                        "  `decimal_col` decimal(10,5) DEFAULT NULL,\n" +
                        "  `fcol` float DEFAULT NULL,\n" +
                        "  `fcol_uns` float unsigned DEFAULT NULL,\n" +
                        "  `dcol` double DEFAULT NULL,\n" +
                        "  `dcol_uns` double unsigned DEFAULT NULL,\n" +
                        "  `date_col` date DEFAULT NULL,\n" +
                        "  `time_col` time DEFAULT NULL,\n" +
                        "  `timestamp_col` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE\n" +
                        "CURRENT_TIMESTAMP,\n" +
                        "  `year_col` year(4) DEFAULT NULL,\n" +
                        "  `bit_col` bit(5) DEFAULT NULL,\n" +
                        "  `char_col` char(5) DEFAULT NULL,\n" +
                        "  `varchar_col` varchar(10) DEFAULT NULL,\n" +
                        "  `binary_col` binary(10) DEFAULT NULL,\n" +
                        "  `varbinary_col` varbinary(10) DEFAULT NULL,\n" +
                        "  `tinyblob_col` tinyblob,\n" +
                        "  `blob_col` blob,\n" +
                        "  `mediumblob_col` mediumblob,\n" +
                        "  `longblob_col` longblob,\n" +
                        "  `text_col` text,\n" +
                        "  `mediumtext_col` mediumtext,\n" +
                        "  `longtext_col` longtext\n" +
                        ")"
        );
        DatabaseMetaData dmd = connection.getMetaData();
        ResultSet rs = dmd.getColumns(connection.getCatalog(), null, "manycols", null);
        while(rs.next()) {
            String columnName = rs.getString("column_name");
            int type = rs.getInt("data_type");
            checkType(columnName, type, "tiny", Types.TINYINT);
            checkType(columnName, type, "tiny_uns", Types.TINYINT);
            checkType(columnName, type, "small", Types.SMALLINT);
            checkType(columnName, type, "small_uns", Types.SMALLINT);
            checkType(columnName, type, "medium", Types.INTEGER);
            checkType(columnName, type, "medium_uns", Types.INTEGER);
            checkType(columnName, type, "int_col", Types.INTEGER);
            checkType(columnName, type, "int_col_uns", Types.INTEGER);
            checkType(columnName, type, "big", Types.BIGINT);
            checkType(columnName, type, "big_uns", Types.BIGINT);
            checkType(columnName, type ,"decimal_col",Types.DECIMAL);
            checkType(columnName, type, "fcol", Types.FLOAT);
            checkType(columnName, type, "fcol_uns", Types.FLOAT);
            checkType(columnName, type, "dcol", Types.DOUBLE);
            checkType(columnName, type, "dcol_uns", Types.DOUBLE);
            checkType(columnName, type, "date_col", Types.DATE);
            checkType(columnName, type, "time_col", Types.TIME);
            checkType(columnName, type, "timestamp_col", Types.TIMESTAMP);
            checkType(columnName, type, "year_col", Types.SMALLINT);
            checkType(columnName, type, "bit_col", Types.BIT);
            checkType(columnName, type, "char_col", Types.CHAR);
            checkType(columnName, type, "varchar_col", Types.VARCHAR);
            checkType(columnName, type, "binary_col", Types.BINARY);
            checkType(columnName, type, "tinyblob_col", Types.LONGVARBINARY);
            checkType(columnName, type, "blob_col", Types.LONGVARBINARY);
            checkType(columnName, type, "longblob_col", Types.LONGVARBINARY);
            checkType(columnName, type, "mediumblob_col", Types.LONGVARBINARY);
            checkType(columnName, type, "text_col", Types.LONGVARCHAR);
            checkType(columnName, type, "mediumtext_col", Types.LONGVARCHAR);
            checkType(columnName, type, "longtext_col", Types.LONGVARCHAR);
        }
    }

}