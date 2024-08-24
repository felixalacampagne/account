package com.felixalacampagne.account.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felixalacampagne.account.model.Accounts;
import com.felixalacampagne.account.model.Transactions;
import com.felixalacampagne.account.service.AccountService;
import com.felixalacampagne.account.service.TransactionService;

@RestController
@RequestMapping
@CrossOrigin(origins = "http://localhost:4200") // required because ng serve is on 4200 and standalone spring server is on 8080
public class AccountController {

   private final AccountService accountService;
   private final TransactionService transactionService;

   public AccountController(AccountService accountService,
         TransactionService transactionService) 
   {
      this.accountService = accountService;
      this.transactionService = transactionService;
   }

    @GetMapping("/greeting")
    public String getMessage()
    {
        return "Welcome to the Account Spring Boot Application running on Tomcat server\nand reading from Access database;\n";
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
}
