package com.felixalacampagne.account.model;

import java.util.List;

// TODO: Merge Transactions and TransactionItem
public class Transactions
{
private final List<TransactionItem> transactions;

   public List<TransactionItem> getTransactions()
   {
      return transactions;
   }

//   public void setTransactions(List<TransactionItem> transactions)
//   {
//      this.transactions = transactions;
//   }

   public Transactions(List<TransactionItem> transactions)
   {
      this.transactions = transactions;
   }
}
