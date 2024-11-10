export class TransactionItem
{
    public accid : number = -1;
    public comment : string = '';
    public date : string = '';
    public amount : string = '';
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
   constructor()
   {
      super();
   }

   override copy(item: AddTransactionItem)
   {
      super.copy(item);
      this.transferAccount = item.transferAccount;
   }
}