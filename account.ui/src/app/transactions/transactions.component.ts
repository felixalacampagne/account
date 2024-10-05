// app/transactions/transactions.component.ts
import { Component, OnInit, ChangeDetectorRef, ViewChild, Input, SimpleChanges } from '@angular/core';
import { FormsModule, NgForm, NgModel } from '@angular/forms';
import { CommonModule, DatePipe } from '@angular/common';
import { NgbModal, ModalDismissReasons, NgbModalRef, NgbModule} from '@ng-bootstrap/ng-bootstrap';
import { NgbDatepickerConfig, NgbDateParserFormatter, NgbDateStruct } from '@ng-bootstrap/ng-bootstrap';
import { DeviceDetectorService } from 'ngx-device-detector';

import {environment} from '../../environments/environment';

import { isoNgbDateParserFormatter } from '../../shared/datepickformatter';
import {AccountService} from '../../shared/service/account.service';
import {AccountItem} from '../../shared/model/accountitem.model';
import {TransactionItem} from '../../shared/model/transaction.model';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { Html5QrcodeResult } from 'html5-qrcode/esm/core';
import { ActivatedRoute } from '@angular/router';

// WARNING: 'standalone: true' means the component must not be put in app.module and all imports must be duplicated
// in the imports sections of @Component otherwise many inexplicable errors will occur, eg.
// NG8002: Can't bind to 'ngModel' since it isn't a known property of 'select'
@Component({
  selector: 'transactions',
  standalone: true,
  imports: [FormsModule, CommonModule, NgbModule],
  templateUrl: './transactions.component.html',
  styleUrls: ['../../sass/account-styles.scss', '../app.component.css', './transactions.component.css'],
  providers: [{provide: NgbDateParserFormatter, useClass: isoNgbDateParserFormatter}]
})


export class TransactionsComponent implements OnInit {

   // accid is set via the route URL which is detected via ngOnChanges. The id is used to load the accountitem from
   // the server - cannot rely on the list previously loaded by the main app is it is cleared by the refresh button
   // and there is no way to know when it has been reloaded.
   // The loadaccount method has to trigger the loadtransactions from within its subscribe lmbda as that is
   // is the only way to know when the accountitem has been loaded. Its mind blowingly clumsy but at least
   // it appears to work including when the refresh button is used.
   @Input() accid!: number;
   
   activeaccount!: AccountItem; 

   @ViewChild('closebutton') closebutton: any;   
   modalReference: NgbModalRef | undefined;


   transactions: TransactionItem[] = [];
  
   public submitted: boolean = false;
   public defaultdate: string = '';
   envName: string = '';

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
      private route: ActivatedRoute,
      private deviceService: DeviceDetectorService)
   {
      this.envName = environment.envName;

      // This is only necessary because the ngModel attribute breaks the selected behaviour of the option tag
      this.txType = 'BC';
      this.resetDatepicker();
      this.desktopDisplay = this.deviceService.isDesktop();
   }


   ngOnInit() 
   {
      // console.log('TransactionsComponent.ngOnInit: start');
      //this.route.queryParams.subscribe(params => {
      //   // console.log("TransactionsComponent.ngOnInit: params:" + JSON.stringify(params, null, 2));
      //   let account : AccountItem = JSON.parse(params["account"]);
      //   // console.log("TransactionsComponent.ngOnInit: account from json:" + JSON.stringify(account, null, 2));
      //   this.getTransactions(account);
      //});
      // console.log("TransactionsComponent.ngOnInit: finish");
   }

   resetDatepicker()
   {
      const d: Date = new Date();  
      this.txDate = {year: d.getFullYear(), month: d.getMonth() + 1, day: d.getDate()}; 
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



   ngOnChanges(changes: SimpleChanges ) 
   {
      console.log("TransactionsComponent.ngOnChanges: enter: " + JSON.stringify(changes, null, 2));
      for (const propName in changes) 
      {
         console.log("TransactionsComponent.ngOnChanges: propName:" + propName);
         const chng = changes[propName];
         if(propName === 'accid')
         {
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
            }
          },
         error: (err)=>{
             console.log("TransactionsComponent.loadAccount: An error occured during subscribe: " + JSON.stringify(err, null, 2));
             } ,
         complete: ()=>{console.log("TransactionsComponent.loadAccount: completed");}
      });
   
     console.log("TransactionsComponent.loadAccount:Finished");
   }
   
loadTransactions(acc : AccountItem)
{
   console.log("TransactionsComponent.loadTransactions: Starting: " + + JSON.stringify(acc, null, 2));
   if(acc.id < 0)
      return;
      
   this.accountService.getTransactions(acc).subscribe({
      next: (res)=>{
            if(!res)
            {
              console.log("TransactionsComponent.loadTransactions: variable is not initialized");
            }
            else
            {
              this.activeaccount = acc;
              this.transactions = res;
              console.log("TransactionsComponent.loadTransactions: transactions contains " + this.transactions.length + " items.");
            }
          },
      error: (err)=>{
          console.log("TransactionsComponent.loadTransactions: An error occured during loadTransactions subscribe" + err);
          } ,
      complete: ()=>{console.log("TransactionsComponent.loadTransactions: loadTransactions loading completed");}
   });

   console.log("TransactionsComponent.loadTransactions:Finished");
}

addTransactionToDB(txn : TransactionItem)
{
  console.log("AppComponent.addTransactionToDB: Starting");
  this.accountService.addTransaction(txn).subscribe( {
      next: (res)=>{
          console.log("AppComponent.addTransactionToDB: Response: " + res);
          // Must wait for add to complete before loading new transaction list
          this.loadTransactions(this.activeaccount);
          // Reset amount to prevent double entry
          this.txAmount = '';
          this.txPastearea = '';
          },
      error: (err)=>{
          console.log("AppComponent.addTransactionToDB: An error occured during addTransactionToDB subscribe:" + JSON.stringify(err));
          } ,
      complete: ()=>{console.log("AppComponent.addTransactionToDB: completed");}
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
          this.loadTransactions(this.activeaccount);
          // Reset amount to prevent double entry
          this.txAmount = '';
          this.txPastearea = '';
          },
      error: (err)=>{
          console.log("AppComponent.updTransactionToDB: An error occured during updTransactionToDB subscribe:" + JSON.stringify(err));
          } ,
      complete: ()=>{console.log("AppComponent.updTransactionToDB: completed");}
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
