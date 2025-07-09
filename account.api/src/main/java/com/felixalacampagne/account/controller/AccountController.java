package com.felixalacampagne.account.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.felixalacampagne.account.model.AccountDetail;
import com.felixalacampagne.account.model.AccountItem;
import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.model.AddTransactionItem;
import com.felixalacampagne.account.model.EPCTransaction;
import com.felixalacampagne.account.model.StandingOrderItem;
import com.felixalacampagne.account.model.TfrAccountItem;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.model.TransferAccountItem;
import com.felixalacampagne.account.model.Version;
import com.felixalacampagne.account.service.AccountException;
import com.felixalacampagne.account.service.AccountService;
import com.felixalacampagne.account.service.BalanceService;
import com.felixalacampagne.account.service.EPCTransactionService;
import com.felixalacampagne.account.service.PhoneAccountService;
import com.felixalacampagne.account.service.StandingOrderService;
import com.felixalacampagne.account.service.TransactionService;
import com.felixalacampagne.account.service.TransactionService.BalanceType;

@RestController
@RequestMapping
@CrossOrigin(origins = "http://localhost:4200") // required because ng serve is on 4200 and standalone spring server is on 8080
public class AccountController {
   public final static String URL_GREETING = "/greeting";
   public final static String URL_VERSION = "/version";
   public final static String URL_GETACCOUNTSSHORT = "/listaccount";
   public final static String URL_GETONEACCOUNT = "/account";
   public final static String URL_GETACCOUNTSFORTFR = "/accsfortfr";

   public final static String URL_GETACCOUNTS = "/listaccinf";
   public final static String URL_ADDACCOUNT = "/addaccount";
   public final static String URL_UPDACCOUNT = "/updaccount";
   public final static String URL_DELACCOUNT = "/delaccount";

   public final static String URL_GETTFRACCOUNTS = "/listtransferaccounts";
   public final static String URL_ADDTFRACCOUNTS = "/addtransferaccount";
   public final static String URL_UPDTFRACCOUNTS = "/updatetransferaccount";
   public final static String URL_DELTFRACCOUNTS = "/deletetransferaccount";

   public final static String URL_GETSTANDORDS = "/liststandingorders";
   public final static String URL_ADDSTANDORD = "/addstandingorder";
   public final static String URL_UPDSTANDORD = "/updatestandingorder";
   public final static String URL_DELSTANDORD = "/delstandingorder";
   public final static String URL_GETONESTANDORD = "/standingorder";

   public final static String URL_GETTRANSACTIONS = "/listtransaction";
   public final static String URL_ADDTRANSACTION = "/addtransaction";
   public final static String URL_UPDTRANSACTION = "/updatetransaction";
   public final static String URL_DELTRANSACTION = "/deletetransaction";

   public final static String URL_CALCCHECKBAL = "/calcchecked";
   public final static String URL_GETCHECKBAL = "/getchecked";
   public final static String URL_GETCHECKTXNS = "/listchecked";
   public final static String URL_GETQRCODE = "/qrcodepayer";


   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final AccountService accountService;
   private final TransactionService transactionService;
   private final StandingOrderService standingOrderService;
   private final BalanceService balanceService;
   private final EPCTransactionService epcTransactionService;
   private final PhoneAccountService phoneAccountService;
   private final Version version;

   public AccountController(AccountService accountService,
                           TransactionService transactionService,
                           StandingOrderService standingOrderService,
                           Version version,
                           BalanceService balanceService,
                           EPCTransactionService epcTransactionService,
                           PhoneAccountService phoneAccountService)
   {
      this.accountService = accountService;
      this.transactionService = transactionService;
      this.standingOrderService = standingOrderService;
      this.version = version;
      this.balanceService = balanceService;
      this.epcTransactionService = epcTransactionService;
      this.phoneAccountService = phoneAccountService;
   }

    @GetMapping(URL_GREETING)
    public String getMessage()
    {
        return "Welcome to the Account Spring Boot Application running on Tomcat server\nand reading from Access database;\n";
    }

    @GetMapping(URL_VERSION)
    public Version getVersion()
    {
       log.debug("getVersion: {}", this.version);
       return this.version;
    }

    // Note Spring will automatically convert objects to json string and supply the appropriate header
    @GetMapping(URL_GETACCOUNTSSHORT)
    public Accounts getAccounts()
    {
       return this.accountService.getAccounts();
    }

    @GetMapping(URL_GETONEACCOUNT + "/{id}")
    public AccountItem getAccount(@PathVariable Long id)
    {
       return this.accountService.getAccountItem(id);
    }

    @GetMapping(URL_GETACCOUNTSFORTFR + "/{id}")
    public List<TfrAccountItem> getTransferAccounts(@PathVariable Long id)
    {
       return this.accountService.getTransferAccounts(id);
    }

