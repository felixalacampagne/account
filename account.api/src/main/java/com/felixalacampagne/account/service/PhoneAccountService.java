package com.felixalacampagne.account.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.TransferAccountItem;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.entities.PhoneAccount;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;

@Service
public class PhoneAccountService
{
private final Logger log = LoggerFactory.getLogger(this.getClass());
private final PhoneAccountJpaRepository phoneAccountJpaRepository;
private final AccountJpaRepository accountJpaRepository;
   public PhoneAccountService(PhoneAccountJpaRepository phoneAccountJpaRepository,
		   AccountJpaRepository accountJpaRepository)
   {
      this.phoneAccountJpaRepository = phoneAccountJpaRepository;
      this.accountJpaRepository = accountJpaRepository;
   }

   public List<TransferAccountItem> getPhoneAccounts()
   {
      Sort sort = Sort.by(Sort.Direction.DESC, "order");
      return phoneAccountJpaRepository.findAll(Sort.by(List.of (
                                                      new Sort.Order(Sort.Direction.DESC, "order"),
                                                      new Sort.Order(Sort.Direction.ASC, "desc"))))
            .stream()
            .map(a -> {
               return new TransferAccountItem(a.getId(),
                     a.getAccId(),
                     a.getDesc(),
                     a.getAccountNumber(),
                     a.getLastComm(),
                     a.getOrder(),
                     a.getType(),
                     a.getAccDesc(),
                     Utils.getToken(a));
            })
            .collect(Collectors.toList());
   }


   public void deleteTransferAccount(TransferAccountItem phoneAccountItem)
   {
      log.info("deleteTransferAccount: phoneAccountItem:{}", phoneAccountItem);
      if(phoneAccountItem == null)
         return;
      PhoneAccount phoneAccount = phoneAccountJpaRepository.findById(phoneAccountItem.getId())
            .orElseThrow(()->new AccountException("PhoneAccount id " + phoneAccountItem.getId() + " not found"));

      String origToken = Utils.getToken(phoneAccount);
      if(!origToken.equals(phoneAccountItem.getToken()))
      {
         log.info("deleteTransferAccount: Token mismatch for PhoneAccount id:{}: original:{} supplied:{}",
               phoneAccountItem.getId(), origToken, phoneAccountItem.getToken());
         throw new  AccountException("Token does not match PhoneAccount id " + phoneAccountItem.getId());
      }
      this.phoneAccountJpaRepository.delete(phoneAccount);
   }


   public void updateTransferAccount(TransferAccountItem phoneAccountItem)
   {
      log.info("updateTransferAccount: phoneAccountItem:{}", phoneAccountItem);
      if(phoneAccountItem == null)
         return;
      PhoneAccount phoneAccount = phoneAccountJpaRepository.findById(phoneAccountItem.getId())
            .orElseThrow(()->new AccountException("PhoneAccount id " + phoneAccountItem.getId() + " not found"));

      String origToken = Utils.getToken(phoneAccount);
      if(!origToken.equals(phoneAccountItem.getToken()))
      {
         log.info("updateTransferAccount: Token mismatch for PhoneAccount id:{}: original:{} supplied:{}",
               phoneAccountItem.getId(), origToken, phoneAccountItem.getToken());
         throw new  AccountException("Token does not match PhoneAccount id " + phoneAccountItem.getId());
      }

      phoneAccount = mapToEntity(phoneAccountItem, phoneAccount);
      this.phoneAccountJpaRepository.saveAndFlush(phoneAccount);
   }


   public void addTransferAccount(TransferAccountItem phoneAccountItem)
   {
      PhoneAccount phoneAccount = mapToEntity(phoneAccountItem);
      this.phoneAccountJpaRepository.saveAndFlush(phoneAccount);
   }


   private PhoneAccount mapToEntity(TransferAccountItem phoneAccountItem)
   {
      PhoneAccount pa = new PhoneAccount();
      return mapToEntity(phoneAccountItem, pa);
   }

   // Use this for updates as still not sure whether saving 'new' object with existing id is 'recommended'
   // If the id of the entity is already set then it will NOT be overridden by a value in item
   private PhoneAccount mapToEntity(TransferAccountItem phoneAccountItem, PhoneAccount pa)
   {
      if((pa.getId() == null) || (pa.getId() < 1))
      {
         pa.setId(phoneAccountItem.getId());
      }

      if(phoneAccountItem.getRelatedAccountId() > 0)
      {
         Account relacc = this.accountJpaRepository.findById(phoneAccountItem.getRelatedAccountId())
       		  .orElseThrow(() -> new  AccountException("No Account for id " + phoneAccountItem.getRelatedAccountId()));
         pa.setAccount(relacc);
      }

      pa.setAccountNumber(phoneAccountItem.getCptyAccountNumber());
      pa.setDesc(phoneAccountItem.getCptyAccountName());
      pa.setLastComm(phoneAccountItem.getLastCommunication());
      pa.setOrder(phoneAccountItem.getOrder());
      pa.setType(phoneAccountItem.getType());  // map string to type char?
      return pa;
   }
}
