package com.felixalacampagne.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.model.StandingOrderItem;
import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;

@AccountTest
public class StandingOrderServiceTest
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   @Autowired
   StandingOrderService standingOrderService;

   @Autowired
   StandingOrdersJpaRepository standingOrdersJpaRepository;

   @Test
   void addItemTest()
   {
      StandingOrderItem soitem = new StandingOrderItem(
            -1L,           //Long sOid,
            "99.99",       // String sOAmount,
            1L,            // Long sOCount,
            "test add standing order", // String sODesc,
            LocalDate.of(2050,11,1), // LocalDate sOEntryDate,
            LocalDate.of(2050,11,7), // LocalDate sONextPayDate,
            "M",           // String sOPeriod,
            "TEST",        // String sOTfrType,
            "",            //String token,
            1L,            // Long accountid,
            ""             // String accountname
            );

      standingOrderService.addStandingOrderItem(soitem);

      List<StandingOrders> allso = standingOrdersJpaRepository.findAll(Sort.by("SOid").descending());
      assertFalse(allso.isEmpty());
      assertEquals(soitem.getSODesc(), allso.get(0).getSODesc());
      log.info("addItemTest: added StandingOrder: {}", allso.get(0));
   }

   @Test
   void updateItemTest()
   {
      List<StandingOrders> allso = standingOrdersJpaRepository.findAll(Sort.by("SOid").descending());
      StandingOrders origso = allso.get(5);

      StandingOrderItem origitm = standingOrderService.mapToItem(origso);
      log.info("updateItemTest: original StandingOrder: {}", origso);
      StandingOrderItem soitem = new StandingOrderItem(
            origitm.getSOId(),           //Long sOid,
            "9999.99",       // String sOAmount,
            origitm.getSOCount() + 1,            // Long sOCount,
            "TEST:" + origitm.getSODesc() + ":TEST", // String sODesc,
            origitm.getSOEntrydate(), // LocalDate sOEntryDate,
            origitm.getSONextpaydate().plusYears(10), // LocalDate sONextPayDate,
            origitm.getSOPeriod(),           // String sOPeriod,
            origitm.getSOTfrtype(),        // String sOTfrType,
            origitm.getToken(),            //String token,
            1L,            // Long accountid,
            ""             // String accountname
            );
      standingOrderService.updateStandingOrderItem(soitem);
      StandingOrders updso = standingOrdersJpaRepository.findById(origso.getSOid()).get();
      log.info("updateItemTest: updated StandingOrder: {}", updso);

   }
}
