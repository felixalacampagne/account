package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.felixalacampagne.account.common.Utils;
import com.felixalacampagne.account.model.AddTransactionItem;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.persistence.entities.Account;
import com.felixalacampagne.account.persistence.entities.PhoneAccount;
import com.felixalacampagne.account.persistence.entities.PhoneWithAccountProjection;
import com.felixalacampagne.account.persistence.entities.Transaction;
import com.felixalacampagne.account.persistence.repository.AccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.PhoneAccountJpaRepository;
import com.felixalacampagne.account.persistence.repository.TransactionJpaRepository;

@Service
public class TransactionService
{
   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private final TransactionJpaRepository transactionJpaRepository;
   private final AccountJpaRepository accountJpaRepository;
   private final PhoneAccountJpaRepository phoneAccountJpaRepository;
   private final ConnectionResurrector<TransactionJpaRepository> connectionResurrector;

   BalanceService balanceService;

   @Autowired
   public TransactionService(TransactionJpaRepository transactionJpaRepository,
                             BalanceService balanceService,
                             AccountJpaRepository accountJpaRepository,
                             PhoneAccountJpaRepository phoneAccountJpaRepository
                             ) {
      this.transactionJpaRepository = transactionJpaRepository;
      this.balanceService = balanceService;
      this.accountJpaRepository = accountJpaRepository;
      this.phoneAccountJpaRepository = phoneAccountJpaRepository;
      this.connectionResurrector = new ConnectionResurrector<TransactionJpaRepository>(transactionJpaRepository, TransactionJpaRepository.class);
   }

   public Transactions getTransactions(long accountId, int page)
   {
      if(page < 0)
      {
         page = 0;
      }
      return getTransactions(getTransactionPage(page, 15, accountId), BalanceType.NORMAL);
   }

   public Transactions getTransactions(List<Transaction> txns, BalanceType balanceType)
   {

      List<TransactionItem> txnitems = txns.stream()
            .map(t -> mapToItem(t, balanceType))
            .collect(Collectors.toList());
      Transactions trns = new Transactions(txnitems);
      return trns;
   }


   public List<Transaction> getTransactionPage(int page, int rows, long accountId)
   {
      connectionResurrector.ressurectConnection();
      List<Transaction> txns = transactionJpaRepository.
            findByAccountId(accountId,  PageRequest.of(page, rows, Sort.by("sequence").descending())).stream()
            .sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      return txns;
   }

   public Optional<Transaction> getTransaction(long id)
   {
      connectionResurrector.ressurectConnection();
      return transactionJpaRepository.findById(id);
   }

   @Transactional
   public void addPhoneAccountTransaction(Long phoneAccId, Transaction srcTxn, String communication)
   {
      Long srcaccid = srcTxn.getAccountId();
      PhoneWithAccountProjection paproj = this.phoneAccountJpaRepository.findPhoneWithAccountById(phoneAccId)
                                              .orElseThrow(() -> new AccountException("Phone account not found: " + phoneAccId));
      PhoneAccount pa = paproj.getPhoneAccount();
      if(pa.getAccountId() > 0) // transfer is to a related account so must apply a 'reverse' transaction to it
      {
         Long tfraccid = pa.getAccountId();
         String srcupdcomm = Utils.prefixNullable(" ref:", communication);
         Account srcacc = this.accountJpaRepository.findById(srcaccid)
               .orElseThrow(() -> new AccountException("Transfer source account not found: " + srcaccid));
         Transaction txntfr = new Transaction();
         txntfr.setAccountId(tfraccid);
         txntfr.setDate(srcTxn.getDate());
         txntfr.setType(srcTxn.getType());
         txntfr.setComment(srcTxn.getComment() + Utils.prefixNullable(" ref:", communication));

         if(srcTxn.getCredit() == null)
         {
            // Debit on src acc -> tfr FROM srcacc
            txntfr.setCredit(srcTxn.getDebit());
            txntfr.setDebit(null);
            txntfr.setComment(txntfr.getComment() + " from:" + srcacc.getAccDesc());
            srcupdcomm = srcupdcomm + " to:" + paproj.getAccDesc();
         }
         else
         {
            // Credit on src acc -> tfr TO srcacc
            txntfr.setDebit(srcTxn.getCredit());
            txntfr.setCredit(null);
            txntfr.setComment(txntfr.getComment() + " to:" + srcacc.getAccDesc());
            srcupdcomm = srcupdcomm + " from:" + paproj.getAccDesc();
         }

         txntfr = add(txntfr);

         if(!srcupdcomm.isBlank())
         {
            srcTxn.setComment(srcTxn.getComment() + srcupdcomm);
            srcTxn = this.transactionJpaRepository.save(srcTxn);
         }
         log.info("addTransaction: added transfer transaction for account id {}: id:{}", txntfr.getAccountId(), txntfr.getSequence());
      }
      else
      {
         String updcomm = "";
         updcomm = updcomm + Utils.prefixNullable(" ref:", communication);
         updcomm = updcomm + Utils.prefixNullable(" to:", pa.getAccountNumber());
         updcomm = updcomm + Utils.prefixNullable(" ", pa.getDesc());
         if(!updcomm.isBlank())
         {
            srcTxn.setComment(srcTxn.getComment() + updcomm);
            srcTxn = this.transactionJpaRepository.save(srcTxn);
         }
      }

      if(communication!=null && !communication.isBlank())
      {
         pa.setLastComm(communication);
         pa = this.phoneAccountJpaRepository.save(pa);
      }
   }

