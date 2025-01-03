// app/transactions/transactions.component.ts
import { Component, OnInit, ChangeDetectorRef, ViewChild, Input, SimpleChanges } from '@angular/core';
import { FormsModule} from '@angular/forms';
import { CommonModule /*, DatePipe */ } from '@angular/common';
import { NgbModal, ModalDismissReasons, NgbModalRef, NgbModule, NgbDateAdapter, NgbDateNativeAdapter} from '@ng-bootstrap/ng-bootstrap';
import { NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DeviceDetectorService } from 'ngx-device-detector';

import {environment} from '../../environments/environment';

import { accountNgbDateParserFormatter, ddmmyyyyNgbDateParserFormatter, isoNgbDateParserFormatter } from '../../shared/datepickformatter';
import {AccountService} from '../../shared/service/account.service';
import {DateformatService} from '../../shared/service/dateformat.service';
import {AccountItem} from '../../shared/model/accountitem.model';
import {AddTransactionItem, TransactionItem} from '../../shared/model/transaction.model';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { Html5QrcodeResult } from 'html5-qrcode/esm/core';
import { RouterModule, RouterOutlet } from '@angular/router'; // for 'routerlink is not a property of button'
import { TxnDelConfirmDialog } from './txndel-confirm-modal.component';
import { TfrAccountItem } from 'src/shared/model/tfraccountitem.model';

// Getting the phoneaccount list to look acceptable is going to be tricky. Apparently the select...option can't
// be formatted so making it into a nicely formatted list of name and number is not going to work. 
// Bootstrap has a 'dropdown' but inevitably the examples for it really suck and offer no explanation of how
// to get the app to respond to selection of an item in the list. The examples also use UL/LI and A which
// doesn't seem to be formattable into columns either. I did find an SO post which sort of displays items 
// in columns but still no clue how to select one of the items in the list. Plus the list appear in a scrollable
// window as it is quite long... would be nice to have a box to type into which scrolls to the first matching account.
// Obviously given how difficult it is to get basic things to work the chances of this happening are remote!
// This is the html using the BS dropdown:
// <div class="acc-modal-row">
// <div ngbDropdown class="d-inline-block">
//    <button class="btn btn-outline-primary" id="dropdownBasic1" ngbDropdownToggle>Counterparty accounts</button>
//    <div ngbDropdownMenu aria-labelledby="dropdownBasic1" style="width: 600px;">
//      <div ngbDropdownItem class="d-flex justify-content-between">
//        <div></div>
//        <div></div>
//      </div>
//      <div *ngFor="let trnAcc of transferAccounts" class="d-flex justify-content-between">
//        <div>
//          {{trnAcc.cptyAccountName}}
//        </div>
//        <div>
//          {{trnAcc.cptyAccountNumber}}
//        </div>
//      </div>
//    </div>
//  </div>
// </div>
// const dateFormatJson: string = 'yyyy-MM-dd';

// WARNING: 'standalone: true' means the component must not be put in app.module and all imports must be duplicated
// in the imports sections of @Component otherwise many inexplicable errors will occur, eg.
// NG8002: Can't bind to 'ngModel' since it isn't a known property of 'select'
@Component({
  selector: 'transactions',
  standalone: true,
  imports: [FormsModule, CommonModule, NgbModule, RouterModule, RouterOutlet ],
  templateUrl: './transactions.component.html',
  styleUrls: ['../../sass/account-styles.scss', '../app.component.css', './transactions.component.css'],
  providers: [{provide: NgbDateParserFormatter, useClass: accountNgbDateParserFormatter},
              { provide: NgbDateAdapter, useClass: NgbDateNativeAdapter }
  ]
})

export class TransactionsComponent implements OnInit {

   // accid is set via the route URL which is detected via ngOnChanges. The id is used to load the accountitem from
   // the server - cannot rely on the list previously loaded by the main app as is it is cleared by the refresh button
   // and there is no way to know when it has been reloaded.
   // The loadaccount method has to trigger the loadtransactions from within its subscribe lambda as that is
   // the only way to know when the accountitem has been loaded. Its mind blowingly clumsy but at least
   // it appears to work including when the refresh button is used.
   @Input() accid!: number;
   
  
   @ViewChild('closebutton') closebutton: any;   
   modalReference: NgbModalRef | undefined;

