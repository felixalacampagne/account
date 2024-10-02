// app.routes.ts
import {Routes} from '@angular/router';
import { StandingordersComponent } from './standingorders/standingorders.component';
import { TransactionsComponent } from './transactions/transactions.component';

export const routes = [
   {path: 'transactions', title: "Transactions", component: TransactionsComponent},
   {path: 'standingorders', title: "Standing Orders", component: StandingordersComponent},
   {path: '**', redirectTo: '/'}
];

// Refresh page doesn't work which means I'll have to waste even more time getting
// something which is blindingly obvious to work again... wtf is it with these forking
// people who create this shirt.
// A clue seems to be
// onsameurlnavigation : relaod
// but the default is to do mothing (WTF -  why the fork do they think the user requested a forking refresh???)
// and there is fork all indication how or where to change the value from the default...
