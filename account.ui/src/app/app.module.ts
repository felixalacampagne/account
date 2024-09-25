import { BrowserModule, HammerModule, HAMMER_GESTURE_CONFIG, HammerGestureConfig } from '@angular/platform-browser';
import { importProvidersFrom, Injectable, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import { AppComponent } from './app.component';
import {AccountService} from '../shared/service/account.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { QrscannerComponent } from './qrscanner/qrscanner.component';


// Magic required to get normal scrolling to work when swipe left is used.
// WTF! Who in their right mind stops scrolling just to get swipe left!!!!!
// Why don't any of the docs/examples mention this is required
@Injectable()
export class HammerConfigForNormalScroll extends HammerGestureConfig {

   // Also requires entry in @NgModule 'providers' below
   override overrides = <any> {
       'pinch': { enable: false },
       'rotate': { enable: false }
   }

   // Suggested in some places but seems to block the swipe
   // override buildHammer(element: HTMLElement) {
   //    let mc = new Hammer(element, {
   //      touchAction: "auto",
   //    });
   //    return mc;
   //  }   
}

@NgModule({ declarations: [
        AppComponent,
        QrscannerComponent
    ],
    bootstrap: [AppComponent], 
    imports: [
      BrowserModule,
      FormsModule,
      NgbModule,
      HammerModule
   ], 
    providers: [
      AccountService, 
      DatePipe, 
      provideHttpClient(withInterceptorsFromDi()),
      { provide: HAMMER_GESTURE_CONFIG, useClass: HammerConfigForNormalScroll }
   ]
})
export class AppModule { }

