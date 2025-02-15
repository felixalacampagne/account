// account.ui/src/app/app.component.ts
import { Component, OnDestroy, OnInit, OutputRefSubscription } from '@angular/core';

import {AccountService} from '../shared/service/account.service';
import {AccountItem} from '../shared/model/accountitem.model';

import { Version } from 'src/shared/model/version.model';


@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css', '../sass/account-styles.scss'],
    standalone: false
})

export class AppComponent implements  OnDestroy, OnInit {
  title: string = 'Account';
  versiontxt: string = '';
  version : Version | undefined;
  accounts: AccountItem[] = [];
  uiversion: string ='';

  accountRefresh?: OutputRefSubscription;
  constructor(private accountService: AccountService)
  {

  }
  
   ngOnInit() 
   {
      console.log('AppComponent.ngOnInit: Starting');
      
      // Change syntax sugar to avoid deprecated warning 
      this.accountService.getVersion().subscribe({
         next:(res) => {
              
              if(!res)
              {
                console.log('AppComponent.ngOnInit: Version is not initialized');
              }
              else
              {
               this.version = res;
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
              
              // debugger;
              if(!res)
              {
                console.log('AppComponent.ngOnInit: accounts is not initialized');
              }
              else
              {
               this.accounts = res;
                console.log("AppComponent.ngOnInit: Accounts contains " + this.accounts.length + " items.");
              }
            },
         error: (err)=>{
            console.log("AppComponent.ngOnInit: An error occured during getAccounts subscribe: " + JSON.stringify(err, null, 2));
            } ,
         complete: ()=>{console.log("AppComponent.ngOnInit: getAccounts loading completed");}
      });

      this.accountRefresh = this.accountService.listenAccountModified(id => {
         console.log("ngOnInit:accountRefresh: account id:" + id + " updated: refreshing account list");
         this.loadAccounts(); 
      })

      console.log("AppComponent.ngOnInit:Finished");
   }
   
   ngOnDestroy() {
      if (this.accountRefresh) {
         this.accountRefresh.unsubscribe()
       }
   }

   loadAccounts() 
   {
      this.accountService.getAccounts().subscribe({
         next: (res) => {
              if(!res)
              {
                console.log('loadAccounts: accounts is not initialized');
              }
              else
              {
               this.accounts = res;
                console.log("loadAccounts: Accounts contains " + this.accounts.length + " items.");
              }
            },
         error: (err)=>{
            console.log("loadAccounts: An error occured during getAccounts subscribe: " + JSON.stringify(err, null, 2));
            } ,
         complete: ()=>{console.log("loadAccounts: getAccounts loading completed");}
      });   
   }   
}

