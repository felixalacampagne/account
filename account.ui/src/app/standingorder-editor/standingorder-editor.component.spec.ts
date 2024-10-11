import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StandingorderEditorComponent } from './standingorder-editor.component';

describe('StandingorderEditorComponent', () => {
  let component: StandingorderEditorComponent;
  let fixture: ComponentFixture<StandingorderEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StandingorderEditorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StandingorderEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