   activeaccount!: AccountItem; 
   transferAccounts!: TfrAccountItem[] | undefined;
   isTransfer: boolean = false;
   transactions: TransactionItem[] = [];
   checkTransaction: TransactionItem | undefined;
   checkLoading: boolean = false;
   inprogress: boolean = false;
   public submitted: boolean = false;
   public defaultdate: string = '';
   envName: string = '';

   txDate: Date = new Date('1970-12-25'); // NgbDateStruct = {year: 1970, month: 12, day: 25};
   txUpdDate: Date = new Date('1970-12-25'); // NgbDateStruct = {year: 1970, month: 12, day: 25};
   txType: string;
   txComment: string = '';
   txAmount: string = '';
   txTfrAccount: TfrAccountItem | undefined;
   txCommunication : string = '';
   txCptyName: string = '';
   txCptyNumber: string = '';
   txPastearea: string = '';
   closeResult: string = '';
   html5QrcodeScanner: Html5QrcodeScanner | undefined; // Only defined while a scan is being performed
   desktopDisplay: boolean = false;
   // The modal code in .html will not compile if updateTxn is set to undefined since it
   // references 'this.updateTxn'. Obviously it should only consider the value when it is displayed
   // however I have no idea how to get it to do this. Thus 'origupdTxn' is the value to check to
   // determine whether an edit is really in progress.
   updateTxn: TransactionItem = new TransactionItem(); // Edited valeus of transaction beign updated
   origupdTxn:TransactionItem | undefined; // Original values of transaction being updated.
   public txnTypes: string[] = [];
   pageNumber: number = 0;

   constructor(private accountService: AccountService,
      private cd: ChangeDetectorRef,
      // private datePipe: DatePipe,
      private modalService: NgbModal,
      private deviceService: DeviceDetectorService,
      private datfmt : DateformatService)
   {
      this.envName = environment.envName;
      this.txnTypes = this.accountService.txnTypes;
      // This is only necessary because the ngModel attribute breaks the selected behaviour of the option tag
      this.txType = this.txnTypes[0];
      this.resetDatepicker();
      this.desktopDisplay = this.deviceService.isDesktop();
   }


   ngOnInit() 
   {
      console.log('TransactionsComponent.ngOnInit: start');
      const d: Date = new Date();  
      this.txDate = d; //{year: d.getFullYear(), month: d.getMonth() + 1, day: d.getDate()};
      console.log("TransactionsComponent.ngOnInit: finish");
   }

   resetDatepicker()
   {
      const d: Date = new Date();  
      this.txUpdDate = d; // {year: d.getFullYear(), month: d.getMonth() + 1, day: d.getDate()}; 
   }

   // Call this to display the modal. 'content' is the name of the 'template' containing the elements to be displayed in the modal, I think
   open(content: any, txn: TransactionItem) 
   {
      this.origupdTxn = txn;
      this.updateTxn = new TransactionItem();
      this.updateTxn.copy(this.origupdTxn);
      console.log("open: txn:" + JSON.stringify(this.updateTxn, null, 2));
      // Date picker field is somehow tied to content of this.txDate which is some sort of date object
      // so must the txn date into the date object... Date seems able to handle the date string in TransactionItem 
      let d : Date = new Date(this.updateTxn.date); // ISO date format, ie. YYYY-MM-DD
      console.log("open: txn orig date:" + d);
      this.txUpdDate = d; // {day: d.getDate(), month: d.getMonth()+1, year: d.getFullYear()}; 
      
      this.modalReference = this.modalService.open(content);

      this.modalReference.result.then(
         (result) => {
            console.log("open:modalReference:result");
            // One maybe the updated transaction will come from the modal
            this.updmodalCloseAction(`${result}`, this.updateTxn);
         }, 
         (reason) => {
            console.log("open:modalReference:reason");
            this.updmodalCloseAction( `${this.getDismissReason(reason)}`, this.updateTxn);
         }
      );
  }

