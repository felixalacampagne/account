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

   // NB. is accountId is null then 'p.accountId != :exaccid' is false!
   @Query("SELECT p FROM PhoneAccount p where p.accountId != :exaccid and p.order not in (255) "
        + "order by p.order DESC, p.desc ASC" )
   List<PhoneAccount> findTransferAccounts(@Param("exaccid") Long excludeAccountId);
}