   @Transactional // potentially two transactions are added so transaction is required
   public void addTransaction(AddTransactionItem transactionItem)
   {
      connectionResurrector.ressurectConnection();
      Transaction txn = mapToEntity(transactionItem);
      Transaction updtxn = add(txn);
      log.info("addTransaction: added transaction for account id {}: id:{}", updtxn.getAccountId(), updtxn.getSequence());
      if(transactionItem.getTransferAccount().isPresent())
      {
         addPhoneAccountTransaction(transactionItem.getTransferAccount().get(), updtxn, transactionItem.getCommunication());
      }
      else if( !( Utils.fromNullable(transactionItem.getCptyAccount()).isBlank()
               && Utils.fromNullable(transactionItem.getCptyAccountNumber()).isBlank()
               && Utils.fromNullable(transactionItem.getCommunication()).isBlank() ) )
      {
         String updcomm = "";
         updcomm = updcomm + " ref:" + transactionItem.getCommunication();
         updcomm = updcomm + " to:" + transactionItem.getCptyAccountNumber();
         updcomm = updcomm + " " + transactionItem.getCptyAccount();
         updtxn.setComment(updtxn.getComment() + updcomm);
         updtxn = this.transactionJpaRepository.save(updtxn);

         // Add the new account to PhoneAccount
         addPhoneAccount(transactionItem.getCptyAccountNumber(), transactionItem.getCptyAccount(), transactionItem.getCommunication());

      }
   }


   private void addPhoneAccount(String cptyAccountNumber, String cptyAccount, String communication)
   {
      PhoneAccount pa = new PhoneAccount();
      pa.setAccountId(0L); // leaving it null means it is excluded from the transferaccount query
      pa.setAccountNumber(cptyAccountNumber);
      pa.setDesc(cptyAccount);
      pa.setLastComm(communication);
      pa.setOrder(9);    // This seems to be the value set for most 'active' accounts
      pa.setType("O");
      pa = this.phoneAccountJpaRepository.save(pa);
      log.info("addPhoneAccount: added PhoneAccount: {}", pa);
   }

