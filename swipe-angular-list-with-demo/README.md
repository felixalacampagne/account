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
