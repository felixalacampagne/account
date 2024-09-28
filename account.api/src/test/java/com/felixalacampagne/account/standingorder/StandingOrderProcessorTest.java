package com.felixalacampagne.account.standingorder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.service.StandingOrderService;

@AccountTest
public class StandingOrderProcessorTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());
   @Autowired
   StandingOrderProcessor standingOrderProcessor;

   @Autowired
   StandingOrderService standingOrderService;

   @Test
   void processStandingOrderTest()
   {
      StandingOrders so = standingOrderService.getNextPendingStandingOrder().get();
      log.info("processStandingOrderTest: processing SO:{}", so);

      standingOrderProcessor.processStandingOrder(so);


   }

   @Test
   void processPendingStandingOrderTest()
   {
      log.info("processPendingStandingOrderTest: processing SOs");

      standingOrderProcessor.processStandingOrders();
   }


   @Test
   void expandSOmemoTest()
   {
      LocalDate now = LocalDate.of(2027, 06, 10);
      LocalDate before = now.minusDays(5);
      String memotmpl;
      String memo;

      memotmpl = "test #mm yyyy#";
      memo = standingOrderProcessor.expandSOmemo(memotmpl, now, before);
      assertEquals("test 06 2027", memo);

      memotmpl = "test #dd mm# test";
      memo = standingOrderProcessor.expandSOmemo(memotmpl, now, before);
      assertEquals("test 10 06 test", memo);

      memotmpl = "#yy mm dd# test";
      memo = standingOrderProcessor.expandSOmemo(memotmpl, now, before);
      assertEquals("27 06 10 test", memo);

      memotmpl = "#Eyy mm dd# test";
      memo = standingOrderProcessor.expandSOmemo(memotmpl, now, before);
      assertEquals("27 06 05 test", memo);   
   }
}