   // This must be @Transactional because it calls 'update()' which is @Transactional and in the same class which
   // means the Spring @Transactional proxies would be bypassed - I think
   @Transactional
   public Transaction updateTransaction(TransactionItem transactionItem)
   {
      log.info("updateTransaction: transactionItem:{}", transactionItem);
      if(transactionItem == null)
         return null;
      Transaction txn = getTransaction(transactionItem.getId())
            .orElseThrow(()->new AccountException("Transaction id " + transactionItem.getId() + " not found"));


      String origToken = Utils.getToken(txn);
      if(!origToken.equals(transactionItem.getToken()))
      {
         log.info("updateTransaction: Token mismatch for transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), origToken, transactionItem.getToken());
         throw new  AccountException("Token does not match Transaction id " + transactionItem.getId());
      }

      Transaction updtxn = mapToEntity(transactionItem);
      if(txn.getAccountId() != updtxn.getAccountId())
      {
         log.info("updateTransaction: Account id does not match transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), txn.getAccountId(), updtxn.getAccountId());
         throw new  AccountException("Account id does not match Transaction id " + transactionItem.getId());
      }

      // Not allowing any updates when checked is a bit extreme. The VB app allowed the checked flag to be
      // cleared, a value updated, and then checked again which was a behaviour I have used on many occasions.
      // Not so easy to implement the same thing now but I
      // still want to ability to update checked entries if required.
      // Thus if the update has the checked flag cleared then allow the update.
      if(txn.getChecked() && updtxn.getChecked())
      {
         log.info("updateTransaction: Locked transaction: id:{}", transactionItem.getId());

         throw new  AccountException("Transaction id " + transactionItem.getId() + " is locked");
      }
      boolean  bRecalcChecked = (!txn.getChecked() && updtxn.getChecked());

      // updtxn is possibly not a complete set of Transaction values.
      // Maybe should add checks for presence of values???
      txn.setDate(updtxn.getDate());
      txn.setType(updtxn.getType());
      txn.setComment(updtxn.getComment());
      txn.setCredit(updtxn.getCredit());
      txn.setDebit(updtxn.getDebit());
      txn.setChecked(updtxn.getChecked());
      txn.setStid(updtxn.getStid());
      Transaction txnupdated = update(txn);

      if(bRecalcChecked)
      {
         // not sure if I really want this as it could be very time consuming
         balanceService.calculateCheckedBalances(txnupdated.getAccountId(), Optional.of(txnupdated));
      }
      return txnupdated;
   }

   @Transactional
   public void deleteTransaction(TransactionItem transactionItem)
   {
      log.info("deleteTransaction: transactionItem:{}", transactionItem);
      if(transactionItem == null)
         return;
      Transaction txn = getTransaction(transactionItem.getId())
            .orElseThrow(()->new AccountException("Transaction id " + transactionItem.getId() + " not found"));

      String origToken = Utils.getToken(txn);
      if(!origToken.equals(transactionItem.getToken()))
      {
         log.info("deleteTransaction: Token mismatch for transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), origToken, transactionItem.getToken());
         throw new  AccountException("Token does not match Transaction id " + transactionItem.getId());
      }

      Transaction deltxn = mapToEntity(transactionItem);
      if(txn.getAccountId() != deltxn.getAccountId())
      {
         log.info("updateTransaction: Account id does not match transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), txn.getAccountId(), deltxn.getAccountId());
         throw new  AccountException("Account id does not match Transaction id " + transactionItem.getId());
      }

      // Must mark txn as unchecked in order to delete it
      if(deltxn.getChecked())
      {
         log.info("deleteTransaction: Locked transaction: id:{}", transactionItem.getId());
         throw new  AccountException("Transaction id " + transactionItem.getId() + " is locked");
      }

      boolean  bRecalcChecked = deltxn.getChecked();

      delete(txn);

      if(bRecalcChecked)
      {
         // not sure if I really want this as it could be very time consuming
         balanceService.calculateCheckedBalances(deltxn.getAccountId(), Optional.empty());
      }
   }
   // This must be @Transactional because it recalculates the balances of all following transactions
   // and any failure during this calculation should revert all changes
   @Transactional
   public Transaction update(Transaction txn)
   {
      txn = transactionJpaRepository.save(txn);
      balanceService.calculateBalances(txn.getAccountId(), Optional.of(txn));
      return txn;
   }

   // Add is effectively the same as update but with 0 following transactions
   @Transactional
   public Transaction add(Transaction txn)
   {
      txn = transactionJpaRepository.saveAndFlush(txn);
      log.info("add: added transaction: {}", txn);
      // TODO: find a way to avoid a full recalculation, can't use the just deleted transaction as the start
      balanceService.calculateBalances(txn.getAccountId(), Optional.of(txn));
      return txn;
   }

   @Transactional
   public void delete(Transaction txn)
   {
      log.info("delete: deleting transaction: {}", txn);
      Optional<Transaction> prevtxn = transactionJpaRepository.findFirstByAccountIdAndSequenceIsLessThanOrderBySequenceAsc(txn.getAccountId(), txn.getSequence());
      transactionJpaRepository.delete(txn);
      balanceService.calculateBalances(txn.getAccountId(), prevtxn);

   }

   private Transaction mapToEntity(TransactionItem transactionItem)
   {
      Transaction tosave = new Transaction();
      tosave.setAccountId(transactionItem.getAccid());
      tosave.setDate(transactionItem.getDate());
      tosave.setType(transactionItem.getType());
      tosave.setComment(transactionItem.getComment());
      tosave.setChecked(transactionItem.isLocked());
      tosave.setStid(transactionItem.getStatementref());

      BigDecimal amount = new BigDecimal(transactionItem.getAmount());
      amount.setScale(2); // Max. two decimal places for a normal currency transaction

      // With VB app both credit and debit could be set. This is not
      // supported for the web UI, ie. either credit or debit can be set, not both
      if(amount.signum() < 0)
      {
         // Transactions in web UI are +ve for DEBIT since debits are what is usually entered
         amount = amount.abs();
         tosave.setCredit(amount);
         tosave.setDebit(null);
      }
      else
      {
         tosave.setDebit(amount);
         tosave.setCredit(null);
      }
      return tosave;
   }

   public enum BalanceType
   {
      NORMAL,
      CHECKED,
      SORTED
   }

   public TransactionItem mapToItem(Transaction t, BalanceType balanceType)
   {
      BigDecimal amount = BigDecimal.ZERO;

      if(t.getDebit() != null)
      {
         amount = t.getDebit();
      }
      else if(t.getCredit() != null) // Shouldn't happen but does in the TEST DB so maybe can in live db
      {
         amount = t.getCredit().negate();
      }

      String itemBalance;
      switch(balanceType)
      {
      case CHECKED:
         itemBalance = Utils.formatAmount(t.getCheckedBalance());
         break;
      case SORTED:
         itemBalance = Utils.formatAmount(t.getSortedBalance());
         break;
      case NORMAL:
      default:
         itemBalance = Utils.formatAmount(t.getBalance());
      }
      // jackson doesn't handle Java dates [it does now!!] and bigdecimal has too many decimal places so it's
      // simpler just to send the data as Strings with the desired formating.
      String token = Utils.getToken(t);
      return new TransactionItem(t.getAccountId(),
            t.getDate(),
            Utils.formatAmount(amount),
            t.getType(),
            t.getComment(),
            t.getChecked(),
            t.getSequence(),
            token,
            itemBalance,
            t.getStid()
            );
   }

   public Transactions getCheckedTransactions(long accountId, int rows, int pageno)
   {
      return getTransactions(getCheckedTransactionPage(accountId, rows, pageno), BalanceType.CHECKED);
   }

   public List<Transaction> getCheckedTransactionPage(long accountId, int rows, int pageno)
   {
      Pageable p = Pageable.unpaged();
      if(pageno >=0 )
      {
         p = PageRequest.of(pageno, rows);
      }
      List<Transaction> txns = transactionJpaRepository.
            findByAccountIdAndCheckedIsTrueOrderByDateDescSequenceDesc(accountId, p).stream()
            .sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      return txns;
   }

   public TransactionItem getCheckedBalance(Long accountid)
   {
      Transaction t =  this.transactionJpaRepository.findFirstByAccountIdAndCheckedIsTrueAndCheckedBalanceIsNotNullOrderByDateDescSequenceDesc(accountid)
            .orElseThrow(() -> new AccountException("Checked balances not found: " + accountid));
      TransactionItem ti = mapToItem(t, BalanceType.CHECKED);
      return ti;
   }



}
