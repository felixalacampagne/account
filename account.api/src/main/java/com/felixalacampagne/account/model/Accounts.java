package com.felixalacampagne.account.model;

import java.util.List;


// TODO Merge Accounts and AccountItem
public class Accounts
{
private final List<AccountItem> accounts;

   public Accounts(List<AccountItem> accounts)
   {
      this.accounts = accounts;
   }

   public List<AccountItem> getAccounts()
   {
      return accounts;
   }

//   public void setAccounts(List<AccountItem> accounts)
//   {
//      this.accounts = accounts;
//   }
}
