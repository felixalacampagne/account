/*
   Copyright 2017 Gordon D. Thompson

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.ucanaccess.hibernate.dialect;

import java.sql.Types;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

/**
 *
 * Hibernate dialect for UCanAccess
 *
 */

// UCanAccess uses Jackcess and HSQLDB so maybe it makes more sense to extend
// HSQLDialect. Anyway, with Hibernate 6.5 the SQLServerDialect is not capable of doing
// pageination queries but HSQLDialect is.
public class UCanAccessDialect extends HSQLDialect
{ // SQLServerDialect {

   public UCanAccessDialect() {
        super();
        System.err.println("UCanAccessDialect.<init>: entry");
        init();
    }

   public UCanAccessDialect(DatabaseVersion version) {
      super(version);
      System.err.println("UCanAccessDialect.<init>(DatabaseVersion): entry");
      init();
   }

   public UCanAccessDialect(DialectResolutionInfo info) {
      // This is the one that is called....
      super(info);
      System.err.println("UCanAccessDialect.<init>(DialectResolutionInfo): entry: " + info);
      init();
   }

   protected void init()
   {
      // lets UCanAccess determine if it is working with Hibernate
      System.setProperty(this.getClass().getName() + ".isActive", "true");

      // SQLSyntaxErrorException: user lacks privilege or object not found: net.ucanaccess.converters.Functions
      // google says this might cure it... and wow! it does appear to have made it go away
      System.setProperty("hsqldb.method_class_names", "net.ucanaccess.converters.*"); // see http://hsqldb.org/doc/2.0/guide/sqlroutines-chapt.html#src_jrt_access_control

   }

    @Override
    public JdbcType resolveSqlTypeDescriptor(
          String columnTypeName,
          int jdbcTypeCode,
          int precision,
          int scale,
          JdbcTypeRegistry jdbcTypeRegistry) {
       System.err.println("UCanAccessDialect.resolveSqlTypeDescriptor: entry");
       // think this replaces the registerColumnType for mapping an Access column type name
       // to a standard JDBC type
       if("LONG".equalsIgnoreCase(columnTypeName))
       {
          jdbcTypeCode = Types.INTEGER;
       }
       else if("YESNO".equalsIgnoreCase(columnTypeName))
       {
          jdbcTypeCode = Types.BOOLEAN;
       }
       else if("MEMO".equalsIgnoreCase(columnTypeName))
       {
          jdbcTypeCode = Types.CLOB;
       }
       else if("OLE".equalsIgnoreCase(columnTypeName))
       {
          jdbcTypeCode = Types.BLOB;
       }
       return super.resolveSqlTypeDescriptor( columnTypeName, jdbcTypeCode, precision, scale, jdbcTypeRegistry );
    }


    @Override
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
       System.err.println("UCanAccessDialect.initializeFunctionRegistry: entry");

      super.initializeFunctionRegistry(functionContributions);
      BasicTypeRegistry basicTypeRegistry = functionContributions.getTypeConfiguration().getBasicTypeRegistry();

      // registerFunction and/or SQLFunctionTemplate no longer exist.
      // I don't really have a clue what this is doing. I'm hoping that there isn't anything in my DB
      // hich requires if
//      registerFunction("current_date", new StandardSQLFunction("Date", StandardBasicTypes.DATE));
//      registerFunction("current_time", new StandardSQLFunction("Time", StandardBasicTypes.TIME));
//      registerFunction("current_timestamp", new StandardSQLFunction("Now", StandardBasicTypes.TIMESTAMP));
//      registerFunction("second", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "Second(?1)"));
//      registerFunction("minute", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "Minute(?1)"));
//      registerFunction("hour", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "Hour(?1)"));

      // Google suggests something something like the following is the new, very much continuously improved,
      // much more complex and much more impossible to comprehend,
      // ie. a perfect example of continuous improvment, shirt that has replaced the registerFunction stuff
      // Got no idea what to do about the SQLFunctionTemplate stuff though

      functionContributions.getFunctionRegistry().register("current_date", new StandardSQLFunction("Date", StandardBasicTypes.DATE));
      functionContributions.getFunctionRegistry().register("current_time", new StandardSQLFunction("Time", StandardBasicTypes.TIME));
      functionContributions.getFunctionRegistry().register("current_timestamp", new StandardSQLFunction("Now", StandardBasicTypes.TIMESTAMP));

      // Got no idea what to do about the SQLFunctionTemplate stuff though. Found this on Google and adapted it a bit
//      functionContributions.getFunctionRegistry().registerPattern(
//          "hstore_find",
//          "(?1 -> ?2 = ?3)",
//          basicTypeRegistry.resolve( StandardBasicTypes.BOOLEAN ));
      functionContributions.getFunctionRegistry()
         .registerPattern("second", "Second(?1)", basicTypeRegistry.resolve( StandardBasicTypes.INTEGER ));
      functionContributions.getFunctionRegistry()
         .registerPattern("minute", "Minute(?1)", basicTypeRegistry.resolve( StandardBasicTypes.INTEGER ));
      functionContributions.getFunctionRegistry()
         .registerPattern("hour", "Hour(?1)", basicTypeRegistry.resolve( StandardBasicTypes.INTEGER ));

//      // SQLServerDIalect seems to translate 'count' to 'count_big' which obviously doesn't work. Can't
//      // see anything for removing registered things so try to overwrite the exiting one....
//      // it seems to have worked....
//      functionContributions.getFunctionRegistry().register(
//            "count",
//            new CountFunction(
//                  this,
//                  functionContributions.getTypeConfiguration(),
//                  SqlAstNodeRenderingMode.DEFAULT,
//                  "count",
//                  "+",
//                  "varchar(max)",
//                  true,
//                  "varbinary(max)"
//            )
//      );
      // ...
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
       System.err.println("UCanAccessDialect.getIdentityColumnSupport: entry");
        return UCanAccessDialectIdentityColumnSupport.IDENTITY_COLUMN_SUPPORT;
    }

    @Override
    public UniqueDelegate getUniqueDelegate() {
       System.err.println("UCanAccessDialect.getUniqueDelegate: entry");
        return UCanAccessDialectUniqueDelegate.UNIQUE_DELEGATE;
    }

    @Override
    public LimitHandler getLimitHandler() {
       // Why isn't this method called? What has it been replaced with?
       System.err.println("UCanAccessDialect.getLimitHandler: returning UCanAccessDialectLimitHandler");
       return UCanAccessDialectLimitHandler.LIMIT_HANDLER;
    }

    @Override
    protected void registerDefaultKeywords() {
       System.err.println("UCanAccessDialect.registerDefaultKeywords: entry");
       super.registerDefaultKeywords();
       getKeywords().remove("top");
    }
    @Override
    public char openQuote() {
       return '[';
    }

    @Override
    public char closeQuote() {
       return ']';
    }

//    // SQLServerSqlAstTranslator might be responsible for the TOP appearing in select queries but
//    // it cannot be sub-classed so no point in doing this override
//    @Override
//    public SqlAstTranslatorFactory getSqlAstTranslatorFactory() {
//       return new StandardSqlAstTranslatorFactory() {
//          @Override
//          protected <T extends JdbcOperation> SqlAstTranslator<T> buildTranslator(
//                SessionFactoryImplementor sessionFactory, Statement statement) {
//             return new SQLServerLegacySqlAstTranslator<>( sessionFactory, statement );
//          }
//       };
//    }
}
