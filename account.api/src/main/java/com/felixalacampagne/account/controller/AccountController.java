package com.felixalacampagne.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felixalacampagne.account.service.AccountService;

@RestController
@RequestMapping
public class AccountController {

   private final AccountService accountService;

   public AccountController(AccountService accountService) {
      this.accountService = accountService;
   }

    @GetMapping("/")
    public String getMessage()
    {
        return "Spring Boot Application running on Tomcat server\nand reading from Access database;\n" + accountService.getAccounts();
    }
}
