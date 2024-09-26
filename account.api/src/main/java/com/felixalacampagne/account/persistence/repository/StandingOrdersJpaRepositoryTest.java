package com.felixalacampagne.account.persistence.repository;

import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

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
      Timestamp ts = Timestamp.valueOf(date.atTime(23,59,59));
      List<StandingOrders> sopending = standingOrdersJpaRepository.findBySOEntryDateLessThanOrderBySOEntryDateAsc(ts);
      log.info("testFindAll: these are the pending StandingOrders:\n{}", sopending);
   }

   @Test
   void testSave()
   {
      fail("Not yet implemented");
   }

}
