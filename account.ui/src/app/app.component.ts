// account.ui/src/app/app.component.ts
import { Component, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';

import {AccountService} from '../shared/service/account.service';
import {AccountItem} from '../shared/model/accountitem.model';

import { Version } from 'src/shared/model/version.model';
import { Params } from '@angular/router';



// <transactions [activeaccount]="currentAccount"></transactions>
// <standingorders></standingorders>

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css', '../sass/account-styles.scss']
})

export class AppComponent {
  title: string = 'Account';
  versiontxt: string = '';
  version : Version | undefined;
  accounts: AccountItem[] = [];
  uiversion: string ='';
  
  currentAccount: AccountItem = new AccountItem();
  
  constructor(private accountService: AccountService)
  {

  }
  
   ngOnInit() 
   {
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
                this.accountService.setAccountList(this.accounts);
              }
            },
         error: (err)=>{
            console.log("AppComponent.ngOnInit: An error occured during getAccounts subscribe: " + JSON.stringify(err, null, 2));
            } ,
         complete: ()=>{console.log("AppComponent.ngOnInit: getAccounts loading completed");}
      });
   
   
      console.log("AppComponent.ngOnInit:Finished");
   }

   loadTransactions(acc : AccountItem)
   {
      console.log("AppComponent.loadTransactions:account.id:" + acc.id);
      this.currentAccount = acc;
   }
   
   getqp(acc:AccountItem) : Params
   {
      let p : Params = { "account": JSON.stringify(acc, null, 0)};
      
      return p;
   }
}

