import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { DatePipe } from '@angular/common';
import {NgbModal, ModalDismissReasons, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import { NgbDatepickerConfig, NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DeviceDetectorService } from 'ngx-device-detector';

import {environment} from '../environments/environment';

import { isoNgbDateParserFormatter } from '../shared/datepickformatter';
import {AccountService} from '../shared/service/account.service';
import {AccountItem} from '../shared/model/accountitem.model';
import {TransactionItem} from '../shared/model/transaction.model';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { Html5QrcodeResult } from 'html5-qrcode/esm/core';
import { Version } from 'src/shared/model/version.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css', '../sass/account-styles.scss'],
  providers: [{provide: NgbDateParserFormatter, useClass: isoNgbDateParserFormatter}]
})

export class AppComponent implements OnInit {
   
@ViewChild('closebutton') closebutton: any;   
  modalReference: NgbModalRef | undefined;

  title: string = 'Account';
  versiontxt: string = '';
  version : Version | undefined;
  accounts: AccountItem[] = [];
  transactions: TransactionItem[] = [];
  activeaccount: AccountItem = new AccountItem();
  public submitted: boolean = false;
  public defaultdate: string = '';
  envName: string = '';
  uiversion: string ='';
  txDate: NgbDateStruct = {year: 1970, month: 12, day: 25};
  txType: string;
  txComment: string = '';
  txAmount: string = '';
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
  public txnTypes: string[] = [
    "BC",
    "AWAL",
    "ITFR",
    "INET",
    "PPAL",
    "CARD",
    "QRMP",
    "DDBT",
    "INT",
    "TFR",
    "ZOOM"
  ];
  
  constructor(private accountService: AccountService,
    private cd: ChangeDetectorRef,
    private datePipe: DatePipe,
    private modalService: NgbModal,
    private deviceService: DeviceDetectorService)
  {
    
    this.envName = environment.envName;
    this.uiversion = environment.uiversion;
    // Default values for the add transaction form
    
    // This is only necessary because the ngModel attribute breaks the selected behaviour of the option tag
    this.txType = 'BC';
    this.resetDatepicker();
    this.desktopDisplay = this.deviceService.isDesktop();
  }

   resetDatepicker()
   {
      const d: Date = new Date();  
      this.txDate = {year: d.getFullYear(), month: d.getMonth() + 1, day: d.getDate()}; 
   }

  // Call this to display the modal. 'content' is the name of the 'template' containing the elements to be displayed in the modal, I think
   open(content: any, txn: TransactionItem) {
      this.origupdTxn = txn;
      this.updateTxn = new TransactionItem();
      this.updateTxn.copy(this.origupdTxn);
      console.log("open: txn:" + JSON.stringify(this.updateTxn, null, 2));
    // Date picker field is somehow tied to content of this.txDate which is some sort of date object
    // so must the txn date into the date object... Date seems able to handle the date string in TransactionItem 
    let d : Date = new Date(this.updateTxn.date); // ISO date format, ie. YYYY-MM-DD
    console.log("open: txn orig date:" + d);
    this.txDate = {day: d.getDate(), month: d.getMonth()+1, year: d.getFullYear()}; 
    
    this.modalReference = this.modalService.open(content);
    this.modalReference.result.then((result) => {
      console.log("open:modalReference:result");
      // One maybe the updated transaction will come from the modal
      this.updmodalCloseAction(`${result}`, this.updateTxn);
    }, (reason) => {
      console.log("open:modalReference:reason");
      this.updmodalCloseAction( `${this.getDismissReason(reason)}`, this.updateTxn);
    });
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
         let updDate : Date = new Date(this.txDate.year, this.txDate.month-1, this.txDate.day);
         updtxn.date = this.datePipe.transform(updDate, 'yyyy-MM-dd') ?? '';         

         console.log("updmodalCloseAction: updating transaction:  " + JSON.stringify(updtxn, null, 2));
         this.updatetransaction(updtxn);
      }
      this.resetDatepicker();
   }


   ngOnInit() {
      console.log('AppComponent.ngOnInit: Starting');

      // Change syntax sugar to avoid deprecated warning 
      this.accountService.getVersion().subscribe({
         next:(res) => {
              this.version = res;
              // debugger;
              if(!this.version)
              {
                console.log('AppComponent.ngOnInit: Version is not initialized');
              }
              else
              {
               this.title = this.version.name + " (" + this.version.db +")";
               this.versiontxt = " v" + this.version.version + this.uiversion;
               console.log("AppComponent.ngOnInit: Version: " + this.title + " " + this.versiontxt);
              }
            },
         error: (err)=>{
            console.log("AppComponent.ngOnInit: An error occured during getVersion subscribe: " + JSON.stringify(err, null, 2));
            } ,
         complete: ()=>{console.log("AppComponent.ngOnInit: getVersion loading completed");}
      });


      this.accountService.getAccounts().subscribe({
         next: (res) => {
              this.accounts = res;
              // debugger;
              if(!this.accounts)
              {
                console.log('AppComponent.ngOnInit: accounts is not initialized');
              }
              else
              {
                console.log("AppComponent.ngOnInit: Accounts contains " + this.accounts.length + " items.");
              }
            },
         error: (err)=>{
            console.log("AppComponent.ngOnInit: An error occured during getAccounts subscribe: " + JSON.stringify(err, null, 2));
            } ,
         complete: ()=>{console.log("AppComponent.ngOnInit: getAccounts loading completed");}
      });


   console.log("AppComponent.ngOnInit:Finished");
}

