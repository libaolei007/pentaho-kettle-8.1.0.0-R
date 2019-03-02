package org.pentaho.di.core.database;

import java.sql.ResultSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;

/**
 * 神通数据库DatabaseMeta(数据库插件)
 *
 * @author Matt
 * @since 11-mrt-2005
 */

public class OscarDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface {

  private static final String STRICT_BIGNUMBER_INTERPRETATION = "STRICT_NUMBER_38_INTERPRETATION";

  @Override
  public int[] getAccessTypeList() {
    return new int[] {
      DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
  }

  @Override
  public int getDefaultDatabasePort() {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      return 2003;
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
      return "com.oscar.Driver";
    }
  }

  @Override
  public String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException {
    if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC ) {
      return "jdbc:odbc:" + databaseName;
    } else if ( getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE ) {
      // <host>/<database>
      // <host>:<port>/<database>
      String _hostname = hostname;
      String _port = port;
      String _databaseName = databaseName;
      if (Utils.isEmpty( hostname )) {
    	  _hostname = "localhost";
      }
      if (Utils.isEmpty( port ) || port.equals( "-1" )) {
    	  _port = "";
      }
      if (Utils.isEmpty( databaseName )) {
    	  throw new KettleDatabaseException("必须指定数据库名称" ); 
      }
      if (!databaseName.startsWith("/")) {
    	  _databaseName = "/" + databaseName;
  	  }
      return "jdbc:oscar://" + _hostname + (Utils.isEmpty( _port ) ? "" : ":" + _port) + _databaseName;
    } else {
        throw new KettleDatabaseException("不支持的数据库连接方式[" + getAccessType() + "]" );
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
      + tablename + " ADD " + getFieldDefinition( v, tk, pk, use_autoinc, true, false );
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
    return "ALTER TABLE " + tablename + " DROP COLUMN " + v.getName() + Const.CR;
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
      retval.append(fieldname).append(" ");
    }

    int type = v.getType();
    switch ( type ) {
      case ValueMetaInterface.TYPE_TIMESTAMP:
      case ValueMetaInterface.TYPE_DATE:
        retval.append("TIMESTAMP");
        break;
      case ValueMetaInterface.TYPE_BOOLEAN:
        if ( supportsBooleanDataType() ) {
          retval.append("BOOLEAN");
        } else {
          retval.append("CHAR(1)");
        }
        break;
      case ValueMetaInterface.TYPE_NUMBER:
      case ValueMetaInterface.TYPE_INTEGER:
      case ValueMetaInterface.TYPE_BIGNUMBER:
        if ( fieldname.equalsIgnoreCase( tk ) || // Technical key
            fieldname.equalsIgnoreCase( pk ) // Primary key
        ) {
          retval.append("BIGSERIAL");
        } else {
          if ( length > 0 ) {
        	if ( precision > 0 || length > 18 ) {
              // Numeric(Precision, Scale): Precision = total length; Scale = decimal places
              retval.append("NUMERIC(").append( length + precision ).append(", ").append(precision).append(")");
            } else if (precision == 0){
            	if ( length > 9 ) {
            		retval.append("BIGINT");
            	} else {
            		if ( length < 5 ) {
            			retval.append("SMALLINT");
            		} else {
            			retval.append("INT");
            		}
            	}
            } else {
            	retval.append("FLOAT(53)");
            }

          } else {
            retval.append("DOUBLE PRECISION");
          }
        }
        break;
      case ValueMetaInterface.TYPE_STRING:
        if ( length < 1 || length >= DatabaseMeta.CLOB_LENGTH ) {
          retval.append("TEXT");
        } else {
          retval.append("VARCHAR(").append(length).append(")");
        }
        break;
      case ValueMetaInterface.TYPE_BINARY:
          retval.append("BLOB");
          break;  
      default:
        retval.append(" UNKNOWN");
        break;
    }

    if ( add_cr ) {
      retval.append(Const.CR);
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
    	"ALIAS", "AND", "AS", "AT", "BEGIN", "BETWEEN", "BIGINT", "BIT", "BY", "BOOLEAN", "BOTH",
		"CALL", "CASE", "CAST", "CHAR", "CHARACTER", "COMMIT", "CONSTANT", "CURSOR", "COALESCE", "CONTINUE",
		"CONVERT", "CURRENT_DATE", "CURRENT_TIMESTAMP", "CURRENT_USER", "DATE", "DEC", "DECIMAL", "DECLARE",
		"DEFAULT", "DECODE", "DELETE", "ELSE", "ELSIF", "END", "EXCEPTION", "EXECUTE", "EXIT", "EXTRACT",
		"FALSE", "FETCH", "FLOAT", "FOR", "FROM", "FUNCTION", "GOTO", "IF", "IN", "INT", "INTO", "IS",
		"INTEGER", "IMMEDIATE", "INDEX", "INOUT", "INSERT", "LEADING", "LIKE", "LIMIT", "LOCALTIME",
		"LOCALTIMESTAMP", "LOOP", "NCHAR", "NEXT", "NOCOPY", "NOT", "NULLIF", "NULL", "NUMBER", "NUMERIC",
		"OPTION", "OF", "OR", "OUT", "OVERLAY", "PERFORM", "POSITION", "PRAGMA", "PROCEDURE", "QUERY", "RAISE",
		"RECORD", "RENAME", "RETURN", "REVERSE", "ROLLBACK", "REAL", "SELECT", "SAVEPOINT", "SETOF", "SMALLINT",
		"SUBSTRING", "SQL", "SYSDATE", "SESSION_USER", "THEN", "TO", "TYPE", "TABLE", "TIME", "TIMESTAMP",
		"TINYINT", "TRAILING", "TREAT", "TRIM", "TRUE", "TYPE", "UID", "UPDATE", "USER", "USING", "VARCHAR",
		"VARCHAR2", "VALUES", "WITH", "WHEN", "WHILE", "LEVEL" };
  }

  /**
   * @return The SQL on this database to get a list of stored procedures.
   */
  @Override
  public String getSQLListOfProcedures() {
    /*return "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.') || object_name "
      + "FROM user_arguments "
      + "ORDER BY 1";*/
	 return "SELECT name FROM ORM_FUNCTIONS union SELECT name FROM ORM_PROCEDURES";
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
    return "http://www.shentongdata.com/?bid=3&eid=249";
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] { "oscarJDBC.jar", "oscarJDBC14.jar", "oscarJDBC16.jar" };
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
    return 2000;
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
    return "DROP TABLE IF EXISTS " + tableName;
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