   private getDismissReason(reason: any): string {
      if (reason === ModalDismissReasons.ESC) {
      return 'CANCEL';
      } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'CANCEL';
      } else {
      return `${reason}`;
      }
   }

   private updmodalCloseAction(reason : string, updtxn : TransactionItem) 
   {
      console.log("updmodalCloseAction: reason: " + reason);
      if(reason == "UPDATE")
      {
         // updtxn contains the new values except for the date
         // since the datepicker sets a value in txDate which needs to be mapped
         // back into the TransactionItem format
         let updDate : Date = this.txUpdDate; //new Date(this.txUpdDate.year, this.txUpdDate.month-1, this.txUpdDate.day);
         updtxn.date = this.datfmt.jsonFormat(updDate); // this.datePipe.transform(updDate, dateFormatJson) ?? '';         

         console.log("updmodalCloseAction: updating transaction:  " + JSON.stringify(updtxn, null, 2));
         this.updatetransaction(updtxn);
      }
      else if(reason == "DELETE")
      {
         this.delTxnConfirm(updtxn);
      }

      this.resetDatepicker();
   }

   delTxnConfirm(updtxn : TransactionItem) 
   {
      // Need a way to pass a message to be displayed to the confirmation dialog
      let modalReference: NgbModalRef = this.modalService.open(TxnDelConfirmDialog);
      modalReference.result.then((result) => {
         console.log("delTxnConfirm:modalReference:result");
         this.delTxnConfirmCloseAction(`${result}`, this.updateTxn);
       }, (reason) => {
         console.log("delTxnConfirm:modalReference:reason");
         this.delTxnConfirmCloseAction( `${this.getDismissReason(reason)}`, this.updateTxn);
       });
   }
   
   private delTxnConfirmCloseAction(reason : string, updtxn : TransactionItem) 
   {
      console.log("delTxnConfirmCloseAction: reason: " + reason);
      if(reason == "OK")
      {
         console.log("delTxnConfirmCloseAction: deleting transaction:  " + JSON.stringify(updtxn, null, 2));
         this.delTransactionToDB(updtxn);
      }
   }
      
   ngOnChanges(changes: SimpleChanges ) 
   {
      console.log("TransactionsComponent.ngOnChanges: enter: " + JSON.stringify(changes, null, 2));
      for (const propName in changes) 
      {
         console.log("TransactionsComponent.ngOnChanges: propName:" + propName);
         const chng = changes[propName];
         if(propName === 'accid')
         {
            this.resetTransfer();  // must reload list to ensure right account is excluded
            // console.log("TransactionsComponent.ngOnChanges: setting checkTransaction undefined");
            this.checkTransaction = undefined; // Hide the value until a new one is loaded
            this.loadAccount(chng.currentValue);
         }
      }
   }

   loadAccount(id : number)
   {
     console.log("TransactionsComponent.loadAccount: Starting: id " + id);
         
     this.accountService.getAccount(id).subscribe({
         next: (res)=>{
            if(!res)
            {
              console.log("TransactionsComponent.loadAccount: variable is not initialized");
            }
            else
            {
               this.loadTransactions(res);
               // this.getCheckedBalance(res);
            }
          },
         error: (err)=>{
             console.log("TransactionsComponent.loadAccount: An error occured during subscribe: " + JSON.stringify(err, null, 2));
             } ,
         complete: ()=>{console.log("TransactionsComponent.loadAccount: completed");}
      });
   
     console.log("TransactionsComponent.loadAccount:Finished");
   }

   // radio buttons
   setTransfer(trans : boolean)
   {
      this.isTransfer = trans;
      console.log("setTransfer: isTransfer=" + this.isTransfer); 
      if(this.isTransfer)
      {
         if(!this.transferAccounts) {
            this.loadTransferAccounts();
         }
      }
   }

   // switch
   toggleTransferAccounts() {
      if(this.isTransfer)
      {
         this.isTransfer = false;
         this.txTfrAccount = undefined;
      }
      else
      {
         this.isTransfer = true;
         if(!this.transferAccounts) {
            this.loadTransferAccounts();
         }

      }
      console.log("TransactionsComponent.toggleTransferAccounts: isTransfer: " + this.isTransfer);
   }
   canShowTransferAccounts() {
      return((this.isTransfer == true) && this.transferAccounts);
   }
   loadTransferAccounts() {
      const id : number = this.activeaccount.id;
      console.log("TransactionsComponent.showTransferAccounts: Starting: id " + id);
      this.inprogress = true;   
      this.accountService.getAccountsForTransfer(this.activeaccount).subscribe({
         next: (res) => {
            if(!res)
            {
              console.log("TransactionsComponent.showTransferAccounts: variable is not initialized");
            }
            else
            {
               this.transferAccounts = res;
            }
         },
         error: (err) => {
            console.log("TransactionsComponent.showTransferAccounts: An error occured during subscribe: " + JSON.stringify(err, null, 2));
            this.inprogress = false; // error or compete NOT error then complete
        } ,
         complete: () => {
            console.log("TransactionsComponent.showTransferAccounts: completed");
            this.inprogress = false;
         }
       });
    
      console.log("TransactionsComponent.showTransferAccounts:Finished");      
   }
   
   getCheckedBalance(acc : AccountItem)
   {
      console.log("TransactionsComponent.getCheckedBalance: Starting: " + JSON.stringify(acc, null, 2));
      if(acc.id < 0)
         return;

      this.accountService.getCheckedBalance(acc).subscribe({
         next: (res)=> {
            if(!res)
            {
               console.log("TransactionsComponent.getCheckedBalance: variable is not initialized");
            }
            else
            {
               this.checkTransaction = res;
               console.log("TransactionsComponent.getCheckedBalance: returned");
            }
         },
         error: (err)=>{
            console.log("TransactionsComponent.getCheckedBalance: An error occured during getCheckedBalance subscribe: " + JSON.stringify(err, null, 2));
         } ,
         complete: ()=>{
            console.log("TransactionsComponent.getCheckedBalance: getCheckedBalance loading completed");
         }
      });
   
      console.log("TransactionsComponent.getCheckedBalance:Finished");
   }

