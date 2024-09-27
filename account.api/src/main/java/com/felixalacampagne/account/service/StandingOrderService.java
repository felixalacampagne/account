package com.felixalacampagne.account.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

      List<StandingOrders> sopending = standingOrdersJpaRepository.findBySOEntryDateLessThanOrderBySOEntryDateAsc(getCurrentEntryDate());
      return sopending;
   }

   public Optional<StandingOrders> getNextPendingStandingOrder()
   {
      return standingOrdersJpaRepository.findFirstBySOEntryDateLessThanOrderBySONextPayDateAsc(getCurrentEntryDate());
   }

   public Date getCurrentEntryDate()
   {
      LocalDate date = LocalDate.now();
      Timestamp ts = Timestamp.valueOf(date.atTime(23,59,59));
      return  Date.valueOf(date);
   }

   public StandingOrders update(StandingOrders updso)
   {
      return standingOrdersJpaRepository.save(updso);
   }
}
