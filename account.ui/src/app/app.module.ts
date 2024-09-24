import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import { AppComponent } from './app.component';
import {AccountService} from '../shared/service/account.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { QrscannerComponent } from './qrscanner/qrscanner.component';


@NgModule({ declarations: [
        AppComponent,
        QrscannerComponent
    ],
    bootstrap: [AppComponent], 
    imports: [
      BrowserModule,
      FormsModule,
      NgbModule], 
    providers: [
      AccountService, 
      DatePipe, 
      provideHttpClient(withInterceptorsFromDi())]
})
export class AppModule { }
