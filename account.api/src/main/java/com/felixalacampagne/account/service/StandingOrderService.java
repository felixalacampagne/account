package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.StandingOrderItem;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;

@Service
public class StandingOrderService
{
   final StandingOrdersJpaRepository standingOrdersJpaRepository;
   final ObjectMapper localdateJsonMapper;

   @Autowired // Not sure this is required but it helps to remind where the object comes from
   public StandingOrderService(StandingOrdersJpaRepository standingOrdersJpaRepository,
                               ObjectMapper localdateJsonMapper)
   {
      this.standingOrdersJpaRepository = standingOrdersJpaRepository;
      this.localdateJsonMapper = localdateJsonMapper;
   }

   public List<StandingOrders> getPendingStandingOrders()
   {

      List<StandingOrders> sopending = standingOrdersJpaRepository.findBySOEntryDateLessThanEqualOrderBySOEntryDateAsc(getCurrentEntryDate());
      return sopending;
   }

   public Optional<StandingOrders> getNextPendingStandingOrder()
   {
      return standingOrdersJpaRepository.findFirstBySOEntryDateLessThanEqualOrderBySONextPayDateAsc(getCurrentEntryDate());
   }

   public LocalDate getCurrentEntryDate()
   {
      LocalDate date = LocalDate.now();
      Timestamp ts = Timestamp.valueOf(date.atTime(23,59,59));
      return  date; //Date.valueOf(date);
   }

   
   public void addStandingOrderItem(StandingOrderItem standingOrderItem)
   {
      StandingOrders so = mapToEntity(standingOrderItem);
      add(so);
   }

   public void updateStandingOrderItem(StandingOrderItem standingOrderItem)
   {
      StandingOrders so = mapToEntity(standingOrderItem);
      // TODO Validate the token, copy non-null/empty fields to the original from the update
      update(so);
   }   
   public StandingOrders add(StandingOrders newso)
   {
      return standingOrdersJpaRepository.saveAndFlush(newso);
   }
   
   public StandingOrders update(StandingOrders updso)
   {
      return standingOrdersJpaRepository.saveAndFlush(updso);
   }

   private StandingOrders mapToEntity(StandingOrderItem standingOrderItem)
   {
      StandingOrders tosave = new StandingOrders();
      BigDecimal amount = new BigDecimal(standingOrderItem.getSOAmount());
      
      amount.setScale(2); // Max. two decimal places for a normal currency transaction

      if(standingOrderItem.getSOId() > 0)
      {
         tosave.setSOid(standingOrderItem.getSOId());
      }
     
      tosave.setSOAmount(amount);
      tosave.setSOCount(standingOrderItem.getSOCount());
      tosave.setSODesc( standingOrderItem.getSODesc());
      tosave.setSOEntryDate( standingOrderItem.getSOEntrydate());
      tosave.setSONextPayDate( standingOrderItem.getSONextpaydate());
      tosave.setSOPeriod( standingOrderItem.getSOPeriod());
      tosave.setSOTfrType( standingOrderItem.getSOTfrtype());
      
      // TODO: How to handle the foreign key - really only the value of SOAccId is needed
      // but that isn't a field at the moment.... should/can it be??
      Account acc = new Account();
      acc.setAccId(standingOrderItem.getAccountid());
      tosave.setAccount(acc);
//      this.token = token;
//      this.accountid = accountid;
//      this.accountname = accountname;
      return tosave;
   }

   public StandingOrderItem mapToItem(StandingOrders t)
   {
      // jackson doesn't handle Java dates and bigdecimal has too many decimal places so it's
      // simpler just to send the data as Strings with the desired formating.
      String token = Utils.getToken(t);
      return new StandingOrderItem(t.getSOid(),
            Utils.formatAmount(t.getSOAmount()),
            t.getSOCount(),
            t.getSODesc(),
            t.getSOEntryDate(),
            t.getSONextPayDate(),
            t.getSOPeriod(),
            t.getSOTfrType(),
            token,
            t.getAccount().getAccId(),
            t.getAccount().getAccDesc());

   }

   public List<StandingOrderItem> getStandingOrderItems()
   {
      List<StandingOrderItem> sois = this.standingOrdersJpaRepository
            .findAll(Sort.by("SOEntryDate").ascending()).stream()
               .map(t -> mapToItem(t))
               .collect(Collectors.toList());
      return sois;
   }
   
   public StandingOrderItem getStandingOrderItem(long id)
   {
      return this.standingOrdersJpaRepository.findById(id)
                                      .map(s -> mapToItem(s))
                                      .orElseThrow(() -> new AccountException("Standing order not found: id " + id));
                                      
   }

}
