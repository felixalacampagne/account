// app.routes.ts
import {Routes} from '@angular/router';
import { StandingordersComponent } from './standingorders/standingorders.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { AccountsComponent } from './accounts/accounts.component';
import { TransferaccountsComponent } from './transferaccounts/transferaccounts.component';
import { CheckedListComponent } from './checked-list/checked-list.component';

export const routes = [
   {path: 'transactions/:accid', title: "Transactions", component: TransactionsComponent},
   {path: 'viewchecked/:accid', title: "Checked Transactions", component: CheckedListComponent},
   {path: 'transferaccounts', title: "Transfer Accounts", component: TransferaccountsComponent},
   {path: 'standingorders', title: "Standing Orders", component: StandingordersComponent},
   {path: 'accounts', title: "Accounts", component: AccountsComponent}
   
   //, {path: '**', redirectTo: '/'}
];

// Refresh page doesn't work which means I'll have to waste even more time getting
// something which is blindingly obvious to work again... wtf is it with these forking
// people who create this shirt.
// A clue seems to be
// onsameurlnavigation : relaod
// but the default is to do mothing (WTF -  why the fork do they think the user requested a forking refresh???)
// and there is fork all indication how or where to change the value from the default...

// Was finally ready to deploy a version using routing for access to StandingOrders and transactions and
// which supported use of the browser refresh button - in fact better than the current version since 
// it actually reloaded the currently displayed page rather than going back to the empty start view.
// Of course I was deluding myself to think that this basic of the the basic functionality was going to
// actually work in the 'production' version.... and it didn't - all that was displayed was the
// unformatted 'loading...' message. WTF! Found a bunch of 403 Permission Denied messages in the
// console log, for main.ts, styles.css, polyfills.ts etc. In other words all the files of the application.
// Obviously I wasted time determining that the 403 was a typical unix red herring design to fritter away
// the hours of the day as if there were too many of them. What it really meant was that it was trying to load
// the files from the root URL instead of the application folder (not sure of the correct buzzwords, it was looking
// for https://host/main.ts instead of https://host/myapp/main.ts. Didn't take too long to figure out this
// was due to the '<base href="/">' statement in index.html which was required to make the refresh button work. FORKING HELL!
// Once more down into the rabbit hole of making Angular work in a sensible, everyday way with a normal, completely
// standard configuration...
//
// Google suggests that the 'base href' must be set to the application folder name. This is plainly forking ridiculous
// but I guess it's the only way forward - difficult to test also.
// One article mentioned that for angular18 the href can be set per environment with something like:
// production:
// projects.PROJECT_NAME.architect.build.configurations.production.baseHref property for production 
// development
// projects.PROJECT_NAME.architect.build.configurations.development.baseHref for development.
//
// I think I saw something which said it could be set on the build command line, but can't find this.
// this maybe:
//   ng build --prod --base-href=/test/
// maybe (see https://angular.dev/cli/build)
//   ng build --prod --deploy-url=   
// Maybe using 'HashLocationStrategy' instead of the default 'PathLocationStrategy' would be better - this makes
// sense since the default settings seem to be ALWAYS the LEAST appropriate ones to use in real life.
// I've set app.module to do this but cannot test it at present
//
// https://angular.dev/tools/cli/deployment#routed-apps-must-fall-back-to-indexhtml
// maybe configuring apache to route the routing links to index.html will allow PathLocationStrategy
// to work, but will still need to set the base-href value.
//
// Maybe adding 'apiUrl: 'http://my-prod-url'' to the environment.ts will fix the problem?
// Maybe it will let me test... doesn't appear to have any effect on 'ng serve'
