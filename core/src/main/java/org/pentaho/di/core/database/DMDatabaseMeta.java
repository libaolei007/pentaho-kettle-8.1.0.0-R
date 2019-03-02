package org.pentaho.di.core.database;

import java.sql.ResultSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * 达梦数据库DatabaseMeta
 * @author hnzs
 *
 */
public class DMDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  private static final String STRICT_BIGNUMBER_INTERPRETATION = "STRICT_NUMBER_38_INTERPRETATION";

  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 12345;
    }
    return -1;
  }

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    return false;
  }

  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
   */
  @Override
  public String getLimitClause( int nrRows ) {
    return " WHERE ROWNUM <= " + nrRows;
  }

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName
   *          The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName + " WHERE 1=0";
  }

  @Override
  public String getSQLTableExists( String tablename ) {
    return getSQLQueryFields( tablename );
  }

  @Override
  public String getSQLColumnExists( String columnname, String tablename ) {
    return getSQLQueryColumnFields( columnname, tablename );
  }

  public String getSQLQueryColumnFields( String columnname, String tableName ) {
    return "SELECT " + columnname + " FROM " + tableName + " WHERE 1=0";
  }

  @Override
  public boolean needsToLockAllTables() {
    return false;
  }

  @Override
  public String getDriverClass() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "sun.jdbc.odbc.JdbcOdbcDriver";
    } else {
      return "dm.jdbc.driver.DmDriver";
    }
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
	if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
    	//jdbc:dm://host:port/dataseName
    	String host = hostname;
    	String dbPort = port;
    	if (Utils.isEmpty( hostname )) {
    		host = "localhost";
    	}
    	if (Utils.isEmpty( port )) {
    		dbPort = "12345";
    	}
    	if (Utils.isEmpty( databaseName )) {
    		throw new KettleDatabaseException("数据库名称不可以为空");
    	}
    	if (!databaseName.startsWith("/")) {
    		databaseName = "/" + databaseName;
    	}
    	return "jdbc:dm://" + host + ":" + dbPort + databaseName;
    } else {
    	throw new KettleDatabaseException("不支持的数据库连接方式[" + getAccessType() + "]");
    }
  }

  /**
   * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
   */
  @Override
  public boolean supportsOptionsInURL() {
    return false;
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return true;
  }

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    int dotPos = sequenceName.indexOf( '.' );
    String sql = "";
    if ( dotPos == -1 ) {
      // if schema is not specified try to get sequence which belongs to current user
      sql = "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" + sequenceName.toUpperCase() + "'";
    } else {
      String schemaName = sequenceName.substring( 0, dotPos );
      String seqName = sequenceName.substring( dotPos + 1 );
      sql =
        "SELECT * FROM ALL_SEQUENCES WHERE SEQUENCE_NAME = '"
          + seqName.toUpperCase() + "' AND SEQUENCE_OWNER = '" + schemaName.toUpperCase() + "'";
    }
    return sql;
  }

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "SELECT " + sequenceName + ".currval FROM DUAL";
  }

  /**
   * Get the SQL to get the next value of a sequence. (Oracle only)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle only)
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "SELECT " + sequenceName + ".nextval FROM dual";
  }

  @Override
  public boolean supportsSequenceNoMaxValueOption() {
    return true;
  }

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  @Override
  public boolean useSchemaNameForTableList() {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return true;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE "
      + tablename + " ADD  " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " DROP  " + v.getName() + Const.CR;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    ValueMetaInterface tmpColumn = v.clone();
    String tmpName = v.getName();
    boolean isQuoted = tmpName.startsWith( "\"" ) && tmpName.endsWith( "\"" );
    if ( isQuoted ) {
      // remove the quotes first.
      //
      tmpName = tmpName.substring( 1, tmpName.length() - 1 );
    }

    int threeoh = tmpName.length() >= 30 ? 30 : tmpName.length();
    tmpName = tmpName.substring( 0, threeoh );

    tmpName += "_KTL"; // should always be shorter than 35 positions

    // put the quotes back if needed.
    //
    if ( isQuoted ) {
      tmpName = "\"" + tmpName + "\"";
    }
    tmpColumn.setName( tmpName );

    // Read to go.
    //
    String sql = "";

    // Create a new tmp column
    sql += getAddColumnStatement( tablename, tmpColumn, tk, use_autoinc, pk, semicolon ) + ";" + Const.CR;
    // copy the old data over to the tmp column
    sql += "UPDATE " + tablename + " SET " + tmpColumn.getName() + "=" + v.getName() + ";" + Const.CR;
    // drop the old column
    sql += getDropColumnStatement( tablename, v, tk, use_autoinc, pk, semicolon ) + ";" + Const.CR;
    // create the wanted column
    sql += getAddColumnStatement( tablename, v, tk, use_autoinc, pk, semicolon ) + ";" + Const.CR;
    // copy the data from the tmp column to the wanted column (again)
    // All this to avoid the rename clause as this is not supported on all Oracle versions
    sql += "UPDATE " + tablename + " SET " + v.getName() + "=" + tmpColumn.getName() + ";" + Const.CR;
    // drop the temp column
    sql += getDropColumnStatement( tablename, tmpColumn, tk, use_autoinc, pk, semicolon );

    return sql;
  }

  @Override
  public String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean use_autoinc,
    boolean add_fieldname, boolean add_cr ) {
    StringBuilder retval = new StringBuilder( 128 );

    String fieldname = v.getName();
    int length = v.getLength();
    int precision = v.getPrecision();

    if ( add_fieldname ) {
      retval.append( fieldname ).append( ' ' );
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
        if ( supportsTimestampDataType() ) {
          retval.append( "TIMESTAMP" );
        } else {
          retval.append( "DATE" );
        }
        break;
      case ValueMetaInterface.TYPE_DATE:
        retval.append( "DATE" );
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        retval.append( "CHAR(1)" );
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        retval.append( "NUMBER" );
        if ( length > 0 ) {
          retval.append( '(' ).append( length );
          if ( precision > 0 ) {
            retval.append( ", " ).append( precision );
          }
          retval.append( ')' );
        }
        break;
      case ValueMetaInterface.TYPE_INTEGER:
        retval.append( "INTEGER" );
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length >= DatabaseMeta.CLOB_LENGTH ) {
          retval.append( "CLOB" );
        } else {
          if ( length == 1 ) {
            retval.append( "CHAR(1)" );
          } else if ( length > 0 && length <= getMaxVARCHARLength() ) {
            retval.append( "VARCHAR2(" ).append( length ).append( ')' );
          } else {
            if ( length <= 0 ) {
              retval.append( "VARCHAR2(2000)" ); // We don't know, so we just use the maximum...
            } else {
              retval.append( "CLOB" );
            }
          }
        }
        break;
      case ValueMetaInterface.TYPE_BINARY: // the BLOB can contain binary data.
        retval.append( "BLOB" );
        break;
      default:
        retval.append( " UNKNOWN" );
        break;
    }

    if ( add_cr ) {
      retval.append( Const.CR );
    }

    return retval.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {
		"ABSOLUTE", "ALL", "ALTER", "AND", "ANY", "AS", "ASC", "AUDIT", "AUTHORIZATION", "AVG",
		"BEGIN", "BIGDATEDIFF", "BOTH", "CALL", "CASE", "CAST", "CHECK", "CLOSE", "CLUSTER", "COALESCE",
		"COLUMN", "COMMIT", "COMMITWORK", "CONNECT", "CONNECT_BY_ISLEAF", "CONNECT_BY_ISCYCLE",
		"CONNECT_BY_ROOT", "CONSTRAINT", "CONTAINS", "CONVERT", "COUNT", "CREATE", "CROSS", "CRYPTO", "CURSOR",
		"DATABASE", "DATEADD", "DATEDIFF", "DATEPART", "DECLARE", "DECODE", "DEFAULT", "DELETE", "DELETING",
		"DEREF", "DISTINCT", "DROP", "ELSE", "ELSEIF", "ELSIF", "END", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS",
		"EXIT", "EXPLAIN", "EXTRACT", "EVENTINFO", "FALSE", "FETCH", "FIRST", "FOR", "FOREIGN", "FREQUENCE",
		"FROM", "FULL", "FUNCTION", "GOTO", "GRANT", "GROUP", "HAVING", "HEXTORAW", "IDENTITY ", "IF ",
		"IFNULL ", "IMMEDIATE", "INCREASE", "INDEX", "INNER", "INSERT", "INSERTING", "INTERVAL", "INTO",
		"ISNULL", "JOIN", "LAST", "LEADING", "LEFT", "LEVEL", "LIMIT", "LINK", "LOGIN", "LOOP", "MAX", "MEMBER",
		"MIN", "NATURAL", "NEW", "NEXT", "NOAUDIT", "NOCYCLE", "NOT", "NOWAIT", "NULL", "NULLIF", "NVL",
		"OBJECT", "OLD", "ON", "ONLINE", "OPEN", "ORDER", "PACKAGE", "PENDANT", "PERCENT", "POLICY", "PRIMARY",
		"PRINT", "PRIOR", "PROCEDURE", "RAISE", "RANGE", "RAWTOHEX", "REF", "REFERENCES", "REFERENCING",
		"RELATIVE", "REPEAT", "REPLACE", "RETURN", "RETURNING", "REVERSE", "REVOKE", "RIGHT", "ROLE",
		"ROLLBACK", "ROW", "ROWNUM", "SAVEPOINT", "SCHEMA", "SELECT", "SEQUENCE", "SET", "SOME", "SQL",
		"SUBSTRING", "SUM", "SYS_CONNECT_BY_PATH", "SYNONYM", "TABLE", "TIMESTAMPADD", "TIMESTAMPDIFF", "TOP",
		"TRAILING", "TRIGGER", "TRIM", "TRUE", "TRUNCATE", "UNIQUE", "UNTIL", "UPDATE", "UPDATING", "USER",
		"USING", "VALUES", "VARIANCE", "VIEW", "VSIZE", "WHEN", "WHERE", "WHILE", "WITH" };
  }

  /**
   * @return The SQL on this database to get a list of stored procedures.
   */
  @Override
  public String getSQLListOfProcedures() {
    return "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.') || object_name "
      + "FROM user_arguments "
      + "ORDER BY 1";
  }

  @Override
  public String getSQLLockTables( String[] tableNames ) {
    StringBuilder sql = new StringBuilder( 128 );
    for ( int i = 0; i < tableNames.length; i++ ) {
      sql.append( "LOCK TABLE " ).append( tableNames[i] ).append( " IN EXCLUSIVE MODE;" ).append( Const.CR );
    }
    return sql.toString();
  }

  @Override
  public String getSQLUnlockTables( String[] tableNames ) {
    return null; // commit handles the unlocking!
  }

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  @Override
  public String getExtraOptionsHelpText() {
    return "http://www.dameng.com";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "DmJdbcDriver.jar" };
  }

  /**
   * Verifies on the specified database connection if an index exists on the fields with the specified name.
   *
   * @param database
   *          a connected database
   * @param schemaName
   * @param tableName
   * @param idx_fields
   * @return true if the index exists, false if it doesn't.
   * @throws KettleDatabaseException
   */
  @Override
  public boolean checkIndexExists( Database database, String schemaName, String tableName, String[] idx_fields ) throws KettleDatabaseException {

    String tablename = database.getDatabaseMeta().getQuotedSchemaTableCombination( schemaName, tableName );

    boolean[] exists = new boolean[idx_fields.length];
    for ( int i = 0; i < exists.length; i++ ) {
      exists[i] = false;
    }

    try {
      //
      // Get the info from the data dictionary...
      //
      String sql = "SELECT * FROM USER_IND_COLUMNS WHERE TABLE_NAME = '" + tableName + "'";
      ResultSet res = null;
      try {
        res = database.openQuery( sql );
        if ( res != null ) {
          Object[] row = database.getRow( res );
          while ( row != null ) {
            String column = database.getReturnRowMeta().getString( row, "COLUMN_NAME", "" );
            int idx = Const.indexOfString( column, idx_fields );
            if ( idx >= 0 ) {
              exists[idx] = true;
            }

            row = database.getRow( res );
          }

        } else {
          return false;
        }
      } finally {
        if ( res != null ) {
          database.closeQuery( res );
        }
      }

      // See if all the fields are indexed...
      boolean all = true;
      for ( int i = 0; i < exists.length && all; i++ ) {
        if ( !exists[i] ) {
          all = false;
        }
      }

      return all;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unable to determine if indexes exists on table [" + tablename + "]", e );
    }
  }

  @Override
  public boolean requiresCreateTablePrimaryKeyAppend() {
    return true;
  }

  /**
   * Most databases allow you to retrieve result metadata by preparing a SELECT statement.
   *
   * @return true if the database supports retrieval of query metadata from a prepared statement. False if the query
   *         needs to be executed first.
   */
  @Override
  public boolean supportsPreparedStatementMetadataRetrieval() {
    return false;
  }

  /**
   * @return The maximum number of columns in a database, <=0 means: no known limit
   */
  @Override
  public int getMaxColumnsInIndex() {
    return 32;
  }

  /**
   * @return The SQL on this database to get a list of sequences.
   */
  @Override
  public String getSQLListOfSequences() {
    return "SELECT SEQUENCE_NAME FROM all_sequences";
  }

  /**
   * @param string
   * @return A string that is properly quoted for use in an Oracle SQL statement (insert, update, delete, etc)
   */
  @Override
  public String quoteSQLString( String string ) {
    string = string.replaceAll( "'", "''" );
    string = string.replaceAll( "\\n", "'||chr(13)||'" );
    string = string.replaceAll( "\\r", "'||chr(10)||'" );
    return "'" + string + "'";
  }

  /**
   * Returns a false as Oracle does not allow for the releasing of savepoints.
   */
  @Override
  public boolean releaseSavepoint() {
    return false;
  }

  /**
   * Returns an empty string as most databases do not support tablespaces. Subclasses can override this method to
   * generate the DDL.
   *
   * @param variables
   *          variables needed for variable substitution.
   * @param databaseMeta
   *          databaseMeta needed for it's quoteField method. Since we are doing variable substitution we need to meta
   *          so that we can act on the variable substitution first and then the creation of the entire string that will
   *          be retuned.
   * @param tablespace
   *          tablespaceName name of the tablespace.
   *
   * @return String the TABLESPACE tablespaceName section of an Oracle CREATE DDL statement.
   */
  @Override
  public String getTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta, String tablespace ) {
    if ( !Utils.isEmpty( tablespace ) ) {
      return "TABLESPACE " + databaseMeta.quoteField( variables.environmentSubstitute( tablespace ) );
    } else {
      return "";
    }
  }

  @Override
  public boolean supportsErrorHandlingOnBatchUpdates() {
    return false;
  }

  /**
   * @return true if Kettle can create a repository on this type of database.
   */
  @Override
  public boolean supportsRepository() {
    return true;
  }

  @Override
  public int getMaxVARCHARLength() {
	  /*
	  数据库页面大小	实际最大长度
	  4K		1900
	  8K		3900
	  16K		8000
	  32K		8188
	  */
    return 1900;
  }

  /**
   * Oracle does not support a construct like 'drop table if exists',
   * which is apparently legal syntax in many other RDBMSs.
   * So we need to implement the same behavior and avoid throwing 'table does not exist' exception.
   *
   * @param tableName Name of the table to drop
   * @return 'drop table if exists'-like statement for Oracle
   */
  @Override
  public String getDropTableIfExistsStatement( String tableName ) {
    return "BEGIN EXECUTE IMMEDIATE 'DROP TABLE " + tableName
      + "'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;";
  }

  @Override
  public SqlScriptParser createSqlScriptParser() {
    return new SqlScriptParser( false );
  }

  /**
   * @return true if using strict number(38) interpretation
   */
  public boolean strictBigNumberInterpretation() {
    return "Y".equalsIgnoreCase( getAttributes().getProperty( STRICT_BIGNUMBER_INTERPRETATION, "N" ) );
  }

  /**
   * @param  strictBigNumberInterpretation true if use strict number(38) interpretation
   */
  public void setStrictBigNumberInterpretation( boolean strictBigNumberInterpretation ) {
    getAttributes().setProperty( STRICT_BIGNUMBER_INTERPRETATION, strictBigNumberInterpretation ? "Y" : "N" );
  }
}
