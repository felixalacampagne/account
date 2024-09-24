package com.felixalacampagne.account.service;

public class AccountException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public AccountException(String message)
   {
      super(message);
   }
}
