package com.felixalacampagne.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;

@Service
public class ConnectionResurrector
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final AccountJpaRepository accountJpaRepository;
   private final int MAX_ATTEMPTS=10;
   
   
   // This should take an argument which is the repository type which the service is using
   // but not sure how to express the templates....
   @Autowired
   public ConnectionResurrector(AccountJpaRepository accountJpaRepository) 
   {
      this.accountJpaRepository = accountJpaRepository;
   }
   
   
   // Have not been able to find anything to prevent the connections from being closed after 
   // a certain period and Hikari is no help since it seems to happily give out useless closed connections.
   // I discovered that after refreshing the account page 10 or so times the connections started to
   // work again.
   // In desperation this kludge is an attempt to flush out the dead connections so the real DB calls
   // have a working connection again. Hopefully if I ever manage to switch to a different database
   // it will no longer be required.
   //
   // Unfortunately there is nothing to say that after successfully reading the DB here the
   // next call immediately after wont give 'connection closed'. So this is just a start....
   public boolean ressurectConnection() 
   {
      boolean rc = false;
      int attempt;
      for(attempt=1; attempt <= MAX_ATTEMPTS; attempt++)
      {
         try
         {
            accountJpaRepository.count();
            log.info("ressurectConnection: attempt {} successful: {}", attempt);
            rc = true;
            break;
         }
         catch(DataAccessResourceFailureException ex)
         {
            log.info("ressurectConnection: attempt {} failed: {}", attempt, ex.toString());
         }
      }
      return rc;
      
   }
}
