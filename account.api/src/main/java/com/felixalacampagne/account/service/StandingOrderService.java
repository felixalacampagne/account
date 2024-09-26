package com.felixalacampagne.account.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;

@Service
public class StandingOrderService
{
   final StandingOrdersJpaRepository standingOrdersJpaRepository;

   @Autowired // Not sure this is required but it helps to remind where the object comes from
   public StandingOrderService(StandingOrdersJpaRepository standingOrdersJpaRepository)
   {
      this.standingOrdersJpaRepository = standingOrdersJpaRepository;
   }

   public List<StandingOrders> getPendingStandingOrders()
   {
      LocalDate date = LocalDate.now();

      // Get any SOs due today
      Timestamp ts = Timestamp.valueOf(date.atTime(23,59,59));
      List<StandingOrders> sopending = standingOrdersJpaRepository.findBySOEntryDateLessThanOrderBySOEntryDateAsc(ts);
      return sopending;
   }
}