loadTransactions(acc : AccountItem, page: number = 0)
{
   console.log("TransactionsComponent.loadTransactions: Starting: " + JSON.stringify(acc, null, 2));
   if(acc.id < 0)
      return;
      
   this.accountService.getTransactions(acc, page).subscribe({
      next: (res)=>{
            if(!res)
            {
              console.log("TransactionsComponent.loadTransactions: variable is not initialized");
            }
            else
            {
              this.activeaccount = acc;
              this.transactions = res;
              this.pageNumber = page;
              console.log("TransactionsComponent.loadTransactions: transactions contains " + this.transactions.length + " items.");
            }
          },
      error: (err)=>{
          console.log("TransactionsComponent.loadTransactions: An error occured during loadTransactions subscribe: " + JSON.stringify(err, null, 2));
          } ,
      complete: ()=>{
         console.log("TransactionsComponent.loadTransactions: loadTransactions loading completed");
      }
   });

   console.log("TransactionsComponent.loadTransactions:Finished");
}

nextPage() 
{
   this.loadTransactions(this.activeaccount, this.pageNumber + 1);
}

prevPage() 
{
   let p = this.pageNumber;
   if(p < 1)
   {
      return;
   }
   p = p - 1;
   this.loadTransactions(this.activeaccount, p);
}

firstPage()
{
   this.loadTransactions(this.activeaccount);
}

calcCheckedBalance()
{
   console.log("TransactionsComponent.calcCheckedBalance: Starting");
   this.checkLoading = true;
   this.accountService.calcChecked(this.activeaccount).subscribe( {
      next: (res)=>{
          console.log("TransactionsComponent.calcCheckedBalance: Response: " + res);
          this.checkTransaction = res;
          },
      error: (err)=>{
          console.log("TransactionsComponent.calcCheckedBalance: An error occured during calcCheckedBalance subscribe:" + JSON.stringify(err));
          } ,
      complete: ()=>{
         this.checkLoading = false;
         console.log("TransactionsComponent.calcCheckedBalance: completed");
      }
   });
   console.log("TransactionsComponent.calcCheckedBalance: Finished");
}

