// shared/service/account.service.ts
import {Injectable, output, OutputEmitterRef, OutputRefSubscription} from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {BehaviorSubject, Observable, Subscription} from "rxjs";
import {map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import { AccountItem } from '../model/accountitem.model';
import { AccountDetail } from '../model/accountdetail.model';
import { AddTransactionItem, TransactionItem, Transactions } from '../model/transaction.model';
import { Version } from '../model/version.model';
import { StandingOrderItem } from '../model/standingorderitem.model';
import { TfrAccountItem } from '../model/tfraccountitem.model';
import { EPCtransaction } from '../model/epctransaction.model';
import { TransferAccountItem } from '../model/transferaccountitem.model';

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
   private delsosvc : string = "delstandingorder";
   private getqrcodepayer : string = 'qrcodepayer';
   private listaccinf : string = "listaccinf";
   private addaccount: string = "addaccount";
   private updaccount: string = "updaccount";
   private delaccount: string = "delaccount";
   private listtfraccs : string = "listtransferaccounts";
   private addtfracc  : string = "addtransferaccount";
   private updtfracc : string = "updatetransferaccount";
   private deltfracc   : string = "deletetransferaccount";
   private accinf : string = "accinf/";

   private apiext: string = "";

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

   accountChanged : BehaviorSubject<number> = new BehaviorSubject(-1);

   constructor(private http : HttpClient)
   {
      // If host value is not given by environment then should assume api
      // is on same server as frontend. Frontend server can be obtained from
      // window.location.hostname, window.location.pathname
      // WARNING! need to allow for the port, not sure if it is included in the hostname value.
      // window.location
      //    .host     gives server and port (in theory)
      //    .origin   gives the protocol, hostname and port number of a URL
      if(environment.accountapi_host)
      {
         this.serverhost = environment.accountapi_host;
      }
      else
      {
         this.serverhost = window.location.origin;
      }

      if( environment.accountapi_ext)
      {
         this.apiext = environment.accountapi_ext;
      }
      this.accountapiapp = environment.folder + environment.accountapi_app;
      this.apiurl = this.serverhost + this.accountapiapp
      console.log("Account API server host: " + this.apiurl);
   }

   // This needs to be called after add/upd/delAccount is completed
   notifyAccountModified(id : number) : void
   {
      this.accountChanged.next(id);
   }

   listenAccountModified(callback: (value: number) => void) : OutputRefSubscription
   {
      return this.accountChanged.subscribe(callback);
   }

   getAccounts() : Observable<AccountItem[]>
   {
     let url : string;
     url = this.makeApiname(this.listaccsvc);
     // The account items are returned wrapped in an array named accounts
     console.log("getAccount API URL: " + url);
     return this.http.get(url).pipe( map((res:any) => res.accounts) );
   }

   getAccount(id : number) : Observable<AccountItem>
   {
     let url : string;
     url = this.makeApiname(this.account + id);
     console.log("getAccount API URL: " + url);
     return this.http.get(url).pipe( map((res:any) => res) );
   }

   getAccountDetails(): Observable<AccountDetail[]>
   {
      let url : string;
      url = this.makeApiname(this.listaccinf);
      // The account items are returned wrapped in an array named accounts
      console.log("getAccountDetails API URL: " + url);
      return this.http.get(url).pipe( map((res:any) => res) );
   }

   addAccount(item : AccountDetail) : Observable<string>
   {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(item);
      url = this.apiurl + this.addaccount ;
      console.log("addAccount: POSTing to " + url + ": " + json);

      // Spring gives exception saying text/plain not supported so need to set content type to JSON
      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");

      return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   updateAccount(item : AccountDetail) : Observable<string>
   {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(item);
      url = this.apiurl + this.updaccount ;
      console.log("updateAccount: POSTing to " + url + ": " + json);

      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");

      return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   deleteAccount(item : AccountDetail) : Observable<string>
   {
       let json : string;
       let url : string;
       let res;
       json = JSON.stringify(item);
       url = this.apiurl + this.delaccount;
       console.log("deleteAccount: POSTing to " + url + ": " + json);

       var headers = new HttpHeaders();
       headers = headers.set('Content-Type', 'application/json');
       headers = headers.set("Accept", "text/plain");

       return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   getAccountDetail(id : number) : Observable<AccountDetail>
   {
      let url : string;
      url = this.makeApiname(this.account + id);
      console.log("getAccountDetail API URL: " + url);
      return this.http.get(url).pipe( map((res:any) => res) );
   }

   updateAccountStatementRef(id: number, statementref: string) : void
   {
      this.postpdateAccountStatementRef(id,statementref).subscribe( {
      next: (res)=>{
         console.log("updateAccountStatementRef: Response: " + res);
         },
      error: (err)=>{
         console.log("updateAccountStatementRef: An error occured:" + JSON.stringify(err));
         } ,
      complete: ()=>{console.log("updateAccountStatementRef: completed");}
      });
   }

   postpdateAccountStatementRef(id: number, statementref: string) : Observable<string>
   {
      let url : string;
      url = this.apiurl + this.updaccstref;

      let acc : AccountItem = new AccountItem(id, '');
      acc.statementref = statementref;

      let json = JSON.stringify(acc);
      console.log("updateAccountStatementRef: POSTing to " + url + ": " + json);

      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");

      return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   calcChecked(a : AccountItem) : Observable<TransactionItem>
   {
      let url : string;
      url = this.makeApiname(this.calcchkbal + a.id);
      console.log("calcChecked API URL: " + url);
      return this.http.get(url).pipe( map((res:any) => res) );
   }

   // TODO: could combine this with getTransactions
   getCheckedTransactions(a : AccountItem, p: number = 0) : Observable<TransactionItem[]>
   {
      let url : string;
      url = this.makeApiname(this.listchktxn + a.id + '/' + p);
      return this.http.get(url).pipe( map((res:any) => res.transactions));
   }

   getCheckedBalance(a : AccountItem) : Observable<TransactionItem>
   {
      let url : string;
      url = this.makeApiname(this.getchktxn + a.id);
      return this.http.get(url).pipe( map((res:any) => res));
   }

   getAccountsForTransfer(a : AccountItem) : Observable<TfrAccountItem[]>
   {
      let url : string;
      url = this.makeApiname(this.accsfortfr + a.id);
      return this.http.get(url).pipe( map((res:any) => res));
   }

   getTransactions(a : AccountItem, p: number = 0, r: number = 15) : Observable<Transactions>
   {
      let url : string;
      url = this.makeApiname(this.listtxnsvc + a.id + '/' + p) + "?rows=" + r;
      // The items are returned wrapped in an array named transactions
      return this.http.get(url).pipe( map((res:any) => res));
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
        console.log("updateTransaction: POSTing to " + url + ": " + json);

        var headers = new HttpHeaders();
        headers = headers.set('Content-Type', 'application/json');
        headers = headers.set("Accept", "text/plain");

        return this.http.post(url, json, {headers: headers,  responseType: 'text'});
    }

    deleteTransaction(txn : TransactionItem) : Observable<string>
    {
        let json : string;
        let url : string;
        let res;
        json = JSON.stringify(txn);
        url = this.apiurl + this.deltxnsvc;
        console.log("deleteTransaction: POSTing to " + url + ": " + json);

        var headers = new HttpHeaders();
        headers = headers.set('Content-Type', 'application/json');
        headers = headers.set("Accept", "text/plain");

        return this.http.post(url, json, {headers: headers,  responseType: 'text'});
    }

    getVersion() : Observable<Version>
    {
        let url : string;
        url = this.makeApiname(this.versionsvc);
        // A json object is returned containing the fields of Version - it is not returned as a named object in the response
        // thus 'res' is the Version object
        console.log("getVersion: API URL: " + url);
        return this.http.get<Version>(url).pipe( map((res:any) => res) );
    }

    getStandingOrders() : Observable<StandingOrderItem[]>
    {
        let url : string;
        url = this.makeApiname(this.listsosvc);
        // The account items are returned wrapped in an array named accounts
        console.log("getAccount API URL: " + url);
        return this.http.get(url).pipe( map((res:any) => res) );
    }

    getStandingOrder(id : number) : Observable<StandingOrderItem>
    {
        let url : string;
        url = this.makeApiname(this.standingorder + id);
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
      console.log("updateStandingOrder: POSTing to " + url + ": " + json);

      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");

      return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   deleteStandingOrder(item : StandingOrderItem) : Observable<string>
   {
       let json : string;
       let url : string;

       json = JSON.stringify(item);
       url = this.apiurl + this.delsosvc;
       console.log("deleteStandingOrder: POSTing to " + url + ": " + json);

       var headers = new HttpHeaders();
       headers = headers.set('Content-Type', 'application/json');
       headers = headers.set("Accept", "text/plain");

       return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   getQRCode(epc: EPCtransaction) : Observable<any>
   {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(epc);
      url = this.apiurl + this.getqrcodepayer;
      console.log("getQRCode: POSTing to " + url + ": " + json);

      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      return this.http.post(url, json, {headers: headers,  responseType: 'blob'});
   }

   getTransferAccounts() : Observable<TransferAccountItem[]>
   {
       let url : string;
       url = this.makeApiname(this.listtfraccs) ;
       console.log("getTransferAccounts API URL: " + url);
       return this.http.get(url).pipe( map((res:any) => res) );
   }

   addTransferAccount(item : TransferAccountItem) : Observable<string>
   {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(item);
      url = this.apiurl + this.addtfracc ;
      console.log("addTransferAccount: POSTing to " + url + ": " + json);

      // Spring gives exception saying text/plain not supported so need to set content type to JSON
      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");

      return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   updateTransferAccount(item : TransferAccountItem) : Observable<string>
   {
      let json : string;
      let url : string;
      let res;
      json = JSON.stringify(item);
      url = this.apiurl + this.updtfracc ;
      console.log("updateTransferAccount: POSTing to " + url + ": " + json);

      var headers = new HttpHeaders();
      headers = headers.set('Content-Type', 'application/json');
      headers = headers.set("Accept", "text/plain");

      return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   deleteTransferAccount(item : TransferAccountItem) : Observable<string>
   {
       let json : string;
       let url : string;
       let res;
       json = JSON.stringify(item);
       url = this.apiurl + this.deltfracc;
       console.log("deleteTransferAccount: POSTing to " + url + ": " + json);

       var headers = new HttpHeaders();
       headers = headers.set('Content-Type', 'application/json');
       headers = headers.set("Accept", "text/plain");

       return this.http.post(url, json, {headers: headers,  responseType: 'text'});
   }

   makeApiname(api : string) : string
   {
      // Since Angular 20 files in the assets directory without an extension are served
      // with garbage at the end of the file making them unparsable as JSON files.
      // One workaround is to handle every response as a pure text, remove the garbage
      // and then parse as JSON (used by sattimers). This is a very annoying thing to
      // need to do for all the api of account and defeats the purpose of having the
      // parsing handled automatically.
      //
      // It appears that giving the test data file and extension of .json prevents the
      // garbage from being appended to the respone and thus allows the automatic
      // to be performed. Obviously the actual apis should not be given an extension
      // but using the environment file allows the extension to be added only when
      // running in the backend less test environment. It still requires all api calls
      // to be updated but not as annoying as converting everything to text.
      return this.apiurl + api + this.apiext;
   }
}
