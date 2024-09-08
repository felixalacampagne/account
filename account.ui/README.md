# Accountui

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 14.2.3.

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

20-Aug-2024

NB. The source of ucanaccess is apparently now https://github.com/spannm/ucanaccess. Not sure if this is what I used
or the one from sourceforge. Actually I'm thinking of the 'dialect', the actual driver I get from maven so it should be
the right one.

I finally have a Java based backend which can read the Account Access database. This results in different API URLs and some
differences in the format of the data returned so a new version of the Account UI is needed. The backend is a Spring web application
which runs in my tomcat server. It may be possible to package the frontend in the same WAR as the backend code so I'm combining
the UI project with the API project. Needless to say getting Eclipse, Spring, Angular and Maven to work together is not going to
be straight forward but ho! hum! isn't it always the case with this continuous improvement shirt.

First thing to do is get the UI code into the project. No idea what the required structure is for this - Google was surprisingly
quiet on the matter. Will need to figure out how a Spring web application that includes a frontend is supposed to work. I have a feeling
that the UI project will need to do it's build thing and then copy the result somewhere into the API file structure so it gets
included in the WAR and somehow served by the tomcat (unlike the current version which is served by the Apache HTTP server.


07-Sep-2024 Decided to make JAccount live, ie. use the live DB on minnie. Only way to set the DB path is currently via the 
application properties so I needed to do a rebuild and redeploy. You guessed it.. that is obviously that is way to complex to 
actually work and the forking spring framework shirt now fails to start with some ludricous error about some XML class not being
found which is required by some incomprehensible class that I certainly am not knowingly using for anything and for which
there is not a shred of documentation anywhere. Why the fork did I bother with this shirt - I could have had something working
in a few hours using 'normal' java, instead the 'framework' has wasted weeks of my incredibly rare spare time, and it still isn't
actually working!

The error is:

BeanInstantiationException: Failed to instantiate [org.springframework.boot.web.servlet.filter.OrderedFormContentFilter]: Factory method 'formContentFilter' threw exception with message: Provider org.apache.xalan.processor.TransformerFactoryImpl not found

In the absence of any real idea how to fix this error I restarted the tomcat server. JAccount started OK. So once again the
magic shirt is forking around with me. Since random shirt is usually due to some sort of version mismatch I guess spring is
using something which doesn't work with something else - way to go Java committee, you managed to invent something that makes
Windows DLL issues seem totally inconsequential and trivial!
Google is not much help, the only reference to 'formContentFilter' suggests an incompatibility in Jackson, but I have no direct
references in the pom.xml, I only use the spring dependencies. I suppose it could be something related to the other applications
running on the Tomcat, eg. pagebuilderrest uses a later version of Jackson. There is no 'xalan' reference in the spring dependencies, according to the Eclipse dependency list so why the fork is it trying to use anything to do with xalan??

In the log there is a messages about 'idleTimeout' being ignored, so I guess that isn't going to cure the connection loss
which appears to occur after 5mins or so and which the super wonderful DB layer is too stupid to resolve by reconnecting!
The DB connection closed message seems to be

UcanaccessSQLException: UCAExc:::5.0.1 connection exception: closed

I don't know why the connection closes, maybe it is something that needs to be configured in the Ucanaccess stack of
software, although why the fork they would write the code to randomly close the DB is anybodies guess.

08-Sep-2024 Discovered there is a newer version of UCanaccess with different maven ids so will give it a try. Maybe it does
not need the extra 'dialect' stuff which would be good. What would be excellent is that the DB connection doesn't close itself
so I don't need to waste days trying to figure out how to get Hikari to open a connection when it is closed (why the fork 
doesn't it just do it?!?!?)

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