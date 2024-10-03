// account.service
import {Injectable} from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import { AccountItem } from '../model/accountitem.model';
import { TransactionItem } from '../model/transaction.model';
import { Version } from '../model/version.model';
import { StandingOrderItem } from '../model/standingorderitem.model';

@Injectable()
export class AccountService 
{
   private serverhost : string; // = "http://minnie"; //""; //"http://minnie"; //"http://localhost:8080";
   private accountapiapp : string = ""; // /account/";
   private apiurl : string;
   private listaccsvc : string = "listaccount";
   private listtxnsvc : string = "listtransaction/";
   private addtxnsvc : string = "addtransaction";
   private updtxnsvc : string = "updatetransaction";
   private versionsvc : string = "version";
   private listsosvc : string = "liststandingorders";
   
   private accounts : AccountItem[]=[];
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


   // For the life of me I cannot figure out how to wait for the http request to finish
   // so I can store the list of accounts for lookup by id when loading the transaction list
   // via the routing. So now when the account list is built by the master app.component
   // it will set the list in the service.
   setAccountList(accounts : AccountItem[])
   {
      console.log("AccountService.setAccountList: account list: " + accounts);
      this.accounts = accounts;
   }
   
   getAccountList() : AccountItem[]
   {
       return this.accounts; 
   }


    getAccounts() : Observable<AccountItem[]>
    {
        let url : string;
        url = this.apiurl + this.listaccsvc;
        // The account items are returned wrapped in an array named accounts
        console.log("getAccount API URL: " + url);
        return this.http.get(url).pipe( map((res:any) => res.accounts) );    
    }

    getTransactions(a : AccountItem) : Observable<TransactionItem[]>
    {
        let url : string;
        url = this.apiurl + this.listtxnsvc + a.id;
        // The items are returned wrapped in an array named transactions
        return this.http.get(url).pipe( map((res:any) => res.transactions));

    }

    addTransaction(txn : TransactionItem): Observable<string>
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

}