getTransactions(acc : AccountItem)
{
  console.log("AppComponent.getTransactions: Starting");
  this.accountService.getTransactions(acc).subscribe({
      next: (res)=>{
            this.transactions = res;
            //debugger;
            if(!this.transactions)
            {
              console.log("AppComponent.getTransactions: variable is not initialized");
            }
            else
            {
              console.log("AppComponent.getTransactions: transactions contains " + this.transactions.length + " items.");
              let t : TransactionItem=this.transactions[this.transactions.length-1]
              console.log("AppComponent.getTransactions: last transaction " + t.comment + ", " +t.amount);
              // Fingers crossed this causes an update of the displayed transaction list, which
              // does not happen automatically when a new transaction is added
              this.cd.markForCheck();
            }
          },
      error: (err)=>{
          console.log("AppComponent.getTransactions: An error occured during getTransactions subscribe" + err);
          } ,
      complete: ()=>{console.log("AppComponent.getTransactions: getTransactions loading completed");}
});

  console.log("AppComponent.getTransactions:Finished");
  this.activeaccount = acc;
}

addTransactionToDB(txn : TransactionItem)
{
  console.log("AppComponent.addTransactionToDB: Starting");
  this.accountService.addTransaction(txn).subscribe( {
      next: (res)=>{
          console.log("AppComponent.addTransactionToDB: Response: " + res);
          // Must wait for add to complete before loading new transaction list
          this.getTransactions(this.activeaccount);
          // Reset amount to prevent double entry
          this.txAmount = '';
          this.txPastearea = '';
          },
      error: (err)=>{
          console.log("AppComponent.addTransactionToDB: An error occured during getTransactions subscribe:" + JSON.stringify(err));
          } ,
      complete: ()=>{console.log("AppComponent.addTransactionToDB: getTransactions loading completed");}
   });

  console.log("AppComponent.addTransactionToDB:Finished");
}

addtransaction()
{
  if(this.activeaccount == null)
  {
    console.log("Account is not set, unable to add transaction.");
    return;
  }

  let newent : TransactionItem = new TransactionItem();
  let d = new Date(this.txDate.year, this.txDate.month-1, this.txDate.day);
  newent.accid = this.activeaccount.id;
  newent.amount = this.txAmount;
  newent.comment = this.txComment;
  
  // With new Typescript cannot just assign return value to a string!
  // Using ternary operator is too clumsy for dealing with the return from a function
  // Apparently the '??' means use the result unless it's undefined or null and then use the value after the ??
  newent.date = this.datePipe.transform(d, 'yyyy-MM-dd') ?? '';
  newent.type = this.txType;

  console.log("Date: " + newent.date);
  console.log("Type: " + newent.type);
  console.log("Comment: " + newent.comment);
  console.log("Amount: " + newent.amount);  
  this.addTransactionToDB(newent); 
}

