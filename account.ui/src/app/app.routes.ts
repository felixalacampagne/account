// app.routes.ts
import {Routes} from '@angular/router';
import { StandingordersComponent } from './standingorders/standingorders.component';
import { TransactionsComponent } from './transactions/transactions.component';

export const routes = [
   {path: 'transactions/:accid', title: "Transactions", component: TransactionsComponent},
   {path: 'standingorders', title: "Standing Orders", component: StandingordersComponent}
];