addTransactionToDB(txn :AddTransactionItem)
{
  console.log("TransactionsComponent.addTransactionToDB: Starting");
  this.inprogress = true;
  this.accountService.addTransaction(txn).subscribe( {
      next: (res)=>{
          console.log("TransactionsComponent.addTransactionToDB: Response: " + res);
          // Must wait for add to complete before loading new transaction list
          this.loadTransactions(this.activeaccount);
          // Reset amount to prevent double entry
          this.txAmount = '';
          this.txPastearea = '';

         // If a transfer was done then the last communication might have been updated.
         // Only way to refresh the list at the moment is to reset it...
         if(this.txTfrAccount)
         {
            this.resetTransfer();           
         }
      },
      error: (err)=>{
          console.log("TransactionsComponent.addTransactionToDB: An error occured during addTransactionToDB subscribe:" + JSON.stringify(err));
          this.inprogress = false; // error or compete NOT error then complete
      } ,
      complete: ()=>{
         console.log("TransactionsComponent.addTransactionToDB: completed");
         this.inprogress = false;
      }
   });

  console.log("TransactionsComponent.addTransactionToDB:Finished");
}

resetTransfer()
{
   this.isTransfer = false;
   this.txTfrAccount = undefined; 
   this.transferAccounts = undefined;   
   this.txCommunication = '';
   this.txCptyName = '';
   this.txCptyNumber = '';
}

addtransaction()
{
   if(this.activeaccount == null)
   {
      console.log("Account is not set, unable to add transaction.");
      return;
   }

   let newent : AddTransactionItem = new AddTransactionItem();
   let d = this.txDate; // new Date(this.txDate.year, this.txDate.month-1, this.txDate.day);
   newent.accid = this.activeaccount.id;
   newent.amount = this.txAmount;
   newent.comment = this.txComment;

   // With new Typescript cannot just assign return value to a string!
   // Using ternary operator is too clumsy for dealing with the return from a function
   // Apparently the '??' means use the result unless it's undefined or null and then use the value after the ??
   newent.date = this.datfmt.jsonFormat(d); // this.datePipe.transform(d, dateFormatJson) ?? '';
   newent.type = this.txType;
 
   if(this.canShowTransferAccounts()) // Only add these if 'Transfer' mode is enabled
   {
      if(this.txTfrAccount)
      {
         newent.transferAccount = this.txTfrAccount.id;
      }
      newent.communication = this.txCommunication;
      newent.cptyAccount = this.txCptyName;
      newent.cptyAccountNumber = this.txCptyNumber;
   }
   console.log("Date: " + newent.date);
   console.log("Type: " + newent.type);
   console.log("Comment: " + newent.comment);;
   console.log("Amount: " + newent.amount);  
   this.addTransactionToDB(newent); 
}

lockedChange()
{
  //console.log("TransactionsComponent.lockedChange: locked:" + this.updateTxn.locked);
  if(this.updateTxn.locked && !this.updateTxn.statementref)
  {
    //console.log("TransactionsComponent.lockedChange: set ref:" + this.activeaccount.statementref);
    this.updateTxn.statementref = this.activeaccount.statementref;  
  }
}

delTransactionToDB(txn : TransactionItem) 
{
   console.log("TransactionsComponent.delTransactionToDB: Starting");
   this.inprogress = true;
   this.accountService.deleteTransaction(txn).subscribe( {
       next: (res)=>{
           console.log("TransactionsComponent.delTransactionToDB: Response: " + res);
           // Must action for add to complete before loading new transaction list
           this.loadTransactions(this.activeaccount, this.pageNumber);
           // Reset amount to prevent double entry
           this.txAmount = '';
           this.txPastearea = '';
           },
       error: (err)=>{
           console.log("delTransactionToDB.updTransactionToDB: An error occured during updTransactionToDB subscribe:" + JSON.stringify(err));
           this.inprogress = false; // error or compete NOT error then complete
         } ,
       complete: ()=>{
         console.log("TransactionsComponent.delTransactionToDB: completed");
         this.inprogress = false;
         }
    });
 
   console.log("TransactionsComponent.delTransactionToDB:Finished");   
}

