
package com.felixalacampagne.account.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.Account;

@Repository
public interface AccountJpaRepository extends JpaRepository<Account, Long>
{
   // This is called Derived Query Method and is probably the more politically correct way
   // to do it in the 'Repository' class with the 'Service' class providing the 'business'
   // knowledge that 255 is the value for non-active accounts.
   // OrderByAccOrderAscAccDescAsc
   // List<Account> findAccountsByAccOrderNotOrderByAccOrderAscAccNameAsc(short accOrder);

   // The derived query method appears to suffer from the limitation that a property ending in
   // Desc cannot be used in the order by clause, eg. accDesc gives strange errors about there being
   // no 'acc'. The whole concept results in hideous impossible to type or read method names so
   // its best to stick to using @Query.
   // WARNING: apparently there is no way to ignore case in the order by. Only solutions I've come across
   // mean any slight advantage to using JPA query is lost as the returned objects are not Accounts, or
   // a simple string select can't be used, etc., etc., on and on, demonstrating once again that this
   // shirt is really not intended for use in the real world where normal humans expect words starting with 'a'
   // to be listed with words starting with 'A', not at the end after all the 'Z's - that's worse than a French
   // phonebook (when such things existed!)
   // TODO: Change ints, longs and shorts in the entity definition to Long to make life easier....
   @Query("select a from Account a where a.accOrder not in (:excluded) order by a.accOrder asc, a.accDesc asc")
   List<Account> findAccountsExcludeAccOrderSorted(@Param(value = "excluded") List<Long> excluded);
   
   @Query("select a from Account a where a.accCurr = :currency and a.id != :srcid and a.accOrder not in (:excluded) order by a.accOrder asc, a.accDesc asc")
   List<Account> findTransferAccounts(
         @Param(value = "srcid") Long srcid,
         @Param(value = "currency") String currency,
         @Param(value = "excluded") List<Long> excluded );
   

}
