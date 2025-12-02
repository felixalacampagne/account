// src/app/accounts/accounts.component.ts

import { Component, inject, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DeviceDetectorService } from 'ngx-device-detector';
import { AccountDetail } from 'src/shared/model/accountdetail.model';
import { AccountService } from 'src/shared/service/account.service';
import { AccountDeleteConfirmDialog } from './accountdel-confirm-modal.component';
import { AccountEditMatDialog } from '../account-edit-mat/account-edit-mat.dialog';

@Component({
    selector: 'accounts',
    imports: [],
    templateUrl: './accounts.component.html',
    styleUrls: [
      '../../sass/account-styles.scss',
      '../app.component.css',
      './accounts.component.css'
    ]
})
export class AccountsComponent  implements OnInit {
   accounts : AccountDetail[] = [];
   dialog = inject(MatDialog);

   constructor(private accountService: AccountService,
        private deviceService: DeviceDetectorService)
   {
   }

   ngOnInit(): void 
   {
      this.getAccounts();
   }

   getAccounts()
   {
      console.log("getAccounts: Starting");
          
      this.accountService.getAccountDetails().subscribe({
         next: (res)=>{
            let  ressos : AccountDetail[] | undefined;
            ressos = res;

            if(!ressos)
            {
            console.log("getAccounts: variable is not initialized");
            }
            else
            {
            this.accounts = ressos; 
            console.log("getTransferAccounts: accounts contains " + this.accounts.length + " items.");
            }
         },
         error: (err)=>{
              console.log("getAccounts: An error occured during subscribe" + err);
         } ,
         complete: ()=>{console.log("getAccounts: loading completed");}
      });
    
      console.log("getAccounts: Finished");
   }

   isAccounts() : boolean
   {
      return this.accounts.length > 0;
   }

   addAccount() 
   {
      let newItem = new AccountDetail();
      this.editAccount(newItem);
   }

   editAccount(acc : AccountDetail) : void
   {
      const position = this.deviceService.isMobile()? { top:'60px', left: '15px'} : {} ;

      this.dialog.open(AccountEditMatDialog, { data: acc, position: position} )  
         .afterClosed().subscribe(result => 
         {
            console.log("editAccount: dialog closed: " + JSON.stringify(result));
            if(result == 'SUBMIT_COMPLETED')
            {
               this.getAccounts();
            }
            else if(result == 'SUBMIT_DELETE')
            {
               this.delAccountConfirm(acc);
            }
         });
   }

   delAccountConfirm(acc : AccountDetail) 
   {
      this.dialog.open(AccountDeleteConfirmDialog, {data :acc} )
         .afterClosed().subscribe(result => 
         {
            console.log("delAccountConfirm: dialog closed: " + JSON.stringify(result, null, 2));
            if(result == 'DELETE_OK')
            {
               this.delAccount(acc);      
            }
         });
   }
   
   delAccount(acc : AccountDetail) 
   {
      console.log("delAccount: start: transfer account:" + JSON.stringify(acc));
      this.accountService.deleteAccount(acc).subscribe(
      {
         next: (result)=>
         {
            console.log("delAccount: result:" + JSON.stringify(result));
            this.getAccounts();
         },
         error: (err)=>{
             console.log("delAccount: An error occured during subscribe" + JSON.stringify(err));
             } ,
         complete: ()=>{
            this.accountService.notifyAccountModified(acc.id);
            console.log("delAccount: complete");
         }
       });      
   }   
}
