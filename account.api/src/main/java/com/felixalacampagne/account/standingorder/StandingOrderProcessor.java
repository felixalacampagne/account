package com.felixalacampagne.account.standingorder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.service.ReSyncService;
import com.felixalacampagne.account.service.StandingOrderService;
import com.felixalacampagne.account.service.TransactionService;

@Component
public class StandingOrderProcessor
{
   Logger log = LoggerFactory.getLogger(this.getClass());
   private final DateTimeFormatter memoon = DateTimeFormatter.ofPattern("dd MMM yy");
   private final StandingOrderService standingOrderService;
   private final TransactionService transactionService;
   private final StandingOrderProcessingService standingOrderProcessingService;
   private final ReSyncService reSyncService;
   private final StandingOrderUtils soUtils = new StandingOrderUtils();
   
   @Autowired
   public StandingOrderProcessor(
         StandingOrderProcessingService standingOrderProcessingService,
         StandingOrderService standingOrderService,
         TransactionService transactionService,
         ReSyncService reSyncService)
   {
      this.standingOrderProcessingService = standingOrderProcessingService;
      this.standingOrderService = standingOrderService;
      this.transactionService = transactionService;
      this.reSyncService = reSyncService;
   }

   public void processStandingOrders()
   {
      // To keep the order of the transactions in the DB consistent the
      // processing will retrieve the single next SO to be processed,
      // it will be processed for one date transition and updated to the DB.
      // Then the next SO will be retrieved, which may or may not be the same SO,
      // and processed for one date transition, and so on until there are no
      // pending SOs.
      // This should mean that for a catch up where each SO might need to be
      // executed multiple times the order of execution in the DB should be correct
      // so there shouldnt be any spurious -ve balances calculated which could
      // occur otherwise.

      while(true)
      {
         Optional<StandingOrders> optso = standingOrderService.getNextPendingStandingOrder();
         if(optso.isEmpty())
         {
            log.info("processStandingOrders: no pending SOs");
            break;
         }
         StandingOrders nextso = optso.get();
         processStandingOrder(nextso);
      }

      // Could have done a per account balance calculation for each SO executed but simpler
      // just to recalculate everything, especially since this usually happens when I'mnot using it.
      // Might even be beneficial to do the sync on a regular basis to mitigate against any missed recalcs.
      this.reSyncService.reSyncBalances();
   }

   public void processStandingOrder(StandingOrders so)
   {

      log.info("processStandingOrder: processing {}", so);

      // Generate comment
      LocalDate txndate = so.getSONextPayDate();
      LocalDate entdate = so.getSOEntryDate();
      String memo = expandSOmemo(so.getSODesc(), txndate, entdate);
      memo = String.join(" ", memo, "On:" + memoon.format(LocalDate.now()));

      BigDecimal soamt = so.getSOAmount();
      Long accId = so.getAccount().getAccId();



      // create new transaction
      Transaction sotxn = new Transaction();
      sotxn.setAccount(so.getAccount());

      sotxn.setDate(so.getSONextPayDate());
      sotxn.setType(so.getSOTfrType());
      sotxn.setComment(memo);
      if (soamt.signum() < 0)
      {
         sotxn.setCredit(soamt.abs());
      }
      else
      {
         sotxn.setDebit(soamt);
      }
      adjustSODates(so);

      // needs to be in same transaction and there is potentially an issue
      // with @Transactional when the method is called by another method
      // in the same class, ie. processStandingOrders -> processStandingOrder
      standingOrderProcessingService.updateTxnAndSo(so, sotxn);
   }

   
   public String expandSOmemo(String memo, LocalDate txndate, LocalDate entdate)
   {
      return soUtils.expandSOmemo(memo, txndate, entdate);
   }

   private void adjustSODates(StandingOrders so)
   {

      TemporalUnit periodUnit;
      switch(so.getSOPeriod())
      {
      case "W":
         periodUnit = ChronoUnit.WEEKS;
         break;
      case "D":
         periodUnit = ChronoUnit.DAYS;
         break;
      case "M":
         periodUnit = ChronoUnit.MONTHS;
         break;
      default:
         log.warn("adjustSODates: unrecognised standing order period value: {}", so.getSOPeriod());
         periodUnit = ChronoUnit.MONTHS;
         break;
      }

      // Dates on or after 28th will eventually become end-of-month since 28th is the EOM for February usually.
      // To avoid EOM adjustment use dates before 28th.
      // Might eventually need to add a flag to indicate whether this should be done but don't want to modify
      // DB until it is converted to a non-Access db.
      // Both entry and pay dates have End Of Month adjustments applied for monthly adjustments.
      // Not sure whether that is appropriate, time will tell.
      // Currently 2 entries are in the 'danger zone', pay date and bank charge, both can be moved out of the zone.
      so.setSOEntryDate(adjustDate(so.getSOEntryDate(), periodUnit, so.getSOCount()));
      so.setSONextPayDate(adjustDate(so.getSONextPayDate(), periodUnit, so.getSOCount()));
   }

   private LocalDate adjustDate(LocalDate origdate, TemporalUnit periodUnit, long numperiods)
   {
      return soUtils.adjustDate(origdate, periodUnit, numperiods);
   }
}
