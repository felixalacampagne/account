package com.felixalacampagne.account.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.model.TransactionItem;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.model.Version;
import com.felixalacampagne.account.service.AccountService;
import com.felixalacampagne.account.service.TransactionService;

@RestController
@RequestMapping
@CrossOrigin(origins = "http://localhost:4200") // required because ng serve is on 4200 and standalone spring server is on 8080
public class AccountController {
   private final Logger log = LoggerFactory.getLogger(this.getClass());
   private final AccountService accountService;
   private final TransactionService transactionService;
   
   private final Version version;
   
   public AccountController(AccountService accountService, TransactionService transactionService, Version version) 
   {
      this.accountService = accountService;
      this.transactionService = transactionService;
      this.version = version;
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
    
//    Could use @RequestParam(name="name", required=false, defaultValue="World" to supply as url ? values
    @GetMapping("/listtransaction/{accountid}")
    public Transactions getTransactions(@PathVariable Long accountid) // This needs to receive an account id
    {
       return this.transactionService.getTransactions(accountid);
    }


    // Without @RequestBody Spring seems to be expecting something other than the JSON of the
    // TransactionItem (haven't got a forking clue what it is expecting). The front-end not surprisingly
    // only provides the TransactionItem json.
    @PostMapping(value = "/addtransaction")
    public String addTransaction(@RequestBody TransactionItem transactionItem, Model model)
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
}
