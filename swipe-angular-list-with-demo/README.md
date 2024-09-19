# SwipeDemo
https://www.npmjs.com/package/swipe-angular-list

This should have been so easy - and it wasn't.
I tried copying the demo project but it wont even install due to dependency issues. I tried changing
the angular versions in package.json to the installed version but swipe-angular-list still gave 
errors.
Then I installed using  

npm install --legacy-peer-deps

This didn't give an error but gave very many warnings for 'deprecated' things that I have no clue
why they are referenced.

npm install --save-dev @angular/cli --force
npm install --save-dev @angular/compiler --force
npm install --save-dev @angular/compiler-cli --force

https://www.npmjs.com/package/swipe-angular-list
npm i swipe-angular-list --legacy-peer-dep
npm install @angular/platform-browser --save

On a different system after installing Node.js v20 it was not possible to use npm install do to
many bad dependencies.

Since the dependencies are all things added for angular I figured creating a new project from scratch
should provide configs with appropriate versions. Of course, this angular being the result of
constant continuous improvement not even the standard 'npm new' command worked. These are some
of the things I tried in an attempt to get it to work:

npm install -g @angular/cli@18
npx @angular/cli@18 new angular18-empty

That worked!

I found a workaround for the disappearing text when no icon is used - for some reason a 'span' around the text
fixes an error seen in the console which must have been related - no way to know from the browser with 
the obfuscated code.

Haven't found a way to make the lines a more sensible height. I think it is controlled from the item-list.component.css
file. Overrides added in the app .CSS do not affect anything.
Really thinking this is not worth the effort...

I found another slide library which is based on Angular Material: https://github.com/ShankyTiwari/mat-list-touch
Not sure it actually does the sliding of the items because the dmeo doesn't work. It's written for Angular 10
so would probably suffer from the same version issues I had for swipe-angular-list. Given it would probably require
me to convert everythign to Material, which is something I've been thinking of trying, then maybe then would be
the time for trying to implement the swiping. Since that is for the far future, better now to concentrate on
getting the functionality in and usable and then I can faff around with the UI. 

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 18.2.4.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via a platform of your choice. To use this command, you need to first add a package that implements end-to-end testing capabilities.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
