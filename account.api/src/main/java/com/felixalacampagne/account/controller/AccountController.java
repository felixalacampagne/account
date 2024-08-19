package com.felixalacampagne.account.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felixalacampagne.account.model.AccountItem;
import com.felixalacampagne.account.service.AccountService;
import com.felixalacampagne.account.service.TransactionService;

@RestController
@RequestMapping
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
    
    
    // Seems Spring is happy returning objects which get converted to json
    @GetMapping("/listaccount")
    public List<AccountItem> getAccounts() 
    {  
       return this.accountService.getAccountList();  
    }
    
//    Could use @RequestParam(name="name", required=false, defaultValue="World" to supply as url ? values
    @GetMapping("/listtransaction/{accountid}")
    public String getTransactions(@PathVariable Long accountid) // This needs to receive an account id
    {
       return this.transactionService.getTransactions(accountid);
    }
}
