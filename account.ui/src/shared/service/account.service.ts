import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import { AccountItem } from '../model/accountitem.model';
import { TransactionItem } from '../model/transaction.model';

@Injectable()
export class AccountService 
{
    private serverhost : string; // = "http://minnie"; //""; //"http://minnie"; //"http://localhost:8080";
    private accountapiapp : string = "/account/";
    private apiurl : string;
    private listaccsvc : string = "listaccount";
    private listtxnsvc : string = "listtransaction/";
    private addtxnsvc : string = "addtransaction";
    constructor(private http : HttpClient)
    {
        this.serverhost = environment.accountapi_host;
        this.accountapiapp = environment.accountapi_app;
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

    getTransactions(a : AccountItem) : Observable<TransactionItem[]>
    {
        let url : string;
        url = this.apiurl + this.listtxnsvc + a.id;
        // The items are returned wrapped in an array named transactions
        return this.http.get(url).pipe( map((res:any) => res.transactions));

    }

    addTransaction(txn : TransactionItem) // : Observable<Response>
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
        return this.http.post(url, json, { responseType: 'text' });
    }
}