updTransactionToDB(txn : TransactionItem, showcheckedbal: boolean)
{
  console.log("TransactionsComponent.updTransactionToDB: Starting");
  this.inprogress = true;
  this.accountService.updateTransaction(txn).subscribe( {
      next: (res)=>{
         console.log("TransactionsComponent.updTransactionToDB: Response: " + res);
         // Must wait for add to complete before loading new transaction list
         this.loadTransactions(this.activeaccount, this.pageNumber);
         // Reset amount to prevent double entry
         this.txAmount = '';
         this.txPastearea = '';
         if(showcheckedbal)
         {
            this.getCheckedBalance(this.activeaccount);
         }
      },
      error: (err)=>{
          console.log("TransactionsComponent.updTransactionToDB: An error occured during updTransactionToDB subscribe:" + JSON.stringify(err));
          this.inprogress = false; // error or compete NOT error then complete
      } ,
      complete: ()=>{
         console.log("TransactionsComponent.updTransactionToDB: completed");
         this.inprogress = false; // error or compete NOT error then complete
      }
   });

  console.log("TransactionsComponent.updTransactionToDB:Finished");
}

// Specify the updated transaction in case I ever manage to make the modal into a separate component
updatetransaction(updtxn : TransactionItem)
{
   if(!this.origupdTxn)
   {
      console.log("No transaction is being updated");
      return;
   }   
   if(!(updtxn.token === this.origupdTxn.token) )
   {
      console.log("Invalid update request: Tokens do not match");
      return;     
   }

   // Problem comparing the dates - the old date has a time value of +1hr but the new one
   // has 0hr. So simpler to compare the strings
   let oldDate: Date = new Date(this.origupdTxn.date);
   let oldDatestr: string = this.datfmt.jsonFormat(oldDate); // this.datePipe.transform(oldDate, dateFormatJson) ?? '';

   // Could verify that something was changed - will need to keep track of the original TransactionItem
   if((updtxn.amount === this.origupdTxn.amount)
      && (updtxn.comment === this.origupdTxn.comment)
      && (updtxn.type === this.origupdTxn.type)
      && (updtxn.date === oldDatestr)
      && (updtxn.locked === this.origupdTxn.locked)
      && (updtxn.statementref === this.origupdTxn.statementref)
   )
   {
      console.log("No values were changed, nothing to update.");
      return;
   }

   // Should probably also verify that 'valid' values are present but this is not done for add...

   console.log("Date:    new:" + updtxn.date + " old:" + oldDatestr);
   console.log("Type:    new:" + updtxn.type + " old:" + this.origupdTxn.type);
   console.log("Comment: new:" + updtxn.comment + " old:" + this.origupdTxn.comment);
   console.log("Amount:  new:" + updtxn.amount + " old:" + this.origupdTxn.amount);  
   console.log("Locked:  new:" + updtxn.locked + " old:" + this.origupdTxn.locked);   
   console.log("StRef:   new:" + updtxn.statementref + " accref:" + this.activeaccount.statementref); 
   
   // Only update the checked balance when it is changed to locked
   // this is not really correct but it is consistent with what the backend is doing at the moment.
   // It assumes that an unlock is temporary for adjustment, eg. of the comment, and the txn
   // will be re-locked immediately.
   let showcheckedbal: boolean = false;
   if(!this.origupdTxn.locked && updtxn.locked)
   {
      showcheckedbal  = true;
   }   
   this.updTransactionToDB(updtxn, showcheckedbal);

   // If the locked state was changed to locked and the txn statementref is present
   // and different to the account statementref then the account statementref should be updated.
   // This will require an update on the server as the accountitem is loaded each time the 
   // update dialog is displayed.
   if((!this.origupdTxn.locked && updtxn.locked)
        && updtxn.statementref 
        && (updtxn.statementref != this.activeaccount.statementref))
   {
      this.activeaccount.statementref = updtxn.statementref;
      this.accountService.updateAccountStatementRef(this.activeaccount.id, this.activeaccount.statementref);
   }



   // NB updTransactionToDB refreshes the transaction list when the response is received.
   // Horribly ugly code, I guess there must be a better way of doing it but alas I
   // don't know what it is...
}

formatDateColumn(jsondate: string) : string
{
   return this.datfmt.listFormat(jsondate) ;   
}

