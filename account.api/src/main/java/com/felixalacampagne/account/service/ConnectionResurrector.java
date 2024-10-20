package com.felixalacampagne.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.jpa.repository.JpaRepository;


public class ConnectionResurrector<R extends JpaRepository<?, ?>>
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final R repository;
   private final String id;
   private final int MAX_ATTEMPTS=10;
   
   // NB id should be the class name of R. Using R as the class name gives an unhelpful jpa proxy name,
   // eg. jdk.proxy2.$Proxy119>, which is not helpful for logging. Perhaps I should make the parameter
   // the actual class since it has proven frustrating that the class is not accessible from a template...
   public ConnectionResurrector(R repository, Class<?> repoClass) 
   {
      this.repository = repository;
      this.id = repoClass.getSimpleName();
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
            long count = repository.count();
            log.info("ressurectConnection<{}>: attempt {} successful: count={}", this.id, attempt, count);
            rc = true;
            break;
         }
         catch(DataAccessResourceFailureException ex)
         {
            log.info("ressurectConnection<{}>: attempt {} failed: {}", this.id, attempt, ex.toString());
         }
         catch(Exception ex)
         {
            log.info("ressurectConnection<{}>: attempt {} failed: {}", this.id, attempt, ex.toString());
            throw ex;
         }         
      }
      return rc;
      
   }
}
