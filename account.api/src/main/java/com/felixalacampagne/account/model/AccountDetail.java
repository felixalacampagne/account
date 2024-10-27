package com.felixalacampagne.account.model;



public class AccountDetail extends AccountItem
{

   private String address;
   private String code;
   private String currency;
   private String format;
   private Long order;
   private String bic;
   private String telephone;
   private String token;

   public AccountDetail()
   {
   }

   public AccountDetail(
         long id, String name,
         String address, String code, String currency, String format, Long order,
         String statement, String bic, String telephone,
         String token)
   {
      super(id, name, statement);
      this.address = address;
      this.code = code;
      this.currency = currency;
      this.format = format;
      this.order = order;
      this.bic = bic;
      this.telephone = telephone;
      this.token = token;
   }

   public String getAddress()
   {
      return address;
   }

   public String getCode()
   {
      return code;
   }

   public String getCurrency()
   {
      return currency;
   }

   public String getFormat()
   {
      return format;
   }

   public Long getOrder()
   {
      return order;
   }

   public String getBic()
   {
      return bic;
   }

   public String getTelephone()
   {
      return telephone;
   }

   public String getToken()
   {
      return token;
   }

   @Override
   public String toString()
   {
      return "AccountDetail [id=" + getId() + ", name=" + getName() + ", code=" + code + ", address=" + address
            + ", currency=" + currency + ", format=" + format + ", statement=" + getStatementref() + ", bic=" + bic
            + ", telephone=" + telephone + ", order="
            + order + ", token=" + token + "]";
   }

}
