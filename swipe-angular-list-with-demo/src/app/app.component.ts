import { Component } from '@angular/core';

@Component({
  selector: 'my-app',
  templateUrl: './app.component.html',
  styleUrls: [ './app.component.css' ]
})
export class AppComponent  {
  name = 'Angular';
    list = [
    {
      id: 1,
      title: 'Realizar la tarea asignada!',
      subTitle: '9:00pm'
    },
    {
      id: 2,
      title: 'Visitar al perro en casa de tu amiga',
      subTitle: '9:00pm'
    },
    {
      id: 3,
      title: 'Llamar al doctor',
      subTitle: '9:00pm'
    },
    {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    },
        {
      id: 4,
      title: 'Buscar el auto en el taller',
      subTitle: '9:00pm'
    }
  ];


  action = (a) => {
    console.log(a);

  };

  clickOnItem = (a) => {
    console.log('Click on item');
  }

  swipeCallback = (a) => {
    console.log('Callback Swipe', a);
  }
}
