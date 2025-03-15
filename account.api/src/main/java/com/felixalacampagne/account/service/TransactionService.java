package com.felixalacampagne.account.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
   public enum BalanceType
   {
      NORMAL,
      SORTED,
      CHECKED
   }

   // Only NORMAL or SORTED is supported. CHECKED is ONLY for displaying checked entries
   // Dynamically switching between modes is not supported - I've done it like this to make
   // the transition from pure sequence to date/sequence a bit easier (I hope)
   // Note that two different transaction columns are used for sequence and date/sequence balances
   // and only one is updated by default. When the default is changed some way of triggering a complete
   // recalculation for all active accounts will be required.
   @Value("${falc.account.transaction.listsort:NORMAL}")
   private BalanceType defaultBalanceType; // = BalanceType.SORTED;

   public BalanceType getDefaultBalanceType()
   {
      return defaultBalanceType;
   }

   private final Logger log = LoggerFactory.getLogger(this.getClass());

   private final TransactionJpaRepository transactionJpaRepository;
   private final AccountJpaRepository accountJpaRepository;
   private final PhoneAccountJpaRepository phoneAccountJpaRepository;
   private final ConnectionResurrector<TransactionJpaRepository> connectionResurrector;
   private final BalanceService balanceService;
   private final TransactionHandlerService transactionHandler;


   @Autowired
   public TransactionService(TransactionJpaRepository transactionJpaRepository,
                             BalanceService balanceService,
                             AccountJpaRepository accountJpaRepository,
                             PhoneAccountJpaRepository phoneAccountJpaRepository,
                             TransactionHandlerService transactionHandler
                             ) {
      this.transactionJpaRepository = transactionJpaRepository;
      this.balanceService = balanceService;
      this.accountJpaRepository = accountJpaRepository;
      this.phoneAccountJpaRepository = phoneAccountJpaRepository;
      this.transactionHandler = transactionHandler;
      this.connectionResurrector = new ConnectionResurrector<TransactionJpaRepository>(transactionJpaRepository, TransactionJpaRepository.class);
   }

   // Page uses 1 based index
   public Transactions getTransactions(long accountId, int pageOneBased)
   {
      return getTransactions(accountId, pageOneBased, 15);
   }

   // Page uses 1 based index
   public Transactions getTransactions(long accountId, int pageOneBased, int pagesize)
   {
      if((pagesize < 10) || (pagesize>300))
      {
         pagesize = 10;
      }

      // Allow -1 to mean last page
      connectionResurrector.ressurectConnection();
      long rowcount = transactionJpaRepository.countByAccountId(accountId);
      int maxPageOneBased = (int) (rowcount / pagesize) + 1;

      if(pageOneBased == -1)
      {
         pageOneBased = maxPageOneBased;
      }
      else if(pageOneBased < 1)
      {
         pageOneBased = 1;
      }

      int pageZeroBased = (int) Long.min(pageOneBased, maxPageOneBased)-1;


      log.debug("getTransactions: page size:{}, rowcount:{}, page:{}, maxpage:{}", pagesize, rowcount, pageZeroBased, maxPageOneBased-1);

      return getTransactions(getTransactionPage(pageZeroBased, pagesize, accountId),
            accountId, pageZeroBased+1, rowcount,
            this.defaultBalanceType);
   }

   public Transactions getTransactions(List<Transaction> txns, long accountId,
         long pageOneBased, long rowcount, BalanceType balanceType)
   {
      if(rowcount < 0)
      {
         rowcount = transactionJpaRepository.countByAccountId(accountId);
      }
      Account acc = this.accountJpaRepository.findById(accountId).orElseThrow(() -> new AccountException("Account not found: " + accountId));
      final String amtfmt = acc.getAccFmt();
      List<TransactionItem> txnitems = txns.stream()
            .map(t -> mapToItem(t, amtfmt, balanceType))
            .collect(Collectors.toList());
      Transactions trns = new Transactions(txnitems, pageOneBased, rowcount);
      return trns;
   }

   // Page uses the native, 0 based, index
   public List<Transaction> getTransactionPage(int page, int rows, long accountId)
   {
      if(BalanceType.SORTED == this.defaultBalanceType)
      {
         return getTransactionPageDateSorted(page, rows, accountId);
      }
      return getTransactionPageSequenceSorted(page, rows, accountId);
   }

   public List<Transaction> getTransactionPageSequenceSorted(int page, int rows, long accountId)
   {
      List<Transaction> txns = transactionJpaRepository.
            findByAccountId(accountId,  PageRequest.of(page, rows, Sort.by("sequence").descending())).stream()
            .sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      return txns;
   }

   // Page uses the native, 0 based, index
   public List<Transaction> getTransactionPageDateSorted(int page, int rows, long accountId)
   {
      List<Transaction> txns = transactionJpaRepository.
            findByAccountId(accountId,  PageRequest.of(page, rows,
                  Sort.by(Sort.Order.desc("date"),Sort.Order.desc("sequence"))))
                  .stream()
            .collect(Collectors.toList());
      Collections.reverse(txns);
      return txns;
   }

   public Optional<Transaction> getTransaction(long id)
   {
      connectionResurrector.ressurectConnection();
      return transactionJpaRepository.findById(id);
   }

   @Transactional // this must be transactional
   private Long addPhoneAccountTransaction(Long phoneAccId, Transaction srcTxn, String communication)
   {
      Long tfraccid = null;
      Long srcaccid = srcTxn.getAccountId();
      PhoneWithAccountProjection paproj = this.phoneAccountJpaRepository.findPhoneWithAccountById(phoneAccId)
                                              .orElseThrow(() -> new AccountException("Phone account not found: " + phoneAccId));
      PhoneAccount pa = paproj.getPhoneAccount();
      if(pa.getAccountId() > 0) // transfer is to a related account so must apply a 'reverse' transaction to it
      {
         tfraccid = pa.getAccountId();
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
         log.info("addPhoneAccountTransaction: added transfer transaction for account id {}: id:{}", txntfr.getAccountId(), txntfr.getSequence());
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
      return tfraccid;
   }

   class UpdateAccounts
   {
      public Long sourceAccId;
      public Long transferAccId;
   }

   public void addTransaction(AddTransactionItem transactionItem)
   {
      // Execute transaction record updates in a transaction, which can only be initiated by an external
      // call to the @Transactional method.
      UpdateAccounts updAccs = this.transactionHandler.runInTransaction(
            () -> addTransactionTransactional(transactionItem));
      log.info("addTransaction: updated accounts: sourceAccId:{}, transferAccId:{}", updAccs.sourceAccId, updAccs.transferAccId);
      this.updateBalance(updAccs.sourceAccId);
      this.updateBalance(updAccs.transferAccId);
   }

   @Transactional // potentially two transactions are added so transaction is required
   public UpdateAccounts addTransactionTransactional(AddTransactionItem transactionItem)
   {
      UpdateAccounts updAccs = new UpdateAccounts();
      connectionResurrector.ressurectConnection();
      Transaction txn = mapToEntity(transactionItem);

      Transaction updtxn = add(txn);
      updAccs.sourceAccId = updtxn.getAccountId();
      log.info("addTransaction: added transaction for account id {}: id:{}", updtxn.getAccountId(), updtxn.getSequence());
      if(transactionItem.getTransferAccount().isPresent())
      {
         updAccs.transferAccId = addPhoneAccountTransaction(transactionItem.getTransferAccount().get(), updtxn, transactionItem.getCommunication());
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
      log.info("addTransactionTransactional: updated accounts: sourceAccId:{}, transferAccId:{}", updAccs.sourceAccId, updAccs.transferAccId);
      return updAccs;
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
   // @Transactional - keep balance calc out of transaction
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
      boolean  bRecalcChecked = (txn.getChecked() != updtxn.getChecked());

      log.debug("updateTransaction: {} is equal to {}: {}", txn.getDate(), updtxn.getDate(), txn.getDate().equals(updtxn.getDate()));
      boolean bRecalcBal = ! (Utils.areSame(txn.getCredit(), updtxn.getCredit())
                           && Utils.areSame(txn.getDebit(), updtxn.getDebit())
                           && txn.getDate().equals(updtxn.getDate()));

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


      // With date sorting must recalc balance if the date is changed
      if( bRecalcBal )
      {
         this.updateBalance(txnupdated.getAccountId());
      }

      if(bRecalcChecked)
      {
         balanceService.calculateCheckedBalances(txnupdated.getAccountId());
      }
      return txnupdated;
   }

//   public boolean addUsers(List<User> users) {
//      for (User user : users) {
//          transactionHandler.runInTransaction(() -> addUser(user.getUsername, user.getPassword));
//      }
//   }
   // @Transactional - keep balance calc out of transaction
   public void deleteTransaction(TransactionItem transactionItem)
   {
      log.info("deleteTransaction: transactionItem:{}", transactionItem);
      if(transactionItem == null)
         return;
      Transaction txn = getTransaction(transactionItem.getId())
            .orElseThrow(()->new AccountException("Transaction id " + transactionItem.getId() + " not found"));

      final long txnAccId = txn.getAccountId();
      String origToken = Utils.getToken(txn);
      if(!origToken.equals(transactionItem.getToken()))
      {
         log.info("deleteTransaction: Token mismatch for transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), origToken, transactionItem.getToken());
         throw new  AccountException("Token does not match Transaction id " + transactionItem.getId());
      }

      Transaction deltxn = mapToEntity(transactionItem);
      if(txnAccId != deltxn.getAccountId())
      {
         log.info("updateTransaction: Account id does not match transaction id:{}: original:{} supplied:{}",
               transactionItem.getId(), txnAccId, deltxn.getAccountId());
         throw new  AccountException("Account id does not match Transaction id " + transactionItem.getId());
      }

      // Must mark txn as unchecked in order to delete it
      if(deltxn.getChecked())
      {
         log.info("deleteTransaction: Locked transaction: id:{}", transactionItem.getId());
         throw new  AccountException("Transaction id " + transactionItem.getId() + " is locked");
      }

      boolean  bRecalcChecked = txn.getChecked();
      delete(txn);

      updateBalance(txnAccId);

      if(bRecalcChecked)
      {
         balanceService.calculateCheckedBalances(txnAccId);
      }
   }

   // Add is effectively the same as update but with 0 following transactions
   // @Transactional - keep balance calc out of transaction
   public Transaction add(Transaction txn)
   {
      log.info("add: adding transaction: {}", txn);
      return update(txn);
   }

   // This must be @Transactional because it recalculates the balances of all following transactions
   // and any failure during this calculation should revert all changes.
   //
   // Must make balance calculation non-transactional as the time it takes to commit many
   // records exceeds the time that the connection remains open, ie. a Connection closed
   // exception can happen which I still haven't been able to prevent.
   //
   // If the balances are not in the transaction then no need for the update to be in a transaction
   public Transaction update(Transaction txn)
   {
      log.info("update: updating transaction: {}", txn);
      txn = transactionJpaRepository.saveAndFlush(txn);
      return txn;
   }

   private void updateBalance(Long accountId)
   {
      if(accountId != null)
      {
         if(BalanceType.SORTED == this.defaultBalanceType)
         {
            log.info("update: updating date/sequence sorted balance for acc id: {}", accountId);
            balanceService.calculateDatesortedBalances(accountId);
         }
         else
         {
            log.info("update: updating sequence only sorted balance for acc id: {}", accountId);
            balanceService.calculateBalances(accountId);
         }
      }
   }

   public void delete(Transaction txn)
   {
      log.info("delete: deleting transaction: {}", txn);
      transactionJpaRepository.delete(txn);
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


   public TransactionItem mapToItem(Transaction t, BalanceType balanceType)
   {
      return mapToItem(t, null, balanceType);
   }

   public TransactionItem mapToItem(Transaction t, String amtfmt, BalanceType balanceType)
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
         itemBalance = Utils.formatAmount(t.getCheckedBalance(), amtfmt);
         break;
      case SORTED:
         itemBalance = Utils.formatAmount(t.getSortedBalance(), amtfmt);
         break;
      case NORMAL:
      default:
         itemBalance = Utils.formatAmount(t.getBalance(), amtfmt);
      }
      // jackson doesn't handle Java dates [it does now!!] and bigdecimal has too many decimal places so it's
      // simpler just to send the data as Strings with the desired formating.
      String token = Utils.getToken(t);
      return new TransactionItem(t.getAccountId(),
            t.getDate(),
            Utils.formatAmount(amount),
            Utils.formatAmount(amount, amtfmt),
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
      // TODO: Make 1 based, resurrectconnection, get rowcount, check for maxpage
      return getTransactions(getCheckedTransactionPage(accountId, rows, pageno), accountId, pageno, -1L, BalanceType.CHECKED);
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
            //.sorted(Comparator.comparingLong(Transaction::getSequence))
            .collect(Collectors.toList());
      Collections.reverse(txns);
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
