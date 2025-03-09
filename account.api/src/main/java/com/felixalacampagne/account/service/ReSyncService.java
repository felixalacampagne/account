package com.felixalacampagne.account.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReSyncService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private final AccountService accountService;
   private final BalanceService balanceService;

   @Autowired
   public ReSyncService(BalanceService balanceService,
                        AccountService accountService) 
   {
      this.balanceService = balanceService;
      this.accountService = accountService;
   }
   
   @Async
   @EventListener(ApplicationReadyEvent.class)
   public void reSyncBalances()
   {
      log.info("reSyncBalances: start: accountJpaRepository:{} balanceService:{}",
            (this.accountService==null) ? "PROBLEM" : "GO",
            (this.balanceService==null) ? "PROBLEM" : "GO");
      
//      this.accountService.getAccountList()
//         .forEach( acc -> {
//            log.info("reSyncBalances: sync date sorted balance for account:{} {}", acc.getId(), acc.getName());
//            this.balanceService.calculateDatesortedBalances(acc.getId(), Optional.empty());
//            
//            log.info("reSyncBalances: sync sequence sorted balance for account:{} {}", acc.getId(), acc.getName());
//            this.balanceService.calculateBalances(acc.getId(), Optional.empty());
//   
//            log.info("reSyncBalances: sync checked balance for account:{} {}", acc.getId(), acc.getName());
//            this.balanceService.calculateCheckedBalances(acc.getId(), Optional.empty());
//         });
      log.info("reSyncBalances: done");
   }
}
