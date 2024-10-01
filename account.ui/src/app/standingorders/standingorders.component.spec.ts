import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StandingordersComponent } from './standingorders.component';

describe('StandingordersComponent', () => {
  let component: StandingordersComponent;
  let fixture: ComponentFixture<StandingordersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StandingordersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StandingordersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
