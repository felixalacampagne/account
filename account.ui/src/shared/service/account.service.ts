// account.service.ts
import {Injectable} from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import { AccountItem } from '../model/accountitem.model';
import { AccountDetail } from '../model/accountdetail.model';
import { AddTransactionItem, TransactionItem } from '../model/transaction.model';
import { Version } from '../model/version.model';
import { StandingOrderItem } from '../model/standingorderitem.model';
import { TfrAccountItem } from '../model/tfraccountitem.model';

@Injectable()
export class AccountService 
{

   private serverhost : string; // = "http://minnie"; //""; //"http://minnie"; //"http://localhost:8080";
   private accountapiapp : string = ""; // /account/";
   private apiurl : string;
   private listaccsvc : string = "listaccount";
   private account : string = "account/";
   private updaccstref : string = "updaccountstref";
   private listtxnsvc : string = "listtransaction/";
   private listchktxn : string = "listchecked/";
   private calcchkbal : string = "calcchecked/";
   private accsfortfr : string = "accsfortfr/";
   private getchktxn  : string = "getchecked/";
   private addtxnsvc : string = "addtransaction";
   private updtxnsvc : string = "updatetransaction";
   private deltxnsvc : string = "deletetransaction";
   private versionsvc : string = "version";
   private listsosvc : string = "liststandingorders";
   private standingorder : string = "standingorder/";
   private addsosvc : string = "addstandingorder";
   private updsosvc : string = "updatestandingorder";
   private listaccinf : string = "listaccinf";
   private accinf : string = "accinf/";
   
   public periodTypes = [
      {period: "D", desc: "Day"},
      {period: "W", desc: "Week"},
      {period: "M", desc: "Month"},
      {period: "Y", desc: "Year"}
     ];
     
   public txnTypes: string[] = [
   "BC",
   "AWAL",
   "ITFR",
   "INET",
   "PPAL",
   "CARD",
   "MED",
   "P2PM",
   "QRMP",
   "DDBT",
   "INT",
   "TFR",
   "ZOOM",
   "PAY",
   "SAVE",
   "GROC",
   "PRIC",
   "FEE",
   "BANK",
   "DOMI",
   "INV",
   "INSR",
   "CAR"
   ];
        
   constructor(private http : HttpClient)
   {
      // If host value is not given by environment then should assume api
      // is on same server as frontend. Frontend server can be obtained from
      // window.location.hostname, window.location.pathname
      // WARNING! need to allow for the port, not sure if it is included in the hostname value.
      // window.location
      //    .host     gives server and port (in theory)
      //    .origin   gives the protocol, hostname and port number of a URL
      if((typeof environment.accountapi_host !== 'undefined') && (environment.accountapi_host))
      {
         this.serverhost = environment.accountapi_host;
      }
      else
      {    
         this.serverhost = window.location.origin;
      }
      this.accountapiapp = environment.folder + environment.accountapi_app;
      this.apiurl = this.serverhost + this.accountapiapp
      console.log("Account API server host: " + this.apiurl);
   }    

   getAccounts() : Observable<AccountItem[]>
   {
     let url : string;
     url = this.apiurl + this.listaccsvc;
     // The account items are returned wrapped in an array named accounts
     console.log("getAccount API URL: " + url);
     return this.http.get(url).pipe( map((res:any) => res.accounts) );    
   }
   
   getAccount(id : number) : Observable<AccountItem>
   {
     let url : string;
     url = this.apiurl + this.account + id;
     console.log("getAccount API URL: " + url);
     return this.http.get(url).pipe( map((res:any) => res) );    
   }

   getAccountDetails(): Observable<AccountDetail[]>
   {
      let url : string;
      url = this.apiurl + this.listaccinf;
      // The account items are returned wrapped in an array named accounts
      console.log("getAccountDetails API URL: " + url);
      return this.http.get(url).pipe( map((res:any) => res.accounts) );    
   } 

   getAccountDetail(id : number) : Observable<AccountDetail>
   {
      let url : string;
      url = this.apiurl + this.account + id;
      console.log("getAccountDetail API URL: " + url);
      return this.http.get(url).pipe( map((res:any) => res) );    
   }    

   updateAccountStatementRef(id: number, statementref: string) : void
   {
      this.putUpdateAccountStatementRef(id,statementref).subscribe( {
      next: (res)=>{
         console.log("updateAccountStatementRef: Response: " + res);
         },
      error: (err)=>{
         console.log("updateAccountStatementRef: An error occured:" + JSON.stringify(err));
         } ,
      complete: ()=>{console.log("updateAccountStatementRef: completed");}
  });      
   }
   putUpdateAccountStatementRef(id: number, statementref: string) : Observable<string>
   {
      let url : string;
      url = this.apiurl + this.updaccstref;

      let acc : AccountItem = new AccountItem(id, '');
      acc.statementref = statementref;

      let json = JSON.stringify(acc);
      console.log("updateAccountStatementRef: PUTing to " + url + ": " + json);

      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");
      
      return this.http.put(url, json, {headers: headers,  responseType: 'text'});       
   }