    @GetMapping("/accinf/{id}")
    public AccountDetail getAccountDetail(@PathVariable Long id)
    {
       return this.accountService.getAccountDetail(id);
    }


    @GetMapping(URL_GETACCOUNTS)
    public List<AccountDetail> getAccountDetails()
    {
       return this.accountService.getAccountDetailList();
    }

    @PostMapping(value = URL_ADDACCOUNT)
    public String addAccountDetail(@RequestBody AccountDetail accountDetail, Model model)
    {
       log.info("addAccountDetail: item to add: {}", accountDetail);
       try
       {
          this.accountService.addAccount(accountDetail);

       }
       catch(Exception ex)
       {
          log.info("addAccountDetail: Failed to add: {}", accountDetail, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_UPDACCOUNT)
    public String updAccountDetail(@RequestBody AccountDetail accountDetail, Model model)
    {
       log.info("updAccountDetail: item to update: {}", accountDetail);
       try
       {
          this.accountService.updateAccount(accountDetail);
       }
       catch(Exception ex)
       {
          log.info("updAccountDetail: Failed to update: {}", accountDetail, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_DELACCOUNT)
    public String deleteAccountDetail(@RequestBody AccountDetail accountDetail, Model model)
    {
       log.info("deleteAccountDetail: item to delete: {}", accountDetail);
       try
       {
          this.accountService.deleteAccount(accountDetail);
       }
       catch(Exception ex)
       {
          log.info("deleteAccountDetail: Failed to delete: {}", accountDetail, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }


    @PostMapping(value = "/updaccountstref")
    public String updateAccountStatementRef(@RequestBody AccountItem accountItem, Model model)
    {
       log.info("updateAccountStatementRef: item to update: {}", accountItem);
       try
       {
          this.accountService.updateStatementRef(accountItem);
       }
       catch(Exception ex)
       {
          log.info("updateAccountStatementRef: Failed to update: {}", accountItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

// Could use @RequestParam(name="name", required=false, defaultValue="World" to supply as url ? values
    @GetMapping(value = {URL_GETTRANSACTIONS +"/{accountid}", URL_GETTRANSACTIONS + "/{accountid}/{page}"})
    public Transactions getTransactions(
          @PathVariable Long accountid,
          @PathVariable Optional<Integer> page,
          @RequestParam Optional<Integer> rows)
    {
       int pageno = 0;
       int pagesize = 20;
       if(page.isPresent())
       {
          pageno = page.get();
          if(rows.isPresent())
          {
             pagesize = rows.get();
          }

       }
       return this.transactionService.getTransactions(accountid, pageno, pagesize);
    }


    // Without @RequestBody Spring seems to be expecting something other than the JSON of the
    // TransactionItem (haven't got a forking clue what it is expecting). The front-end not surprisingly
    // only provides the TransactionItem json.
    @PostMapping(value = URL_ADDTRANSACTION)
    public String addTransaction(@RequestBody AddTransactionItem transactionItem, Model model)
    {
       log.info("addTransaction: transaction item to add: {}", transactionItem);
       try
       {
          this.transactionService.addTransaction(transactionItem);
       }
       catch(Exception ex)
       {
          log.info("addTransaction: Failed to add: {}", transactionItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_UPDTRANSACTION)
    public String updateTransaction(@RequestBody TransactionItem transactionItem, Model model)
    {
       log.info("updateTransaction: transaction item to update: {}", transactionItem);
       try
       {
          this.transactionService.updateTransaction(transactionItem);
          log.info("updateTransaction: transaction item updated: {}", transactionItem);
       }
       catch(Exception ex)
       {
          log.info("updateTransaction: Failed to update: {}", transactionItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_DELTRANSACTION)
    public String deleteTransaction(@RequestBody TransactionItem transactionItem, Model model)
    {
       log.info("deleteTransaction: transaction item to delete: {}", transactionItem);
       try
       {
          this.transactionService.deleteTransaction(transactionItem);
       }
       catch(Exception ex)
       {
          log.info("deleteTransaction: Failed to delete: {}", transactionItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @GetMapping(URL_GETONESTANDORD + "/{id}")
    public StandingOrderItem getStandingOrderItem(@PathVariable Long id)
    {
       return this.standingOrderService.getStandingOrderItem(id);
    }

    // Note Spring will automatically convert objects to json string and supply the appropriate header
    @GetMapping(URL_GETSTANDORDS)
    public List<StandingOrderItem> getStandingOrderItems()
    {
       return this.standingOrderService.getStandingOrderItems();
    }

    @PostMapping(value = URL_ADDSTANDORD)
    public String addStandingOrder(@RequestBody StandingOrderItem standingOrderItem, Model model)
    {
       log.info("addStandingOrder: item to add: {}", standingOrderItem);
       try
       {
          this.standingOrderService.addStandingOrderItem(standingOrderItem);

       }
       catch(Exception ex)
       {
          log.info("addStandingOrder: Failed to add: {}", standingOrderItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_UPDSTANDORD)
    public String updateStandingOrder(@RequestBody StandingOrderItem standingOrderItem, Model model)
    {
       log.info("updateStandingOrder: item to update: {}", standingOrderItem);
       try
       {
          this.standingOrderService.updateStandingOrderItem(standingOrderItem);

       }
       catch(Exception ex)
       {
          log.info("updateStandingOrder: Failed to update: {}", standingOrderItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_DELSTANDORD)
    public String deleteStandingOrder(@RequestBody StandingOrderItem standingOrderItem, Model model)
    {
       log.info("deleteStandingOrder: item to delete: {}", standingOrderItem);
       try
       {
          this.standingOrderService.deleteStandingOrder(standingOrderItem);
       }
       catch(Exception ex)
       {
          log.info("deleteStandingOrder: Failed to delete: {}", standingOrderItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @GetMapping(value = URL_CALCCHECKBAL + "/{accountid}")
    public TransactionItem calcChecked(@PathVariable Long accountid)
    {
       // This is most likely to be used because an external reconcile was done in which
       // case the actual balances might need re-calculation. I'll put it here for
       // now but maybe it should be done via the transaction list - or auto-detected?
       // transactionService.updateBalance decides whether sorted balances are being used or not
       // - should move it to balanceService really
       this.transactionService.updateBalance(accountid);
       // An exception might be a bit extreme for no checked balances
       TransactionItem ti = this.balanceService.calculateCheckedBalances(accountid)
                      .map(t ->this.transactionService.mapToItem(t, BalanceType.CHECKED))
                      .orElseThrow(() -> new AccountException("Checked balances not found: " + accountid));
       return ti;
    }

    @GetMapping(value = URL_GETCHECKBAL + "/{accountid}")
    public TransactionItem getLatestChecked(@PathVariable Long accountid)
    {
       // An exception might be a bit extreme for no checked balances
       TransactionItem ti = this.transactionService.getCheckedBalance(accountid);
       return ti;
    }

    @GetMapping(value = {URL_GETCHECKTXNS + "/{accountid}", URL_GETCHECKTXNS + "/{accountid}/{page}"})
    public Transactions getCheckedTransactions(
          @PathVariable Long accountid,
          @PathVariable Optional<Integer> page)
    {
       int pageno = -1;
       if(page.isPresent())
       {
          pageno = page.get();
       }
       return this.transactionService.getCheckedTransactions(accountid, 25, pageno);
    }

    @PostMapping(value = URL_GETQRCODE, produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getQREPCImage(@RequestBody EPCTransaction epcTransaction, Model model)
    {
       log.info("getQREPCImage: epc: {}", epcTransaction);
       try
       {
          byte[] png = this.epcTransactionService.getQRImagePNG(epcTransaction);
          return png;
       }
       catch(Exception ex)
       {
          log.info("getQREPCImage: Failed to create QR code: {}", epcTransaction, ex);
       }

       return null;
    }

    @GetMapping(URL_GETTFRACCOUNTS)
    public List<TransferAccountItem> getTransferAccounts()
    {
       return this.phoneAccountService.getPhoneAccounts();
    }

    @PostMapping(value = URL_UPDTFRACCOUNTS)
    public String updateTransferAccount(@RequestBody TransferAccountItem phoneAccountItem, Model model)
    {
       log.info("updateTransferAccount: item to update: {}", phoneAccountItem);
       try
       {
          this.phoneAccountService.updateTransferAccount(phoneAccountItem);

       }
       catch(Exception ex)
       {
          log.info("updateTransferAccount: Failed to update: {}", phoneAccountItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_ADDTFRACCOUNTS)
    public String addTransferAccount(@RequestBody TransferAccountItem phoneAccountItem, Model model)
    {
       log.info("addTransferAccount: item to add: {}", phoneAccountItem);
       try
       {
          this.phoneAccountService.addTransferAccount(phoneAccountItem);

       }
       catch(Exception ex)
       {
          log.info("addTransferAccount: Failed to add: {}", phoneAccountItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PostMapping(value = URL_DELTFRACCOUNTS)
    public String deleteTransferAccount(@RequestBody TransferAccountItem phoneAccountItem, Model model)
    {
       log.info("deleteTransferAccount: item to delete: {}", phoneAccountItem);
       try
       {
          this.phoneAccountService.deleteTransferAccount(phoneAccountItem);
       }
       catch(Exception ex)
       {
          log.info("deleteTransferAccount: Failed to delete: {}", phoneAccountItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }


}
