# Accountui

30-Jan-2025 wanted to install the 'Roboto Flex' using:

npm i @fontsource-variable/roboto-flex

but got loads of errors related to old version of stuff, I think. The chief culprit appeared to be
@angular/localize. Tried 

ng update @angular/localize

which gave a message about Angular CLI being outdated, did something and then said 'Package '@angular/localize' is not a dependency.' which is pretty stupid since it obviously is!

Then tried

npm update -g @angular/cli

to avoid the 'Installing a temporary Angular CLI' message when installing. It didn't work, still get the message!
Tried

ng update @ng-bootstrap/ng-bootstrap

This gave 'Package "@ng-bootstrap/ng-bootstrap" has a missing peer dependency of "@angular/localize" @ "^19.0.0".' so why the fork did it say it wasn't a dependency before? It then said it was installing packages, no idea which packages and finished with a green tick!


28-Jan-2025 Sick and tired of trying to use Angular Material docs and being taken to v19 pages
which seem vaugely relevant only to find there is no equivalent at all for v18.
Thus going to bite the bullet and upgrade to Angular 19.
According to https://angular.dev/update-guide?v=18.0-19.0&l=1 it should be easy-peasy so will
almost certainly entail hours of wasted effort trying to decipher incomprehensible error messages
into whatever actions are required to workaround the inevitable random changes. But hey! ho!,
that's what continuous improvement is all about.

- Upgrade to Angular19
      ng update @angular/core@19 @angular/cli@19
      optional migration: 
         new build system: selected: enter
         provide-initializer: selected: enter

- Update to Typescript 5.5: obviously docs give no clue how to do this, fingers crossed it is automatic
- Replace usages of BrowserModule.withServerTransition() with injection of the APP_ID token to set the application id instead. No forking clue what this means, fingers crossed I don't need to do anything.

- Migrate from using Router.errorHandler to withNavigationErrorHandler from provideRouter or errorHandler from RouterModule.forRoot. Equally no idea what this refers to, although I do use Routing and did have to fork around with it to get the browser refresh and back buttons to work as expected - fingers crossed this has not been broken by the continuous improvement grassholes.

So.... here goes

   Forking Hell! It doesn't update Material: still says 18.2.7 in package.json. No Angular documentation
   for this, of course. Google suggests:
      npm update @angular/material @angular/cdk
   it didn't work but suggests using '--force'.
   First I tried to update some of the packages referenced in the error message but they all gave similar
   error messages so in the end I did 
      npm update --force @ng-bootstrap/ng-bootstrap @angular/localize @ng-bootstrap/ng-bootstrap @angular/material @angular/cdk
   it completed with lots of meaningless warn messages about packages numbers.

   Tried to run 'ng serve' and was hit with a mass of deprecation warnings about 'Sass @import rules are deprecated'.
   Since these are required for Material and the link to some sort of workaround is blocked the only choice
   is to ignore or disable them. I found instructions to disable here: https://www.angulararchitects.io/en/blog/how-to-disable-the-angular-v19s-sass-compiler-deprecation-warnings/ - it requires a change in angular.json.
   That left 'WARNING RouterOutlet is not used within the template of CheckedListComponent' which was referring to
   an unused item in the @Component import list.

   Seems to be working OK. Now maybe I'll be able to figure out from the POS Angular material docs how to make it look more sensible!

   Repeated the upgrade for on site installation. 

   Realised that all the 'npm update' bullshirt had not actually done a thing to update Angular Material - it was still at 18.2 in package.json. More wasted effort to discover that it should be 'ng update', ie.

   ng update @angular/material @angular/cdk

   Now package.json is updated with '"@angular/material": "^19.1.1"'
   Repeated for off-site and it seemed to work aswell.

   So now the 'fun' starts because my nice red warning button is once again back to being the same blue as the other safe buttons - forking hell!
   Gave up trying to find the 'Material way' to set the button to warning colour and defined my own class which actually to uses the bootstrap warning colour which makes it consistent with the infinitely more pleasing bootstrap dialog used for transactions. I think Material's days in my code are numbered as whoever develops it appears to have a continuously-fork-the-idiots-who-use-this-shirt policy which does not exactly fill me full of joy.