// An EPC looks like this
// BCD
// 002
// 1
// SCT
//
// account name
// BE30776595425911
// EUR100.00
//
// memo text line
//


parseEPC(epc : string) : TransactionItem | undefined
{
  console.log('parseEPC: entry');
  let trans : TransactionItem | undefined;

  // Docs say replaceAll should exist but it fails to compile. VSCode says replace should replace
  // all but it doesn't. I guess something needs to be updated but not sure what!! Maybe
  // the wierd failure of the SCT check is also something to do with versions....
  epc = epc.replaceAll('\r', ''); //replace(/\r/g, '');
  const lines: string[] = epc.split("\n");

  // Inexplicably (lines[3] === 'SCT') is giving 'false' when lines[3] is 'SCT'
  if((lines[0] === 'BCD') && (lines[3] === 'SCT'))
  {
    // Name Comment (Account)
    trans = new TransactionItem();
    trans.comment = lines[5] + " " + lines[9] + lines[10] + " " + lines[6];
    trans.amount = lines[7].substring(3);
    trans.type = 'QRMP';
    console.log('parseEPC: successfully parsed to transaction');
  }
  else
  {
    console.log('Lines 0: [' + lines[0] + "] = " + (lines[0] === 'BCD'));
    console.log('Lines 3: [' + lines[3] + "] = " + (lines[3] === 'SCT'));
    console.log('parseEPC: invalid epc string: ' + epc);
  }
  console.log('parseEPC: exit');
  return trans;
}


cleanSComm( rawval : string) : string | undefined
{
   let scomm : string | undefined;
   console.log("cleanSComm: rawval: " + rawval);
   const scommre = /(?: *\+){0,3} *(\d) *(\d) *(\d) *\/* *(\d) *(\d) *(\d) *(\d) *\/* *(\d) *(\d) *(\d) *(\d) *(\d)(?: *\+){0,3} */;
   const replace =   '$1$2$3$4$5$6$7$8$9$10$11$12';
   if(rawval.match(scommre))
   {
      scomm =rawval.replace(scommre, replace);
      console.log("cleanSComm: clean val: " + scomm);
   }
   else
   {
      console.log("cleanSComm: scomm NO MATCH");
   }
   return scomm;
}

onPasteUpd(event: ClipboardEvent) {
   console.log("onPasteUpd: entry");
   const clipboardData = event.clipboardData; // || window.clipboardData;
   if(clipboardData !== null)
   {
     const clptxt = clipboardData.getData('text');
     const scomm : string | undefined = this.cleanSComm(clptxt);
     if(scomm)
     {
      console.log("onPasteUpd: replace clipboard content with cleaned scomm: " + scomm); 

      // Can't update the clipboard and do the paste with the new text.
      // Instead need to cancel the paste and then try to emulate what should
      // be happening. Forking crazy.
      // This BS seems to work except for some things don't work properly after, eg. it is not 
      // possible to revert the change, ie. Ctrl-Z. Still it is better than trying to edit
      // the structure communications by hand on the phone!!
      //clipboardData.setData('text', scomm);

      event.preventDefault();
   
      let element = event.target  as HTMLInputElement;;
      console.log("onPasteUpd: " + JSON.stringify(element, null, 2));
      let start = element.selectionStart as number;
      let end = element.selectionEnd as number;
      console.log("onPasteUpd: start,end:" + start + "," + end);

      const value = element.value;
      element.value = value.slice(0, start) + scomm + value.slice(end);
      console.log("onPasteUpd: text: orig: " + value + " new: " + element.value);
      element.selectionStart = element.selectionEnd = start + scomm.length;
     }
   }
   console.log("onPasteUpd: exit");
 }
 
// Adapted this so I can paste an EPC code into the memo field and have it parsed
// This relies on the EPC parser returning undefined if it fails to parse the
// clipboard content, in which case the normal paste action should take place
// To be seen whether it works on the phone!
onPaste(event: ClipboardEvent) {
  console.log("onPaste: entry");
  const clipboardData = event.clipboardData; // || window.clipboardData;
  if(clipboardData !== null)
  {
    const epc = clipboardData.getData('text');
    const txn : TransactionItem | undefined = this.parseEPC(epc);
    if(txn)
    {
      this.txAmount = txn.amount;
      this.txComment = txn.comment;
      this.txType = txn.type;
      //clipboardData.setData('text', '');
      event.preventDefault();
    }
  }
  console.log("onPaste: exit");
}

