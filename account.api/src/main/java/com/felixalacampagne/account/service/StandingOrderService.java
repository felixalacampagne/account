package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.StandingOrderItem;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.entities.StandingOrders;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.StandingOrdersJpaRepository;

@Service
public class StandingOrderService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   final StandingOrdersJpaRepository standingOrdersJpaRepository;
   final AccountJpaRepository accountJpaRepository;
//   final ObjectMapper localdateJsonMapper;

   @Autowired // Not sure this is required but it helps to remind where the object comes from
   public StandingOrderService(StandingOrdersJpaRepository standingOrdersJpaRepository,
//                               ObjectMapper localdateJsonMapper,
                               AccountJpaRepository accountJpaRepository)
   {
      this.standingOrdersJpaRepository = standingOrdersJpaRepository;
      this.accountJpaRepository = accountJpaRepository;
//      this.localdateJsonMapper = localdateJsonMapper;
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
      return  date;
   }


   public void addStandingOrderItem(StandingOrderItem standingOrderItem)
   {
      StandingOrders so = mapToEntity(standingOrderItem);
      add(so);
   }

   public void updateStandingOrderItem(StandingOrderItem standingOrderItem)
   {
      StandingOrders updso = mapToEntity(standingOrderItem);
      StandingOrders origso = standingOrdersJpaRepository.findById(updso.getSOid())
                                 .orElseThrow(() -> new AccountException("StandingOrder not found: id:" + updso.getSOid()));

      String origToken = Utils.getToken(origso);
      if(!origToken.equals(standingOrderItem.getToken()))
      {
         log.info("updateStandingOrderItem: Token mismatch for StandingOrder id:{}: original:{} supplied:{}",
               standingOrderItem.getSOId(), origToken, standingOrderItem.getToken());
         throw new  AccountException("Token does not match StandingOrder id " + standingOrderItem.getSOId());

      }

      // Going to assume that ALL fields are present in updso so it can replace the origso... it may be
      // required to copy the fields from updso into origso by the framework
      update(updso);
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

      Account acc = accountJpaRepository.findById(standingOrderItem.getAccountid())
            .orElseThrow(() -> new AccountException("Account not found: id:" + standingOrderItem.getAccountid()));

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
      tosave.setAccount(acc);
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