10-Jan-2025 The 'auto-zoom' when entering transactions on the phone is starting to really
piddle me off. Alas it is something which is not going to go away, and it's been around a very long time judging by the number of SO requests for how to stop it.
The general idea appears to be that it happens when the input font size is less than 16 which is probably the case for '.form-control-sm'. This bit of magic is what is suggested to work around it:

input, textarea {
    font-size: 1rem !important;
}
@media (min-width: 768px) {
    input, textarea {
        font-size: inherit !important;
    }
}

The @media directive (or query) is used to conditionally do css depending on device charactersitics, eg. screen width.
(see https://css-tricks.com/a-complete-guide-to-css-media-queries/ )
The example font size changes above is probably incorrect, my guess is what it is trying to do is set the input/textarea
font size to 16px and if screen is wider than an iphone then override this with the default parent size. Since the 'screen' media-type 
is missing both have !important it is not likely to work well unless later definitions override earlier ones however the font size of the 
input fields was larger (and uglier).

Needless to say it the larger font had no impact on the annoying auto-zooming of the input fields.

Next thing I tried was adding 'maximum-scale=1' to the existing 'meta viewport' line in index.html. This actually seems
to have worked - no more annying auto-zoom and I can still zoom if I want to (since I use Safari, but maybe not with Chrome). 
Still haven't managed to get pinch to zoom on the list to work with the slide gesture - should try to figure it out as it is kind
of irritating having to find a non-list part of the screen to reset the zoom - but not so important now the zoom is not
happening all the time. (see https://webkit.org/blog/7367/new-interaction-behaviors-in-ios-10/ for potentially interesting stuff
about how to workaround annoying iPhone Safari behaviours!!)

09-Jan-2025 Added pageination controls which are visisble above (desktop only) and below the list. This demonstrates how to
use 'ng-template' to re-use bits of html and 'flex' to get a left,centre,right aligned layout (ie. a pretty basic layout which is
once again difficult to achieve with angular/bootstrap/whatever thanks to the complete lack of useful examples of how to use the
features).

24-Oct-2024 Really rockin' 'n rollin' now with working versions of transaction update, standing order
processing and editing and now 'checked balance' calculation and display and even paging to show 
the history. 

For now I'm keeping the original format of the transaction list with the add new transaction panel 
below the list since this is what seems to work best on the phone.
The standing order edit dialog was the first use of Angular Material and now I've used it for the
display of the checked entry balances. It seems to be essentially a really ugly replacement
for Bootstrap with virtually zero useful documentation for making it look any better. Therefore I've
ended up wasting hours trying to get it looking half way decent, and I'm not really sure I actually
achieved that! Still, I can see the checked balances when I need to so I suppose it's an 
achievement of sorts. I haven't figured out what the point of Material is given that Bootstrap seems 
to do the same thing but with better looks and documentation. At some stage I should opt to go with 
Material everywhere, or convert the Material things back to Bootstrap - it'll probably end staying a
mixture of both for the forseeable future since there are more interesting things to add, like the
transfers, previously known as phone transfers. Not so sure that the way it works for the VB app
makes sense nowadays, although having previous used destinations still makes it easier to enter
a transfer, and entering the info once to do transfers between 'my' accounts is also nice - same
would be nice when entering standing orders between 'my' accounts.

02-Oct-2024 After much blood, sweat and tears - well hours of wasted time thanks to the 
continuous improvement grassholes that keep changing how to do things (whilst senselessly 
breaking basic UI behaviour) and providing zero useable documentation
thereby ensuring all googling results in misinformation - I have managed to 
force the display of the transactions into something more or less acceptable to me.

So I thought it was time to start thinking about how to add screens for things like the 
standing order definitions, account definitions, etc. 

First thing to do was get the transaction list into a 'screen' of it's own. Naturally this 
was anything but straight forward. The biggest problem was getting the account menu to 
communicate the account to display the transaction for. 
There are all sorts of wonderous ways to transfer data between a parent and 
a child - which of course are all useless when the 'routing' to different screens comes into play. 
Thus only an id can be readily sent from the routing link to the transaction component. 
No problem, there is a list of accounts already loaded so just put that into the service so it can
be used to lookup the account by its id. No problem, right?? Wrong. When the browser 
is refreshed the list goes away, and there is no way to get the transaction list to wait for 
the account menu to reload it. So, once again, there go the hours and hours of my time 
disappearing out the window while trying to figure out how to workaround this. Again the 
continuous grassholes have worked their magic to ensure there is nothing useful for the 
current way to get this work, if there even is one. After a few false leads, like getting it to reload the same url when the refresh button is used instead of ignoring it (which worked!) I eventually figured out a way to pass the account object via the routing url. It looks pretty
clumsy in the address bar, but who gives a shirt about that. So now browser navigation works as I expect it to.

Eventually I might go back to passing just the id and doing a getAccount for a single id.

Next is to populate the standing order list, should be easypeasy now!!!! Afterall 
it's more or less the same thing as the transaction list, just slightly different data.


26-Sep-2024 Have managed to get the update modal working and have even managed to implement a crude 'swipe' to 
trigger it. Simply doing a swipe left triggers the update modal, on rows what can be updated. The 'locked' rows
are colourized to indicate that update is not available. On a desktop display there is an edit icon since swipe is
not available. 

I still didn't figure out how to do the modal as a different class so currently there is a 
big chunk of html which is more or less duplicated but changing this is low priority for now. The ability to update rows
has highlighted the next thing to be implemented: the standing order execution. Since I use the VB app less and less
there is a risk that the automatic payments will get entered in a timely way and won't serve their purpose of reminding
me to look out for the payments demands. Given that the web server is always running it seems the perfect place to
do the automatic payments. In theory having both the VB app and the Java code doing the automatic payments should not
cause a problem unless they happen to run both at the same time, which will be highly unlikely.

19-Sep-2024 After some experimentation with trying to use a library for 'swipe' I've given up on that idea
for the time being. The library I tried is unconfigurable for my purposes and I wasted hours just trying to
get it to compile with the new Angular. An alternative it to use Angular Material but that will require
too much effort with no gain. Therefore the next thing to do is to figure out how to display a popup
containing a transaction to be updated when an edit icon is clicked.

See this for an example of modal in use: https://stackblitz.com/angular/odaogjmkqod?file=app%2Fmodal-basic.ts

18-Sep-2024 Thought I'd found the perfect solution to doing the swipe to edit thing at https://www.npmjs.com/package/swipe-angular-list.
Of course I had forgotten about the grassholes responsible for continuously improving things so they no longer work. After
hours getting around the failures to build due to some version being different to some other version I nearly got the swipe demo
to compile until 'ng serve' came up with "error NG6002: 'SwipeAngularListModule' does not appear to be an NgModule class. 'SwipeAngularListModule'
is not compatible with Angular Ivy". WTF is Angular Ivy!?!?!?!? Guess I'm just going to have to re-think how updating a row is triggered. 
Thank you continuous improvement grassholes for once again forking things up.

The source to swipe-angular-list is at https://github.com/leifermendez/swipe-angular-list/tree/master
I suppose it might be possible to use it directly in my project and maybe then it would work - it might be just the
forking around with versions necessary to get it to compile which has broken something. Do I really want to mess
around fixing something I know nothing about, though????

Well I did download the source and merged it with the swipe-demo code and eventually got it to run 
but not without wasting hours trying to figure out bizarre compile failure messages like:

"Error: Cannot read properties of undefined (reading 'setAttribute')"
"Error: link.parentNode.insertBefore is not a function"

Needless so say the cause of the messages was very very far from being obvious. Thanks to a lucky guess and a random 
unrelated google page the cause of the problem was identified as being the 'index.hml' file. Fork only knows why it 
was a problem but replacing the demo version with the one from accoutui made the bizarre messages go away.

Unfortunately the lunacy does not end with getting the demo to run. Attempts to modify the data displayed to 
more closely resemble my needs results in an empty screen and yet another obfuscated error message referring 
to obfuscated code so no way to figure out what is going on. 
It appears that the component only works if the first item in the list line is an image, 
using normal text causes the bizzare message. I certainly don't need images in my list. 
It also appears that the list line height is fixed at some absurdly large value which is also something I don't need. 
Pity, since the swipe thing is more or less what I was thinking of and it sort of worked in a normal, 
non-touch, browser.

I think that to get something I can use in the shortest time, I'll initially just go with a teeny tiny edit icon,
eventually maybe a 'long-press'/right-click 
[long press is no good, it is the trigger for selecting text
double-click/tap is no good as it is the trigger for zooming the page
] 
to trigger the edit and who knows by the time I try that there might be
an 'out-of-the-box' implementation of swipe to edit in angular. I did notice something in 'Angular material' that
might work - the 'Drawer' - but don't know if it can be adapted to a list.


17-Sep-2024 Sooo, turns out AngularJS is actually Angular 1.x. I appear to be using Angular 14.2, ie.
quite a large difference which I imagine will make HammerJS unusable. In any case HammerJS appears to only
detect a 'swipe', all the heavy lifting of sliding a line and revealing the options is not present. Maybe 
more recent versions of Angular are more helpful for this now very common feature. In which case maybe the
most important thing to do before embarking on any major changes is to upgrade Angular to a more recent version.
The current version is 18 which will be replaced by 19 soon, so I guess I will have to bite the bullet and go to v18
now. v14 is not supported anymore so any upgrades are going to involve a fair amount of pain...

v14->v15 eclipse caused the pain by pushing the angular18 branch to the remote main branch - WTF? It forking
forces you to pick a remote branch to branch from and then forks up the remote with the stuff
you explicitly created a branch off to avoid corrupting it - I think the eclipse development forkheads must have vaped one 
too many noxious substance to actually be able to think anymore...

BTW it appears the upgrade is system widem ie. node/ng report the new versions even when the branch is switched back
to 'main'. This suggests that the upgrade will need to be done on every development system and that there is no going back!


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

ng update --force @angular/core@18 @angular/cli@18

Now at:
   Angular CLI: 18.2.4
   Node: 20.17.0
   Package Manager: npm 8.19.2
   OS: win32 x64
   
   Angular: 18.2.4
   ... animations, cli, common, compiler, compiler-cli, core, forms
   ... localize, platform-browser, platform-browser-dynamic, router
   
   Package                         Version
   ---------------------------------------------------------
   @angular-devkit/architect       0.1802.4
   @angular-devkit/build-angular   18.2.4
   @angular-devkit/core            18.2.4
   @angular-devkit/schematics      18.2.4
   @schematics/angular             18.2.4
   rxjs                            7.5.7
   typescript                      5.4.5
   zone.js                         0.14.10

npm uninstall -g @angular/cli
npm install -g @angular/cli@latest
ng add @ng-bootstrap/ng-bootstrap

Seems to build ok with 'ng serve' so I guess it is good to go!!

16-Sep-2024 
Java version of AccountAPI is now live and working. This offers the possibility of making the app more sophisticated, eventually
with the aim of replacing the VB6 application. Since AccountVB6 has quite a lot of functionality by now and I have limited
time to work on it the replacement will be a very gradual process. 

I think the first thing to do is to add the possibility to update an entry. For this I need some way of triggering
the update on the phone. After a bit of though I realise that the 'swipe-left' function, eg. for unread, reminder, delete,
is just what I need but how the fork do I do that, not being much of a UI person. Eventually I discovered that angular (AngularJS, whatever that is)
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
