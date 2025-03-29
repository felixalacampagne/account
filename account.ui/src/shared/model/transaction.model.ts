// src/shared/model/transaction.model.ts
export class TransactionItem
{
    public accid : number = -1;
    public comment : string = '';
    public date : string = '';
    public amount : string = '';
    public amountfmtd : string = '';
    public type : string = '';
    public id: string = '';
    public locked: boolean = false;
    public token: string = '';
    public balance : string = '';
    public statementref : string = "";
    constructor()
    {
        
    }
    
    copy(item: TransactionItem)
    {
      this.accid = item.accid;
      this.comment = item.comment;
      this.date = item.date;
      this.amount = item.amount;
      this.amountfmtd = item.amountfmtd;
      this.balance = item.balance;
      this.type = item.type;
      this.id = item.id;
      this.locked = item.locked;
      this.statementref = item.statementref;
      this.token = item.token;

    }
}

export class AddTransactionItem extends TransactionItem
{
   public transferAccount: number | undefined | null;
   public communication : string | undefined | null;     // Only used if transferAccount or cptyAccountNumber are present
   public cptyAccount   : string | undefined | null;       // Only used if transferAccount is missing and communication and cptyAccountNumber are present
   public cptyAccountNumber: string | undefined | null; // Only used if transferAccount is missing and communication and cptyAccount are present

   constructor()
   {
      super();
   }

   override copy(item: AddTransactionItem)
   {
      super.copy(item);
      this.transferAccount = item.transferAccount;
      this.communication = item.communication;
      this.cptyAccount = item.cptyAccount;
      this.cptyAccountNumber = item.cptyAccountNumber;
   }
}

export class Transactions
{
   public transactions: TransactionItem[] = [];
   public currentpage: number = -1;
   public rowcount: number = -1;
}
