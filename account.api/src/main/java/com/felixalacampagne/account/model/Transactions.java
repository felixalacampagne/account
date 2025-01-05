package com.felixalacampagne.account.model;

import java.util.List;

// TODO: Merge Transactions and TransactionItem
public class Transactions
{
private final List<TransactionItem> transactions;
private final Long rowcount;
private final Long currentpage;

   public Long getCurrentpage()
{
   return currentpage;
}

   public List<TransactionItem> getTransactions()
   {
      return transactions;
   }

//   public void setTransactions(List<TransactionItem> transactions)
//   {
//      this.transactions = transactions;
//   }

   public Transactions(List<TransactionItem> transactions, Long rowcount, Long currentpage)
   {
      this.transactions = transactions;
      this.rowcount = rowcount;
      this.currentpage = currentpage;
   }

   public Long getRowcount()
   {
      return rowcount;
   }
}
