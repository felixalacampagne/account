package com.felixalacampagne.account.model;

import java.util.List;

public class Transactions
{
private final List<TransactionItem> transactions;
private final Long currentpage;
private final Long rowcount;

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

   public Transactions(List<TransactionItem> transactions, Long currentpage, Long rowcount)
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
