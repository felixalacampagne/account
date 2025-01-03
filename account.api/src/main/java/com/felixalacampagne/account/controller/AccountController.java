package com.felixalacampagne.account.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felixalacampagne.account.model.AccountDetail;
import com.felixalacampagne.account.model.AccountItem;
import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.model.AddTransactionItem;
import com.felixalacampagne.account.model.StandingOrderItem;
import com.felixalacampagne.account.model.TfrAccountItem;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.model.Version;
import com.felixalacampagne.account.service.AccountException;
import com.felixalacampagne.account.service.AccountService;
import com.felixalacampagne.account.service.BalanceService;
import com.felixalacampagne.account.service.StandingOrderService;
import com.felixalacampagne.account.service.TransactionService;
import com.felixalacampagne.account.service.TransactionService.BalanceType;

@RestController
@RequestMapping
@CrossOrigin(origins = "http://localhost:4200") // required because ng serve is on 4200 and standalone spring server is on 8080
public class AccountController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final AccountService accountService;
   private final TransactionService transactionService;
   private final StandingOrderService standingOrderService;
   private final BalanceService balanceService;

   private final Version version;

   public AccountController(AccountService accountService,
                           TransactionService transactionService,
                           StandingOrderService standingOrderService,
                           Version version,
                           BalanceService balanceService)
   {
      this.accountService = accountService;
      this.transactionService = transactionService;
      this.standingOrderService = standingOrderService;
      this.version = version;
      this.balanceService = balanceService;
   }

    @GetMapping("/greeting")
    public String getMessage()
    {
        return "Welcome to the Account Spring Boot Application running on Tomcat server\nand reading from Access database;\n";
    }

    @GetMapping("/version")
    public Version getVersion()
    {
       log.debug("getVersion: {}", this.version);
       return this.version;
    }


    // Note Spring will automatically convert objects to json string and supply the appropriate header
    @GetMapping("/listaccount")
    public Accounts getAccounts()
    {
       return this.accountService.getAccounts();
    }

    @GetMapping("/account/{id}")
    public AccountItem getAccount(@PathVariable Long id)
    {
       return this.accountService.getAccountItem(id);
    }

    @GetMapping("/accsfortfr/{id}")
    public List<TfrAccountItem> getTransferAccounts(@PathVariable Long id)
    {
       return this.accountService.getTransferAccounts(id);
    }

    @GetMapping("/accinf/{id}")
    public AccountDetail getAccountDetail(@PathVariable Long id)
    {
       return this.accountService.getAccountDetail(id);
    }

    @GetMapping("/listaccinf")
    public List<AccountDetail> getAccountDetails()
    {
       return this.accountService.getAccountDetailList();
    }


    @PutMapping(value = "/updaccountstref")
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
    @GetMapping(value = {"/listtransaction/{accountid}", "/listtransaction/{accountid}/{page}"})
    public Transactions getTransactions(
          @PathVariable Long accountid,
          @PathVariable Optional<Integer> page)
    {
       int pageno = 0;
       if(page.isPresent())
       {
          pageno = page.get();
       }
       return this.transactionService.getTransactions(accountid, pageno);
    }


    // Without @RequestBody Spring seems to be expecting something other than the JSON of the
    // TransactionItem (haven't got a forking clue what it is expecting). The front-end not surprisingly
    // only provides the TransactionItem json.
    @PostMapping(value = "/addtransaction")
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

    @PutMapping(value = "/updatetransaction")
    public String updateTransaction(@RequestBody TransactionItem transactionItem, Model model)
    {
       log.info("updateTransaction: transaction item to add: {}", transactionItem);
       try
       {
          this.transactionService.updateTransaction(transactionItem);
       }
       catch(Exception ex)
       {
          log.info("updateTransaction: Failed to update: {}", transactionItem, ex);
          return "failed: " + ex.getMessage();
       }

       return "ok";
    }

    @PutMapping(value = "/deletetransaction")
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

    // Note Spring will automatically convert objects to json string and supply the appropriate header
    @GetMapping("/liststandingorders")
    public List<StandingOrderItem> getStandingOrderItems()
    {
       return this.standingOrderService.getStandingOrderItems();
    }

    @GetMapping("/standingorder/{id}")
    public StandingOrderItem getStandingOrderItem(@PathVariable Long id)
    {
       return this.standingOrderService.getStandingOrderItem(id);
    }

    @PostMapping(value = "/addstandingorder")
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

    @PutMapping(value = "/updatestandingorder")
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


    @GetMapping(value = "/calcchecked/{accountid}")
    public TransactionItem calcChecked(@PathVariable Long accountid)
    {
       // An exception might be a bit extreme for no checked balances
       TransactionItem ti = this.balanceService.calculateCheckedBalances(accountid, Optional.empty())
                      .map(t ->this.transactionService.mapToItem(t, BalanceType.CHECKED))
                      .orElseThrow(() -> new AccountException("Checked balances not found: " + accountid));
       return ti;
    }

    @GetMapping(value = "/getchecked/{accountid}")
    public TransactionItem getLatestChecked(@PathVariable Long accountid)
    {
       // An exception might be a bit extreme for no checked balances
       TransactionItem ti = this.transactionService.getCheckedBalance(accountid);
       return ti;
    }

    @GetMapping(value = {"/listchecked/{accountid}", "/listchecked/{accountid}/{page}"})
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
}
