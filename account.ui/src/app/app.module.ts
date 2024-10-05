// account.ui/src/app/app.module.ts
import { BrowserModule, HammerModule, HAMMER_GESTURE_CONFIG, HammerGestureConfig } from '@angular/platform-browser';
import { APP_INITIALIZER, importProvidersFrom, Injectable, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import { AppComponent } from './app.component';
import {AccountService} from '../shared/service/account.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { QrscannerComponent } from './qrscanner/qrscanner.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { StandingordersComponent } from './standingorders/standingorders.component';
import { ActivatedRouteSnapshot, BaseRouteReuseStrategy, provideRouter, RouteReuseStrategy, RouterLink, RouterLinkActive, RouterModule, RouterOutlet, withComponentInputBinding, withRouterConfig } from '@angular/router';
import { routes } from './app.routes';
import { AccountItem } from 'src/shared/model/accountitem.model';



// Magic required to get normal scrolling to work when swipe left is used.
// WTF! Who in their right mind stops scrolling just to get swipe left!!!!!
// Why don't any of the docs/examples mention this is required.
// Even worse! Select for copy is disabled! Fingers crossed it can be
// re-enabled with similar magic, eg. 'press' and 'tap'
@Injectable()
export class HammerConfigForNormalScroll extends HammerGestureConfig {

   // Also requires entry in @NgModule 'providers' below
   override overrides = <any> {
       'pinch': { enable: false },
       'rotate': { enable: false }
      //  ,'press': { enable: false },
      //  'tap' : { enable: false },
      //  'pan' : { enable: false }
   };
   // override options = { 'touchAction' : 'auto' }

   // This appears to restore the selection behviour for the swipe column   
   override options = {cssProps:{userSelect:'auto'}}  
   // Suggested in some places but seems to block the swipe
   // override buildHammer(element: HTMLElement) {
      // Hammer.defaults.cssProps.userSelect = '';
      // let mc = new Hammer(element, {
      //   touchAction: "pan-y",
      // });
      // return mc;
   //  }   
  
}


// Originally the fix for reload/refresh not working was to add
// this.router.routeReuseStrategy.shouldReuseRoute = () => { return false; };
// to app.module which seemed to solve the problem but compiler complained that
// it was deprecated and, of course, provided no useful indication what to replace it with. 
// Once again countless hours were wasted thanks to the grasshole continuous improvers
// whose sole purpose in life is to fork over the folks who are unfortunate enough to
// have been suckered into using this shirt. 
// Eventually I managed to put the thing below which,
// together with the 'provide: RouteReuseStrategy' and
// 'withRouterConfig({onSameUrlNavigation: 'reload'})' statements in 'providers' below,
// appears to replace the entry in app.module and allow reload/refresh to work!
//
// NB. It might be possible to avoid needing this if I can find a way to delay loading the
// transaction list until the account list has been re-loaded.
// It seems that the reload/refresh resets the account list in AccountService which can only
// be loaded via AppComponent. Normally this works because the link to the transactions can only
// be used when the account list is populated. On refresh/reload however the transaction link
// is triggered before the account list is present so nothing gets displayed. If I can find a way
// of forcing list to be present before it is used then it might work without this reuseroute shirt.
// Alas I have no clue how to get everything to wait for the list to be loaded!!
//export class FixRefreshRouteReuseStrategy extends BaseRouteReuseStrategy {
//  public override shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
//    return false; // (future.routeConfig === curr.routeConfig) || future.data.reuseComponent;
//  }
//}




@NgModule({ declarations: [
        AppComponent,
        QrscannerComponent
    ],
    bootstrap: [AppComponent], 
    imports: [
      BrowserModule,
      FormsModule,
      NgbModule,
      HammerModule,
      TransactionsComponent,
      StandingordersComponent,
      RouterOutlet,
      RouterLink,
      RouterLinkActive
   ], 
    providers: [
      AccountService, 
      //{ provide: APP_INITIALIZER, useFactory: () => appConfigFactory,
      //           deps: [AccountService],
      //           multi: true
      //},      
      DatePipe, 
      provideHttpClient(withInterceptorsFromDi()),
      { provide: HAMMER_GESTURE_CONFIG, useClass: HammerConfigForNormalScroll },
      //{ provide: RouteReuseStrategy, useClass: FixRefreshRouteReuseStrategy}, // supposed to replace this.router.routeReuseStrategy.shouldReuseRoute = () => { return false; }; in app.module
      provideRouter(routes, withComponentInputBinding()
      //, withRouterConfig({onSameUrlNavigation: 'reload'}) // onSameUrlNavigation must be reload for refresh button to work
      ) 
   ]
})
export class AppModule { }
