package com.felixalacampagne.account.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.PhoneAccount;
import com.felixalacampagne.account.persistence.entities.PhoneWithAccountDTO;
import com.felixalacampagne.account.persistence.entities.PhoneWithAccountProjection;

@Repository
public interface PhoneAccountJpaRepository extends JpaRepository<PhoneAccount, Long>
{

   // NB. is accountId is null then 'p.accountId != :exaccid' is false!
   @Query("SELECT p FROM PhoneAccount p where p.accountId != :exaccid and p.order not in (255) "
        + "order by p.order DESC, p.desc ASC" )
   List<PhoneAccount> findTransferAccounts(@Param("exaccid") Long excludeAccountId);

   @Query("SELECT p as phoneAccount, a.accDesc as accDesc, a.accCode as accCode FROM PhoneAccount p LEFT JOIN Account a ON  p.accountId = a.accId where p.accountId != :exaccid and p.order not in (255) "
         + "order by p.order DESC, p.desc ASC" )
   List<PhoneWithAccountProjection> findTransferAccountsWithAccount(@Param("exaccid") Long excludeAccountId);

   @Query("SELECT p as phoneAccount, a.accDesc as accDesc, a.accCode as accCode "
         + "FROM PhoneAccount p LEFT JOIN Account a ON  p.accountId = a.accId "
         + "where p.Id = :Id")
   Optional<PhoneWithAccountProjection> findPhoneWithAccountById(@Param("Id") Long id);


   // This does the same as findTransferAccountsWithAccount except the result contains objects with a sensible toString.
   // Since it is a lot harder to maintain, eg. updates in mutliple places and new equals/hashcode for each new field,
   // it is probably not the best way to do it if the only reason is to have a useful toString.
   @Query("SELECT "
         + "new com.felixalacampagne.account.persistence.entities.PhoneWithAccountDTO("
         + "p as phoneAccount, a.accDesc as accDesc, a.accCode as accCode"
         + ") "
         + "FROM PhoneAccount p LEFT JOIN Account a ON  p.accountId = a.accId where p.accountId != :exaccid and p.order not in (255) "
         + "order by p.order DESC, p.desc ASC" )
   List<PhoneWithAccountDTO> findTransferAccountsWithAccountDTO(@Param("exaccid") Long excludeAccountId);

}
