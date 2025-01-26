import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DeviceDetectorService } from 'ngx-device-detector';
import { TransferAccountItem } from 'src/shared/model/transferaccountitem.model';
import { AccountService } from 'src/shared/service/account.service';
import { TransferaccountEditMatDialog } from '../transferaccount-edit-mat/transferaccount-edit-mat.dialog';

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
  dialog = inject(MatDialog);

  constructor(private accountService: AccountService,
        private deviceService: DeviceDetectorService)
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
   let newItem = new TransferAccountItem();
   this.editTransferAccount(newItem);
  }

  editTransferAccount(ta : TransferAccountItem) : void
  {
    const position = this.deviceService.isMobile()? { top:'60px', left: '15px'} : {} ;

    // these have no effect on the height of the dialog
    //   ,height: '1000px',
    //   minHeight: '1000px'
    this.dialog.open(TransferaccountEditMatDialog, { 
        data: ta,
        position: position
    } ) //;     // returns MatDialogRef  
    .afterClosed().subscribe(result => {
        console.log("editTransferAccount: dialog closed: " + JSON.stringify(result, null, 2));
        if(result == 'SUBMIT_COMPLETED')
        {
          this.getTransferAccounts();
        }
      });
  }
}