updTransactionToDB(txn : TransactionItem)
{
  console.log("AppComponent.updTransactionToDB: Starting");
  this.accountService.updateTransaction(txn).subscribe( {
      next: (res)=>{
          console.log("AppComponent.updTransactionToDB: Response: " + res);
          // Must wait for add to complete before loading new transaction list
          this.getTransactions(this.activeaccount);
          // Reset amount to prevent double entry
          this.txAmount = '';
          this.txPastearea = '';
          },
      error: (err)=>{
          console.log("AppComponent.updTransactionToDB: An error occured during getTransactions subscribe:" + JSON.stringify(err));
          } ,
      complete: ()=>{console.log("AppComponent.updTransactionToDB: getTransactions loading completed");}
   });

  console.log("AppComponent.updTransactionToDB:Finished");
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
   let oldDatestr: string = this.datePipe.transform(oldDate, 'dd/MM/yyyy') ?? '';

   // Could verify that something was changed - will need to keep track of the original TransactionItem
   if((updtxn.amount === this.origupdTxn.amount)
      && (updtxn.comment === this.origupdTxn.comment)
      && (updtxn.type === this.origupdTxn.type)
      && (updtxn.date === oldDatestr)
   )
   {
      console.log("No values were changed, nothing to update.");
      return;
   }

   // Should probably also verify that 'valid' values are present but this is not done for add...

   console.log("Date:    new:" + updtxn.date + " old:" + oldDatestr);
   console.log("Type:    new:" + updtxn.type + " old:" + this.origupdTxn.type);
   console.log("Comment: new:" + updtxn.comment + " old:" + this.origupdTxn.comment);
   console.log("Amount:  new:" + updtxn.amount) + " old:" + this.origupdTxn.amount;   
   this.updTransactionToDB(updtxn);

   // NB updTransactionToDB refreshes the transaction list when the response is received.
   // Horribly ugly code, I guess there must be a better way of doing it but alas I
   // don't know what it is...
}

parseEPC(epc : string) : TransactionItem
{
  console.log('parseEPC: entry');
  const trans : TransactionItem = new TransactionItem();

  // Docs say replaceAll should exist but it fails to compile. VSCode says replace should replace
  // all but it doesn't. I guess something needs to be updated but not sure what!! Maybe
  // the wierd failure of the SCT check is also something to do with versions....
  epc = epc.replaceAll('\r', ''); //replace(/\r/g, '');
  const lines: string[] = epc.split("\n");

  // Inexplicably (lines[3] === 'SCT') is giving 'false' when lines[3] is 'SCT'
  if((lines[0] === 'BCD') && (lines[3] === 'SCT'))
  {
    // Name Comment (Account)
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

// So once again ease and convenience of use is well and truely shafted by 'security' which makes
// reading the clipboard from the browser effectively impossible, especially for the primary target
// browser, ie. iphone safari. In the absence of any other solution for now I will have to manually
// paste from the clipboard into a textarea and then read the pasted data. 
onPaste(event: ClipboardEvent) {
  console.log("onPaste: entry");
  const clipboardData = event.clipboardData; // || window.clipboardData;
  if(clipboardData !== null)
  {
    const epc = clipboardData.getData('text');
    const txn : TransactionItem = this.parseEPC(epc);
    this.txAmount = txn.amount;
    this.txComment = txn.comment;
    this.txType = txn.type;
  }
  console.log("onPaste: exit");
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
  const txn : TransactionItem = this.parseEPC(decodedText);
  console.log('onScanSuccess: setting transaction details');
  this.txAmount = txn.amount;
  this.txComment = txn.comment;
  this.txType = txn.type;
  console.log('onScanSuccess: exit');
}

onScanFailure(error: any) {
  // handle scan failure, usually better to ignore and keep scanning.
  // for example:
  //console.warn(`Code scan error = ${error}`);
}

doScan() {
  this.html5QrcodeScanner = new Html5QrcodeScanner(
    "reader",
    { fps: 10, qrbox: {width: 250, height: 250}, supportedScanTypes: [], rememberLastUsedCamera: true },
    /* verbose= */ false);

  // Could not get it to work. It appeared to execute the first console.log of onScanSuccess and then vanish
  // literally without trace. Luckily I remembered something about 'this' getting lost especially in callbacks.
  // Eventually tracked the 'bind' thing down and whoopee it works now! It's a bit tricky to use with a PC camera
  // and an image on the iPhone but seems to work OK on the iPhone apart from always needing to ask permission.
  let callback = (this.onScanSuccess).bind(this);
  this.html5QrcodeScanner.render(callback,  this.onScanFailure);
}

isTransactions() : boolean {
  return this.transactions.length > 0;
}

clearComment() {
  this.txComment = "";
}

}

