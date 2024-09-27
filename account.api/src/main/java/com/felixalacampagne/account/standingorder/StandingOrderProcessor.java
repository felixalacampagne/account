package com.felixalacampagne.account.standingorder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
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
import com.felixalacampagne.account.service.StandingOrderService;
import com.felixalacampagne.account.service.TransactionService;

@Component
public class StandingOrderProcessor
{
   Logger log = LoggerFactory.getLogger(this.getClass());

   private final StandingOrderService standingOrderService;
   private final TransactionService transactionService;
   private final StandingOrderProcessingService standingOrderProcessingService;

   @Autowired
   public StandingOrderProcessor(
         StandingOrderProcessingService standingOrderProcessingService,
         StandingOrderService standingOrderService,
         TransactionService transactionService)
   {
      this.standingOrderProcessingService = standingOrderProcessingService;
      this.standingOrderService = standingOrderService;
      this.transactionService = transactionService;
   }

   public void processStandingOrders()
   {
      // To keep the order of the transactions in the DB consistent the
      // processing will retrieve the single next SO to be processed,
      // it will be processed for one date transition and updated to the DB.
      // Then the next SO will be retrieved, which may or may not be the same SO,
      // and processed for one date transition, and so on until there are no
      // pending SOs.
      // This should mean that for a catch up the where each SO might need to be
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
   }

   public void processStandingOrder(StandingOrders so)
   {
      BigDecimal balance = BigDecimal.ZERO;
      LocalDate now = LocalDate.now();
      // Following processing must be in a single transaction
      log.info("processStandingOrder: processing {}", so);

      // Generate comment
      String memo = expandSOmemo(so.getSODesc(), now);
      BigDecimal soamt = so.getSOAmount();
      // Get last transaction
      Optional<Transaction> lasttxn = this.transactionService.getLatestTransaction(so.getAccount().getAccId());

      // Calculate next balance
      if(lasttxn.isPresent() && (lasttxn.get().getBalance() != null))
      {
         balance = lasttxn.get().getBalance();
      }
      balance = balance.subtract(soamt);
      // create new transaction
      Transaction sotxn = new Transaction();

      sotxn.setDate(new Timestamp(new java.util.Date().getTime()));
      sotxn.setType(so.getSOTfrType());
      sotxn.setComment(memo);
      if (soamt.signum() < 0)
      {
         sotxn.setDebit(soamt);
      }
      else
      {
         sotxn.setCredit(soamt);
      }
      sotxn.setBalance(balance);

      adjustSODates(so);

      // needs to be in same transaction and there is potentially an issue
      // with @Transactional when the method is called by another method
      // in the same class, ie. processStandingOrders -> processStandingOrder
      standingOrderProcessingService.updateTxnAndSo(so, sotxn);
   }

   public String expandSOmemo(String memo, LocalDate now)
   {
      int i;
      int e;
      String srtn = memo;
      String sl;
      String sr;
      String pat;

      do
      {
         i = srtn.indexOf('#');
         if(i < 0)
         {
            break;
         }
         e = srtn.indexOf('#', i+1);
         if(e < 0)
         {
            break;
         }

         sl = "";
         sr = "";

         if(i > 0)
            sl = srtn.substring(0, i);

         if(e < srtn.length())
            sr = srtn.substring(e+1, srtn.length());

         pat = srtn.substring(i+1, e);
         // pat should now be a VB date format - unfortunately this is not the same as a Java date format
         // There are only a few formats actually used and the main difference is for month, ie. m vs. M for java
         pat = pat.replace('m', 'M');
         DateTimeFormatter df = DateTimeFormatter.ofPattern(pat);

         srtn = sl + df.format(now) + sr;
      }while(true);

      return srtn;
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

      so.setSOEntryDate(adjustDate(so.getSOEntryDate(), periodUnit, so.getSOCount()));
      so.setSONextPayDate(adjustDate(so.getSONextPayDate(), periodUnit, so.getSOCount()));
   }

   private Date adjustDate(Date origdate, TemporalUnit periodUnit, long numperiods)
   {
      LocalDate adjustdate = convertToLocalDate(origdate);
      adjustdate = adjustdate.plus(numperiods, periodUnit);
      return Date.valueOf(adjustdate);
   }

   private LocalDate convertToLocalDate(Date date)
   {
      log.trace("convertToLocalDate: date:{}", date);
      LocalDate utcld =  date.toLocalDate();
      log.trace("convertToLocalDate: localdate:{}", utcld);
      return utcld;
   }
}
