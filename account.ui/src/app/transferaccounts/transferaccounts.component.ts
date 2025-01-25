import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { TransferAccountItem } from 'src/shared/model/transferaccountitem.model';
import { AccountService } from 'src/shared/service/account.service';

@Component({
  selector: 'transferaccounts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transferaccounts.component.html',
  styleUrls: ['./transferaccounts.component.css'
            , '../../sass/account-styles.scss'
            , '../app.component.css']  
})
export class TransferaccountsComponent implements OnInit {

  transferAccounts : TransferAccountItem[] = [];

  constructor(private accountService: AccountService)
  {
  }

  ngOnInit(): void 
  {
    this.getTransferAccounts();
  }

  getTransferAccounts()
  {
      console.log("getTransferAccounts: Starting");
          
      this.accountService.getTransferAccounts().subscribe({
          next: (res)=>{
                let  ressos : TransferAccountItem[] | undefined;
                ressos = res;

                if(!ressos)
                {
                  console.log("getTransferAccounts: variable is not initialized");
                }
                else
                {
                  this.transferAccounts = ressos; 
                  console.log("getTransferAccounts: standorders contains " + this.transferAccounts.length + " items.");
                }
              },
          error: (err)=>{
              console.log("getTransferAccounts: An error occured during subscribe" + err);
              } ,
          complete: ()=>{console.log("getTransferAccounts: loading completed");}
        });
    
      console.log("getTransferAccounts: Finished");

  }

  isTransferAccounts() : boolean
  {
    return this.transferAccounts.length > 0;
  }

  addTransferAccount() 
  {
  }

  editTransferAccount(ta : TransferAccountItem) : void
  {
  }
}
