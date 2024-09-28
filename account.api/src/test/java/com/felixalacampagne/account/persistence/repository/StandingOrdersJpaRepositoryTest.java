package com.felixalacampagne.account.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.felixalacampagne.account.AccountTest;
import com.felixalacampagne.account.persistence.entities.StandingOrders;

@AccountTest
class StandingOrdersJpaRepositoryTest
{
   Logger log = LoggerFactory.getLogger(this.getClass());

@Autowired
StandingOrdersJpaRepository standingOrdersJpaRepository;

   @BeforeEach
   void setUp() throws Exception
   {
   }

   @Test
   void testFind()
   {
      List<StandingOrders> soall = standingOrdersJpaRepository.findAll();
      log.info("testFindAll: these are the StandingOrders:\n{}", soall);

      LocalDate date = LocalDate.of(2018,03,01);
      Date ts = Date.valueOf(date);
      List<StandingOrders> soallpending = standingOrdersJpaRepository.findBySOEntryDateLessThanOrderBySOEntryDateAsc(date);
      log.info("testFindAll: these are the pending StandingOrders:\n{}", soallpending);


      Optional<StandingOrders> optsopending = standingOrdersJpaRepository.findFirstBySOEntryDateLessThanOrderBySONextPayDateAsc(date);
      assertTrue(optsopending.isPresent());

      StandingOrders sopending = optsopending.get();
      log.info("testFindAll: these are the pending StandingOrders:\n{}", sopending);
      log.info("testFindAll: pending StandingOrders is for Account:\n{}", sopending.getAccount());

   }

   @Test
   void testSave()
   {
      fail("Not yet implemented");
   }

}
