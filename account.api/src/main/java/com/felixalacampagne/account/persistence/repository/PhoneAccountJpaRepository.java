package com.felixalacampagne.account.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.felixalacampagne.account.persistence.entities.PhoneAccount;

@Repository
public interface PhoneAccountJpaRepository extends JpaRepository<PhoneAccount, Long>
{

   // NB. if accountId is null then 'p.accountId != :exaccid' is false so nulls are also excluded.
   // the accountId is 0 for entries without an entry in Accounts
   // PhoneAccounts with a null Account are excluded even though there is no Account.id to be compared to the excluded id
   // null accounts must be explicitly allowed
   @Query("SELECT p FROM PhoneAccount p where ((p.account is null) or (p.account.id != :exaccid)) and p.order not in (255) "
        + "order by p.order DESC, p.desc ASC" )
   List<PhoneAccount> findTransferAccounts(@Param("exaccid") Long excludeAccountId);

   long deleteByAccountId(Long accId);

}
