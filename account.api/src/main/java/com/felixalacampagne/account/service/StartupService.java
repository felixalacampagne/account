package com.felixalacampagne.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// Seems only way to block the EventListener from running is with a profile. Don't want to
// use 'test' as it is too generic and might result in unwanted use of test.properties.
// Seems the profile only works when it is at class level - but then the class cannot
// be autowired elsewhere, eg. ReSyncService is used by StandingOrderProcessor. So only
// choice is to put the EventListener method in a dedicated class which is not
// referenced directly by the account code.
// NB This is only really relevant for the H2 database creation test... at the moment
@Profile("!noinittest")
@Component
public class StartupService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final ReSyncService reSyncService;

   public StartupService(ReSyncService reSyncService)
   {
      this.reSyncService = reSyncService;
   }


   @Async
   @EventListener(ApplicationReadyEvent.class)
   protected void reSyncBalancesOnStartup()
   {
      log.info("reSyncBalancesOnStartup: start");
      this.reSyncService.reSyncBalances();
      log.info("reSyncBalancesOnStartup: done");
   }

}
