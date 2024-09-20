import { NgModule } from '@angular/core';
import { BrowserModule ,HammerModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import {SwipeAngularListModule} from './swipe-angular-list';
import { AppComponent } from './app.component';
import { HelloComponent } from './hello.component';

@NgModule({
  imports:      [ BrowserModule, FormsModule, SwipeAngularListModule ,HammerModule ],
  declarations: [ AppComponent, HelloComponent ],
  bootstrap:    [ AppComponent ]
})
export class AppModule { }
