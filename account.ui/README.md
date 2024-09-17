# Accountui

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 14.2.3.
17-Sep-2024 Sooo, turns out AngularJS is actually Angular 1.x. I appear to be using Angular 14.2, ie.
quite a large difference which I imagine will make HammerJS unusable. In any case HammerJS appears to only
detect a 'swipe', all the heavy lifting of sliding a line and revealing the options is not present. Maybe 
more recent versions of Angular are more helpful for this now very common feature. In which case maybe the
most important thing to do before embarking on any major changes is to upgrade Angular to a more recent version.
The current version is 18 which will be replaced by 19 soon, so I guess I will have to bite the bullet and go to v18
now. v14 is not supported anymore to any upgrades are going to involve a fair amount of pain...

v14->v15 eclipse caused the pain by pushing the angular18 branch to the remote main branch - WTF? It forking
forces you to pcik a remote branch to make the branch from and then forks up the remote branch with the stuff
you explicitly created a branch for to avoid corrupting the main branch - I think the eclipse development forkheads must have vaped one too many noxious substance to actually be able to think anymore...

BTW it appears the upgrade is system widem ie. node/ng report the new versions even when the branch is switched back
to 'main'. This suggests that the upgrade will need to be done on every development system.


Commands:
ng update @angular/core@15 @angular/cli@15
ng add @ng-bootstrap/ng-bootstrap
ng update @angular/core@16 @angular/cli@16

Update to 17 gives the following:
Node.js version v16.17.1 detected.
The Angular CLI requires a minimum Node.js version of v18.19.
Please update your Node.js

It seems the only way to 'update your Node.js' is to download the installer from https://nodejs.org/
I downloaded the latest stable version: v20.17 and elected to install the additional tools. It installs something
called Chocolatey which might enable Node.js to be updated more easily in future rather than having to uninstall/reinstall.
Unfortunately the scary Powershell window which opened as part of the installation appears to have hung so I have no
idea whether the installation has succeeded or not. [UPDATE: not hung, just takes ages. It finished with a message 
'Type ENTER to exit:'. I types 'ENTER' and it exited.


Use the '--force' option to ignore the @ng-bootstrap/ng-bootstrap dependencies which appear to be specific to
one version of angular. Will need to update to suitable value once the final version of angular is installed.
ng update --force @angular/core@17 @angular/cli@17

Now at:
Angular CLI: 17.3.9
Node: 20.17.0
Package Manager: npm 8.19.2

ng update @angular/core@18 @angular/cli@18

npm uninstall -g @angular/cli
npm install -g @angular/cli@latest

16-Sep-2024 
Java version of AccountAPI is now live and working. This offers the possibility of making the app more sophisticated, eventually
with the aim of replacing the VB6 application. Since AccountVB6 has quite a lot of functionality by now and I have limited
time to work on it the replacement will be a very gradual process. 

I think the first thing to do is to add the possibility to update an entry. For this I need some way of triggering
the update on the phone. After a bit of though I realise that the 'swipe-left' function, eg. for unread, reminder, delete,
is just what I need but how the fork do I do that, not being mush of a UI person. Eventually I discovered that angular (AngularJS, whatever that is)
used to have this functionality but it is now moved to something called 'HammerJS'. I hope that this works for the 'AngularCLI' which I 
think I am using!!!! Anyway, the link is: https://hammerjs.github.io

26-Sep-2022
It was originally started with Angular 5 but it proved impossible to figure out what changes were needed to get
that version to run the 'ng build' when Angular was updated to 14. The the only solution was to create a new project from scratch and copy the actual code from the Angular 5 version into the new project. The builder at least ran on the new project, all that was left to do was fix all the bugs which suddenly appeared in the previously successful compiling code.

The end result of a few days of work was finally a compiling version of the same code. Hopefully this pain wont be needed again for a couple of years.

One bug was fixed during to port to Angular 14: the reason for the failure to clear the transaction fields and update 
the transaction list post submission was discovered - failure to parse the non-json response as json!! Took a while to figure out how to stop the json parsing but it now seems to work as expected - finally!

There is at least one new piece of functionality: a formatted EPC,. mobile payment, text can now be parsed into a transaction. Such texts are usually seen as QR codes so a QR code can be used to make a payment via the banking app and the same QR code can be used to make the entry into Account. It's a bit clumsy as there is no way to access the clipboard and unfortunately the iPhone 'Scan Text' option which appears sometimes is not capable of decoding a QR.

16-Oct-2022
QR reader can be invoked directly from the account web page. This is made possible by: 
http://github.com/mebjas/html5-qrcode. 
The behaviour is a bit crude, even after hours and hours of forking around with CSS, however it is certainly usable and definitely better than having to paste the result of an external scan into a text field. The downside is that the page must be served from a HTTPS server. Luckily my server already had an HTTPS version which account seems to work with, even on the iPhone using the VPN.


TODO: Make the scanner automatically request the permissions if necessary. It's a stupid unnecessary action which seems to be required each time the page is loaded - multiple uses of the scanner in the same 'session' do not require the permissions to be granted each time.

TODO: Close and remove the scanner when no scan is to be performed. Only way to do this at the moment is to reload the page.

TODO: Make the scanner into a component which can be displayed floating over the page like a dialog, with the page darkened. This is a real 'nice to have' since I managed to get some sort of control over the position of the scanner in the main page without messing up everything else.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build --configuration production` to build the project for installation on webserver. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.io/cli) page.

## vsCode settings

Things to change to make vsCode vaugely civilized:

editor.suggestOnTriggerCharacters  - stops it constantly popping up things while you are typing and then randomly
selecting shirt from the list.

Bunch of delay values need to be set to prevent it from constantly popping up things when the cursor is moved around