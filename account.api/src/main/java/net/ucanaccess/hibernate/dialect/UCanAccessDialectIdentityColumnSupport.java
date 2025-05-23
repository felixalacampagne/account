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

import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

/**
 *
 * Hibernate dialect for UCanAccess - identity column support
 *
 */
public class UCanAccessDialectIdentityColumnSupport extends IdentityColumnSupportImpl {
   public static final UCanAccessDialectIdentityColumnSupport IDENTITY_COLUMN_SUPPORT =
         new UCanAccessDialectIdentityColumnSupport();

    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "COUNTER";
    }

    /*
     * public boolean supportsInsertSelectIdentity() { return true; // As
     * specified in NHibernate dialect }
     */

    /*
     * public String appendIdentitySelectToInsert(String insertString) { return
     * new StringBuffer(insertString.length()+30). // As specified in NHibernate
     * dialect append(insertString).
     * append("; ").append(getIdentitySelectString()). toString(); }
     */
}
