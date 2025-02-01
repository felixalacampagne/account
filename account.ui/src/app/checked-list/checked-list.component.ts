// account.ui/src/app/checked-list/checked-list.component.ts
import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { TransactionItem } from 'src/shared/model/transaction.model';
import { AccountService } from '../../shared/service/account.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AccountItem } from 'src/shared/model/accountitem.model';
import { CommonModule } from '@angular/common';
import {MatIconModule} from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router'; // for 'routerlink is not a property of button'
import { DateformatService } from 'src/shared/service/dateformat.service';

// import { DataSource } from '@angular/cdk/collections';
// import { BehaviorSubject, Observable } from 'rxjs';

@Component({
    selector: 'checked-list',
    imports: [MatTableModule, MatCardModule, MatIconModule, MatButtonModule, CommonModule, RouterModule],
    templateUrl: './checked-list.component.html',
    styleUrls: ['../app.component.css', './checked-list.component.css']
})
export class CheckedListComponent implements OnInit {
   @Input() accid!: number;  
   activeaccount!: AccountItem; 
   
   transactions: TransactionItem[] = [];

   columnsToDisplay = ['date', 'memo', 'ref', 'amount', 'chkbal'];
   // Appears a simple array should be sufficient for datasource.. https://material.angular.io/components/table/overview
   // // 64Gazillion dollar question: when the transaction list is loaded and assigned to 'transactions'
   // // does a new datasource need to be created?
   // dataSource = new TransactionDataSource(this.transactions);
   desktopDisplay: boolean = false;
   pageNumber: number = 0;

   constructor(private accountService: AccountService,
               private datfmt : DateformatService,
               private modalService: NgbModal)
   {
    
   }

   ngOnInit() 
   {
     // console.log('TransactionsComponent.ngOnInit: start');
      // console.log("TransactionsComponent.ngOnInit: finish");
   }


   // Based on transactions.component: TODO find a way to share it (NB transaction are checked transactions
   ngOnChanges(changes: SimpleChanges ) 
   {
      console.log("CheckedListComponent.ngOnChanges: enter: " + JSON.stringify(changes, null, 2));
      for (const propName in changes) 
      {
         console.log("CheckedListComponent.ngOnChanges: propName:" + propName);
         const chng = changes[propName];
         if(propName === 'accid')
         {
            this.loadAccount(chng.currentValue);
         }
      }
   }
   
   formatDateColumn(jsondate: string) : string
   {
      return this.datfmt.listFormat(jsondate) ;   
   }
   
   loadAccount(id : number)
   {
     console.log("CheckedListComponent.loadAccount: Starting: id " + id);
         
     this.accountService.getAccount(id).subscribe({
         next: (res)=>{
            if(!res)
            {
              console.log("CheckedListComponent.loadAccount: variable is not initialized");
            }
            else
            {
               this.loadTransactions(res);
            }
          },
         error: (err)=>{
             console.log("CheckedListComponent.loadAccount: An error occured during subscribe: " + JSON.stringify(err, null, 2));
             } ,
         complete: ()=>{console.log("CheckedListComponent.loadAccount: completed");}
      });
   
     console.log("CheckedListComponent.loadAccount:Finished");
   }
   
   loadTransactions(acc : AccountItem, page: number = 0)
   {
      console.log("CheckedListComponent.loadTransactions: Starting: " + JSON.stringify(acc, null, 2));
      if(acc.id < 0)
         return;
         
      this.accountService.getCheckedTransactions(acc, page).subscribe({
         next: (res)=>{
            if(!res)
            {
               console.log("CheckedListComponent.loadTransactions: variable is not initialized");
            }
            else
            {
               this.activeaccount = acc;
               this.transactions = res;
               this.pageNumber = page;
               console.log("CheckedListComponent.loadTransactions: transactions contains " + this.transactions.length + " items.");
            }
         },
         error: (err)=> {
            console.log("CheckedListComponent.loadTransactions: An error occured during loadTransactions subscribe" + err);
         },
         complete: ()=> {
            console.log("CheckedListComponent.loadTransactions: loadTransactions loading completed");
         }
      });

      console.log("CheckedListComponent.loadTransactions:Finished");
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

   isTransactions() : boolean 
   {
      return this.transactions.length > 0;
   }
    
}


// // Based on example at 
// export class TransactionDataSource extends DataSource<TransactionItem> {
// /** Stream of data that is provided to the table. */
// data = new BehaviorSubject<TransactionItem[]>([]);

// constructor(transactionList: TransactionItem[] )
// {
//   super();
//   this.data = new BehaviorSubject<TransactionItem[]>(transactionList);
// }

//   /** Connect function called by the table to retrieve one stream containing the data to render. */
//   connect(): Observable<TransactionItem[]> {
//     return this.data;
//   }

//   disconnect() {}
// }