onPasteComm(event: ClipboardEvent)
{
   // Maybe want to filter the "+", "/" . " " of the 'structured" communications?
   // already done for the updated dialog
   this.onPasteUpd(event);
}

onChangeCptySelect(event : any)
{
   // event content is not useful
   // the 'ngModel' fields has been updated with the clicked item
   console.log("TransactionsComponent.onChangeCptySelect: txTfrAccount:" + JSON.stringify(this.txTfrAccount));
   if(this.txTfrAccount)
   {
      this.txCommunication = this.txTfrAccount.lastCommunication;
      this.txCptyName = this.txTfrAccount.cptyAccountName;
      this.txCptyNumber = this.txTfrAccount.cptyAccountNumber;
   }
   else
   {
      this.txCommunication = "";
      this.txCptyName = "";
      this.txCptyNumber = "";
   }
}

onScanSuccess(decodedText: string, decodedResult: Html5QrcodeResult) {
  // handle the scanned code as you like, for example:
  console.log('onScanSuccess: entry');
  if(this.html5QrcodeScanner != undefined) {
    console.log('onScanSuccess: stopping scanner');
    this.html5QrcodeScanner.clear();
    this.html5QrcodeScanner = undefined;
  }  
  console.log('onScanSuccess: Parsing result ' + decodedText);
  const txn : TransactionItem | undefined = this.parseEPC(decodedText);
  if(txn)
  {
   console.log('onScanSuccess: setting transaction details');
   this.txAmount = txn.amount;
   this.txComment = txn.comment;
   this.txType = txn.type;
  }
  console.log('onScanSuccess: exit');
}

onScanFailure(error: any) {
  // handle scan failure, usually better to ignore and keep scanning.
  // for example:
  //console.warn(`Code scan error = ${error}`);
}

doScan() {
   this.html5QrcodeScanner = new Html5QrcodeScanner("reader",
      { 
         fps: 10, 
         qrbox: {width: 250, height: 250}, 
         supportedScanTypes: [],
         showTorchButtonIfSupported: true,
         rememberLastUsedCamera: true,
         experimentalFeatures: 
         {
            // This requires the 'Shape Detection API' flag to be true. This is located buried in 
            // Settings>Apps>Safari>Advanced>Feature Flags>Shape Detection API. No clue if it makes any difference
            useBarCodeDetectorIfSupported: true
         } 
      },
      /* verbose= */ false);

  // Could not get it to work. It appeared to execute the first console.log of onScanSuccess and then vanish
  // literally without trace. Luckily I remembered something about 'this' getting lost especially in callbacks.
  // Eventually tracked the 'bind' thing down and whoopee it works now! It's a bit tricky to use with a PC camera
  // and an image on the iPhone but seems to work OK on the iPhone apart from always needing to ask permission.
  let callback = (this.onScanSuccess).bind(this);
  this.html5QrcodeScanner.render(callback,  this.onScanFailure);

   // // Found this as a suggestion to allow scanning of small(er) QR codes. The phone wont
   // // scan the small QR codes which have started appearing on invoices recently, which is a pity
   // // since the same phone can scan the codes from the banking app.
   // // Obviously it doesn't actually compile because 'zoom' is not a valid property,
   // // and autofocus already appears to be enabled by default.
   // // wait 2 seconds to guarantee the camera has already started to apply the focus mode and zoom...
   // setTimeout(() => 
   // {
   //    if(this.html5QrcodeScanner)
   //    {
   //       this.html5QrcodeScanner.applyVideoConstraints(
   //       {
   //          focusMode: "continuous"
   //          , advanced: [{ zoom: 2.0 }],
   //       } );
   //    }
   // }, 2000);

}

isTransactions() : boolean {
  return this.transactions.length > 0;
}

clearComment() {
  this.txComment = "";
}

}
