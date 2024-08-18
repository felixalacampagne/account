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

import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;

/**
 * 
 * Hibernate dialect for UCanAccess - limit handler for query results
 * 
 * With Hibernate 6.5 this class is never used because the Dialect.getLimitHandler method is never invoked.
 * 
 */
public class UCanAccessDialectLimitHandler extends LimitOffsetLimitHandler { //implements LimitHandler {
   public static final UCanAccessDialectLimitHandler LIMIT_HANDLER = new UCanAccessDialectLimitHandler();
//    @Override
//    public boolean supportsLimit() {
//        return true;
//    }

    @Override
    public boolean supportsLimitOffset() {
        return true;
    }

   @Override
   public boolean supportsOffset()
   {
      // TODO Auto-generated method stub
      return false;
   }

//   @Override
//   public String processSql(String sql, Limit limit)
//   {
//      System.out.println("UCanAccessDialectLimitHandler.processSql: " + sql);
//      return String.format("%s limit %d offset %d", sql, 
//            limit.getMaxRows(), limit.getFirstRow());
////            selection.getMaxRows(), selection.getFirstRow());
//
//   }

//   @Override
//   public int bindLimitParametersAtStartOfQuery(Limit limit, PreparedStatement statement, int index) throws SQLException
//   {
//      // TODO Auto-generated method stub
//      return 0;
//   }
//
//   @Override
//   public int bindLimitParametersAtEndOfQuery(Limit limit, PreparedStatement statement, int index) throws SQLException
//   {
//      // TODO Auto-generated method stub
//      return 0;
//   }
//
//   @Override
//   public void setMaxRows(Limit limit, PreparedStatement statement) throws SQLException
//   {
//      // TODO Auto-generated method stub
//      
//   }

}