   calcChecked(a : AccountItem) : Observable<TransactionItem>
   {
      let url : string;
      url = this.apiurl + this.calcchkbal + a.id;
      console.log("calcChecked API URL: " + url);
      return this.http.get(url).pipe( map((res:any) => res) );      
   }

   // TODO: could combine this with getTransactions
   getCheckedTransactions(a : AccountItem, p: number = 0) : Observable<TransactionItem[]>
   {
      let url : string;
      url = this.apiurl + this.listchktxn + a.id + '/' + p;
      return this.http.get(url).pipe( map((res:any) => res.transactions));      
   }

   getCheckedBalance(a : AccountItem) : Observable<TransactionItem>
   {
      let url : string;
      url = this.apiurl + this.getchktxn + a.id;
      return this.http.get(url).pipe( map((res:any) => res));
   }

   getAccountsForTransfer(a : AccountItem) : Observable<TfrAccountItem[]>
   {
      let url : string;
      url = this.apiurl + this.accsfortfr + a.id;
      return this.http.get(url).pipe( map((res:any) => res));
   }

   getTransactions(a : AccountItem, p: number = 0, r: number = 15) : Observable<TransactionItem[]>
   {
      let url : string;
      url = this.apiurl + this.listtxnsvc + a.id + '/' + p + "?rows=" + r;
      // The items are returned wrapped in an array named transactions
      return this.http.get(url).pipe( map((res:any) => res.transactions));
   }

   addTransaction(txn : AddTransactionItem): Observable<string>
   {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(txn);
      url = this.apiurl + this.addtxnsvc;
      console.log("addTransaction: POSTing to " + url + ": " + json);

      // To prevent response being parsed as JSON must use responseType: 'text'. 
      // When responseType: 'text' is used the return value must not be a generic
      // type, ie. Observable<Response>, otherwise a incomprehensible compiler error
      // is generated.
      //
      // Now for the first time the clearing of the transaction entry screen is
      // actually working!!!
        
      // Spring gives exception saying text/plain not supported so need to set content type to JSON
      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");
        
      return this.http.post(url, json, {headers: headers,  responseType: 'text'}); 
   }
    
    updateTransaction(txn : TransactionItem) : Observable<string>
    {
        let json : string;
        let url : string;
        let res;
        json = JSON.stringify(txn);
        url = this.apiurl + this.updtxnsvc;
        console.log("updateTransaction: PUTing to " + url + ": " + json);

        var headers = new HttpHeaders();
        headers = headers.set('Content-Type', 'application/json');
        headers = headers.set("Accept", "text/plain");
        
        return this.http.put(url, json, {headers: headers,  responseType: 'text'}); 
    }

    deleteTransaction(txn : TransactionItem) : Observable<string>
    {
        let json : string;
        let url : string;
        let res;
        json = JSON.stringify(txn);
        url = this.apiurl + this.deltxnsvc;
        console.log("deleteTransaction: PUTing to " + url + ": " + json);

        var headers = new HttpHeaders();
        headers = headers.set('Content-Type', 'application/json');
        headers = headers.set("Accept", "text/plain");
        
        return this.http.put(url, json, {headers: headers,  responseType: 'text'}); 
    }

    getVersion() : Observable<Version>
    {
        let url : string;
        url = this.apiurl + this.versionsvc;
        // A json object is returned containing the fields of Version - it is not returned as a named object in the response
        // thus 'res' is the Version object
        console.log("getVersion: API URL: " + url);
        return this.http.get(url).pipe( map((res:any) => res) );    
    }    

    getStandingOrders() : Observable<StandingOrderItem[]>
    {
        let url : string;
        url = this.apiurl + this.listsosvc;
        // The account items are returned wrapped in an array named accounts
        console.log("getAccount API URL: " + url);
        return this.http.get(url).pipe( map((res:any) => res) );    
    }

    getStandingOrder(id : number) : Observable<StandingOrderItem>
    {
        let url : string;
        url = this.apiurl + this.standingorder + id;
        // The account items are returned wrapped in an array named accounts
        console.log("getAccount API URL: " + url);
        return this.http.get(url).pipe( map((res:any) => res) );    
    }

    addStandingOrder(soi : StandingOrderItem) : Observable<string>
    {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(soi);
      url = this.apiurl + this.addsosvc;
      console.log("addStandingOrder: POSTing to " + url + ": " + json);

      // Spring gives exception saying text/plain not supported so need to set content type to JSON
      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");
        
      return this.http.post(url, json, {headers: headers,  responseType: 'text'});      
    }
    
    updateStandingOrder(soi : StandingOrderItem) : Observable<string>
    {
        let json : string;
        let url : string;
        let res;
        json = JSON.stringify(soi);
        url = this.apiurl + this.updsosvc;
        console.log("updateStandingOrder: PUTing to " + url + ": " + json);

        var headers = new HttpHeaders();
        headers = headers.set('Content-Type', 'application/json');
        headers = headers.set("Accept", "text/plain");
        
        return this.http.put(url, json, {headers: headers,  responseType: 'text'}); 
    }
}
